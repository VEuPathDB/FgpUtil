package org.gusdb.fgputil.iterator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.log4j.Logger;

public class IteratingInputStream extends InputStream {

  private static final Logger LOG = Logger.getLogger(IteratingInputStream.class);

  public interface DataProvider {

    byte[] getHeader();

    byte[] getRecordDelimiter();

    Iterator<byte[]> getRecordIterator();

    byte[] getFooter();

  }

  private enum CurrentValueType {
    BEGIN, HEADER, RECORD_DELIMETER, RECORD, FOOTER, END
  }

  private final byte[] _header;
  private final byte[] _footer;
  private final Iterator<byte[]> _recordIterator;
  private final byte[] _recordDelimiter;

  // current record (0 until a record is being returned); will remain the
  // 1-based index of the record until the next record is loaded into the buffer.
  private int _recordNum = 0;

  private CurrentValueType _currentValueType = CurrentValueType.BEGIN;
  private byte[] _recordBuffer = new byte[0];
  private int _recordBufferIndex = 0;

  public IteratingInputStream(DataProvider dataProvider) {
    _header = dataProvider.getHeader();
    _footer = dataProvider.getFooter();
    _recordDelimiter = dataProvider.getRecordDelimiter();
    _recordIterator = dataProvider.getRecordIterator();
  }

  @Override
  public int read() throws IOException {
    while (_recordBufferIndex >= _recordBuffer.length) {
      // buffer "empty"; load next value
      _recordBufferIndex = 0;
      switch(_currentValueType) {
        case BEGIN:
          _recordBuffer = _header;
          _currentValueType = CurrentValueType.HEADER;
          break;
        case HEADER:
          if (_recordIterator.hasNext()) {
            _recordBuffer = _recordIterator.next();
            _currentValueType = CurrentValueType.RECORD;
            _recordNum++;
          }
          else {
            _recordBuffer = _footer;
            _currentValueType = CurrentValueType.FOOTER;
          }
          break;
        case RECORD_DELIMETER:
          _recordBuffer = _recordIterator.next();
          _currentValueType = CurrentValueType.RECORD;
          _recordNum++;
          break;
        case RECORD:
          if (_recordIterator.hasNext()) {
            _recordBuffer = _recordDelimiter;
            _currentValueType = CurrentValueType.RECORD_DELIMETER;
          }
          else {
            LOG.info("Out of rows. Using footer.");
            _recordBuffer = _footer;
            _currentValueType = CurrentValueType.FOOTER;
          }
          break;
        case FOOTER:
          _recordBuffer = new byte[0];
          _currentValueType = CurrentValueType.END;
          break;
        case END:
          LOG.info("Streamed " + _recordNum + " records.");
          return -1;
      }
    }
    return Byte.toUnsignedInt(_recordBuffer[_recordBufferIndex++]);
  }
}
