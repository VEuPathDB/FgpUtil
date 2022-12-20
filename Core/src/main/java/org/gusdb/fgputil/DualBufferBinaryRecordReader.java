package org.gusdb.fgputil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;

import org.gusdb.fgputil.iterator.OptionStream;

/**
 * Reads uniform binary records from a file, using a double buffered system that allows
 * I/O-bound applications to read and process the binary records in parallel.  While the
 * contents of one buffer are being delivered as requested to the caller, a background
 * thread is filling a second buffer so its contents are ready as close to when they are
 * needed as possible.
 *
 * @author rdoherty
 */
public class DualBufferBinaryRecordReader<T> implements OptionStream<T>, AutoCloseable {
  private static final ExecutorService DESERIALIZER_THREAD_POOL = Executors.newFixedThreadPool(6, r -> new Thread(r, "deserializer"));
  private static final ExecutorService DISK_THREAD_POOL = Executors.newCachedThreadPool(r -> new Thread(r, "disk-read"));

  private final Path _file;
  private final AsynchronousFileChannel _channel;
  private final int _bufferSize;

  private final int _recordLength;

  private boolean _wasLastFill;
  private long _fileCursor = 0;
  private Buffer<T> _current;
  private Buffer<T> _next;

  private CompletableFuture<Buffer.FileChannelReadResult> _nextFill;
  private Timer _awaitingFillTimer = null;

  /**
   * Creates a instance that will read from the passed file, expecting records of size recordLength
   * (in bytes).  Buffer size will equal recordLength * recordsPerBuffer, so if memory is a concern,
   * callers can choose a smaller number of records if record size is large.  The first buffer will
   * begin filling immediately upon creation of this object.
   *
   * @param file             file to read
   * @param recordLength     size of binary records in bytes
   * @param recordsPerBuffer number of records to store in a buffer at one time.  Note: memory
   *                         footprint will be approximately recordLength * recordsPerBuffer * 2.
   * @throws IOException if unable to read file
   */
  public DualBufferBinaryRecordReader(Path file, int recordLength, int recordsPerBuffer,
                                      Function<ByteBuffer, T> deserializer) throws IOException {
    _file = file;
    _channel = AsynchronousFileChannel.open(file, Set.of(StandardOpenOption.READ), DISK_THREAD_POOL);
    _bufferSize = recordLength * recordsPerBuffer;
    _recordLength = recordLength;
    _current = new Buffer<>(recordsPerBuffer, deserializer, recordLength);
    _next = new Buffer<>(recordsPerBuffer, deserializer, recordLength);

    // start reading into _next immediately
    startNextFill();
  }

  /**
   * Returns the next record
   */
  @Override
  public Optional<T> next() {
    if (!_current.hasRemaining()) {
      if (_wasLastFill) {
        return Optional.empty();
      }
      resetCurrent();
      // check again in case the latest fill was empty
      if (!_current.hasRemaining()) {
        return Optional.empty();
      }
    }
    // read the next record
    return _current.next();
  }

  public long getTimeAwaitingFill() {
    return _awaitingFillTimer == null ? 0L : _awaitingFillTimer.getElapsed();
  }

  private void startNextFill() {
    _nextFill = _next.startFill(_channel, _fileCursor, DESERIALIZER_THREAD_POOL);
    _fileCursor += _bufferSize;
  }

  private void resetCurrent() {
    try {
      // wait for the fill to complete (hopefully will be ready immediately)
      if (_awaitingFillTimer == null){
        _awaitingFillTimer = Timer.start();
      } else {
        _awaitingFillTimer.resume();
      }
      int bytesRead = _nextFill.get()._bytesRead;
      _awaitingFillTimer.pause();

      // throw if bytes does not represent an exact number of records
      if (bytesRead != -1 && bytesRead % _recordLength != 0) {
        throw new RuntimeException("File " + _file + " does not contain a number of bytes divisible " +
            "by [record length] " + _recordLength + "; last buffer fill had " + bytesRead + " bytes.");
      }

      // set last buffer if unable to completely fill
      if (bytesRead < _bufferSize) {
        _wasLastFill = true;
      }

      // swap the buffers
      Buffer<T> tmp = _next;
      _next = _current;
      _current = tmp;

      // start the next fill as long as current is not the last buffer
      if (!_wasLastFill) {
        startNextFill();
      }
    } catch (InterruptedException e) {
      close();
      // FIXME: Throw exception here?  Maybe unnecessary...?
    } catch (ExecutionException e) {
      close();
      throw new RuntimeException("Unable to complete reading of file " + _file, e);
    }
  }

