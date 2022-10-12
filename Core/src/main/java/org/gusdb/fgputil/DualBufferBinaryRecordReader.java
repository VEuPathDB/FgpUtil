package org.gusdb.fgputil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

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

  private final Path _file;
  private final ExecutorService _exec;
  private final AsynchronousFileChannel _channel;
  private final int _recordLength;
  private final int _bufferSize;
  private final Function<ByteBuffer, T> _deserializer;

  private long _fileCursor = 0;
  private boolean _wasLastFill = false;
  private ByteBuffer _current;
  private ByteBuffer _next;
  private Future<Integer> _nextFill;

  /**
   * Creates a instance that will read from the passed file, expecting records of size recordLength
   * (in bytes).  Buffer size will equal recordLength * recordsPerBuffer, so if memory is a concern,
   * callers can choose a smaller number of records if record size is large.  The first buffer will
   * begin filling immediately upon creation of this object.
   *
   * @param file file to read
   * @param recordLength size of binary records in bytes
   * @param recordsPerBuffer number of records to store in a buffer at one time.  Note: memory
   * footprint will be approximately recordLength * recordsPerBuffer * 2.
   * @throws IOException if unable to read file
   */
  public DualBufferBinaryRecordReader(Path file, int recordLength, int recordsPerBuffer,
                                      Function<ByteBuffer, T> deserializer) throws IOException {
    _file = file;
    _exec = Executors.newSingleThreadExecutor();
    _channel = AsynchronousFileChannel.open(file, Set.of(StandardOpenOption.READ), _exec);
    _recordLength = recordLength;
    _bufferSize = recordLength * recordsPerBuffer;
    _current = ByteBuffer.allocate(_bufferSize);
    _next = ByteBuffer.allocate(_bufferSize);
    _deserializer = deserializer;

    // start reading into _next immediately
    startNextFill();
    // put _current's position at its limit so we reset right away
    _current.position(_current.capacity());
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
      // check again in case latest fill was empty
      if (!_current.hasRemaining()) {
        return Optional.empty();
      }
    }
    // read the next record
    return Optional.of(_deserializer.apply(_current));
  }

  private void startNextFill() {
    _next.clear();
    _nextFill = _channel.read(_next, _fileCursor);
    _fileCursor += _bufferSize;
  }

  private void resetCurrent() {
    try {
      // wait for the fill to complete (hopefully will be ready immediately)
      Integer bytesRead = _nextFill.get();

      // throw if bytes does not represent an exact number of records
      if (bytesRead != -1 && bytesRead % _recordLength != 0) {
        throw new RuntimeException("File " + _file + " does not contain a number of bytes divisible " +
            "by [record length] " + _recordLength + "; last buffer fill had " + bytesRead + " bytes.");
      }

      // set last buffer if unable to completely fill
      if (bytesRead < _bufferSize) {
        _wasLastFill = true;
      }

      // sets _next up to be read from
      _next.flip();

      // swap the buffers
      ByteBuffer tmp = _next;
      _next = _current;
      _current = tmp;

      // start the next fill as long as current is not the last buffer
      if (!_wasLastFill) {
        startNextFill();
      }
    }
    catch (InterruptedException e) {
      close();
      // FIXME: Throw exception here?  Maybe unnecessary...?
    }
    catch (ExecutionException e) {
      close();
      throw new RuntimeException("Unable to complete reading of file " + _file, e);
    }
  }

  @Override
  public void close() {
    IoUtil.closeQuietly(_channel);
    _exec.shutdown();
  }

}
