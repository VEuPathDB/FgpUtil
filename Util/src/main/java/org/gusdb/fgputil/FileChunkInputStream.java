package org.gusdb.fgputil;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.log4j.Logger;

public class FileChunkInputStream extends FileInputStream {

  private static final Logger LOG = Logger.getLogger(FileChunkInputStream.class);

  private final long _lastByteToRead;
  private long _currentByte = 0;

  /**
   * Creates a input stream that will read a range of bytes from the passed file
   * starting with the beginning of the passed range and ending with either the
   * end of the range or the end of the file, whichever comes first.  If the
   * beginning of the range is earlier than the first byte of the file, reads
   * start at the beginning of the file.  Treats range as zero-based i.e. a
   * range of 0-9 inclusive will read the first 10 bytes of the file.
   * 
   * @param filePath path to a regular file
   * @param byteRange range of bytes to read
   * @throws IOException if unable to read file
   */
  public FileChunkInputStream(Path filePath, Range<Long> byteRange) throws IOException {
    super(filePath.toFile());
    _lastByteToRead = (byteRange.isEndInclusive() ?
        byteRange.getEnd() : byteRange.getEnd() - 1);
    long bytesToSkip = (byteRange.isBeginInclusive() ?
        byteRange.getBegin() : byteRange.getBegin() + 1);
    if (bytesToSkip <= 0) {
      _currentByte = 0;
      LOG.debug("Don't need to skip any bytes. Will read until byte " + _lastByteToRead);
    }
    else if (bytesToSkip > 0) {
      _currentByte = skip(bytesToSkip);
      LOG.debug("Successfully skipped first " + _currentByte + " bytes. Will read until byte " + _lastByteToRead);
    }
  }

  private int bytesLeftToRead() {
    return (int)(_lastByteToRead - _currentByte + 1);
  }

  @Override
  public int read() throws IOException {
    if (_currentByte > _lastByteToRead) {
      return -1;
    }
    _currentByte++;
    return super.read();
  }

  @Override
  public int read(byte b[]) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    LOG.debug("INPUT[" + off + "," + len + "] current: " + _currentByte + ", last: " + _lastByteToRead);
    if (_currentByte > _lastByteToRead) {
      return -1;
    }
    if (_currentByte + len > _lastByteToRead) {
      len = bytesLeftToRead();
    }
    _currentByte += len;
    return super.read(b, off, len);
  }

  @Override
  public int available() throws IOException {
    return Math.min(bytesLeftToRead(), super.available());
  }
}