  @Override
  public void close() {
    IoUtil.closeQuietly(_channel);
  }

  private static class Buffer<T> {
    private final int _recordLength;
    private final ByteBuffer _byteBuf;
    private final Object[] _deserializedElements;
    private final Function<ByteBuffer, T> _deserializer;
    private final Object _elementAvailableLock;
    private int _recordsReadFromDiskCount;
    private int _deserializedRecordsConsumed;
    private int _deserializedElementsAvailable;

    public Buffer(int bufferSize, Function<ByteBuffer, T> deserializer, int recordLength) {
      _deserializer = deserializer;
      _deserializedElements = new Object[bufferSize];
      _elementAvailableLock = new Object();
      _byteBuf = ByteBuffer.allocate(bufferSize * recordLength);
      _recordLength = recordLength;
      _recordsReadFromDiskCount = 0;
      _deserializedRecordsConsumed = 0;
      _deserializedElementsAvailable = 0;
    }

    public boolean hasRemaining() {
      return _recordsReadFromDiskCount != _deserializedRecordsConsumed && _recordsReadFromDiskCount != -1;
    }

    /**
     * Return the next available deserialized element from the buffer.
     *
     * @return
     */
    public Optional<T> next() {
      // If all elements read from disk have been deserialized and consumed, return empty.
      if (_recordsReadFromDiskCount == _deserializedRecordsConsumed) {
        return Optional.empty();
      }
      // Lock while checking if elements are available.
      synchronized (_elementAvailableLock) {
        if (_deserializedRecordsConsumed == _deserializedElementsAvailable) {
          try {
            // If we have consumed all deserialized elements, release lock and wait for producer thread.
            _elementAvailableLock.wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
          }
        }
      }
      final T element = (T) _deserializedElements[_deserializedRecordsConsumed];
      _deserializedRecordsConsumed++;
      return Optional.of(element);
    }

    /**
     * Start asynchronously filling the {@link Buffer#_byteBuf}. Once data is read from disk, start deserializing into
     * {@link Buffer#_deserializedElements} array buffer.
     *
     * @param channel         File channel used to read from disk.
     * @param fileCursor      Cursor indicating where to begin the read from file.
     * @param executorService Thread pool used to read from file.
     * @return
     */

    public CompletableFuture<FileChannelReadResult> startFill(AsynchronousFileChannel channel, long fileCursor, ExecutorService executorService) {
      this._byteBuf.clear();
      this._deserializedRecordsConsumed = 0;
      this._deserializedElementsAvailable = 0;
      final CompletableFuture<FileChannelReadResult> bufferFill = new CompletableFuture<>();
      // Wrapper the {@link AsynchronousFileChannel#read(ByteBuffer, long)} method returning a CompletableFuture
      // instead of a Future to enable chaining.
      channel.read(this._byteBuf, fileCursor, null, new CompletionHandler<Integer, Void>() {
        public void completed(Integer result, Void attachment) {
          if (result == -1) {
            _recordsReadFromDiskCount = -1;
          } else {
            _recordsReadFromDiskCount = result / Buffer.this._recordLength;
          }

          _byteBuf.flip();
          bufferFill.complete(new FileChannelReadResult(result, _byteBuf));
        }

        public void failed(Throwable exc, Void attachment) {
          bufferFill.completeExceptionally(exc);
        }
      });

      bufferFill.thenAcceptAsync((channelReadResult) -> {
        for (int i = 0; i < this._recordsReadFromDiskCount; ++i) {
          _deserializedElements[i] = this._deserializer.apply(channelReadResult._byteBuffer);
          // Lock while we increment the counter indicating available elements. The consumer will check if elements are
          // available and block if not so we need to ensure the count is consistent.
          synchronized (this._elementAvailableLock) {
            _deserializedElementsAvailable++;
            // The only scenario where our consumer is awaiting this lock is if there are no deserialized elements
            // available to read. If this is the case, we are making one available here, so we notify the consumer.
            if (this._deserializedElementsAvailable - 1 == this._deserializedRecordsConsumed) {
              _elementAvailableLock.notify();
            }
          }
        }
      }, executorService);
      return bufferFill;
    }

  private static class FileChannelReadResult {
    private int _bytesRead;
    private ByteBuffer _byteBuffer;

    public FileChannelReadResult(int _bytesRead, ByteBuffer _byteBuffer) {
      this._bytesRead = _bytesRead;
      this._byteBuffer = _byteBuffer;
    }
  }
  }
}
