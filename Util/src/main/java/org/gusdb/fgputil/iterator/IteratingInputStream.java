package org.gusdb.fgputil.iterator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.log4j.Logger;

public class IteratingInputStream extends InputStream {

  private static final Logger LOG = Logger.getLogger(IteratingInputStream.class);

  public interface DataProvider {

    byte[] getHeader();

    byte[] getRowDelimiter();

    Iterator<byte[]> getRowIterator();

    byte[] getFooter();

  }

  private enum CurrentValueType {
    BEGIN, HEADER, ROW_DELIMETER, ROW, FOOTER, END
  }

  private final byte[] _header;
  private final byte[] _footer;
  private final Iterator<byte[]> _rowIterator;
  private final byte[] _rowDelimiter;

  // current result row (0 until a row is being returned); will remain the
  // 1-based index of the row until the next row is loaded into the buffer.
  private int _rowNum = 0;

  private CurrentValueType _currentValueType = CurrentValueType.BEGIN;
  private byte[] _rowBuffer = new byte[0];
  private int _rowBufferIndex = 0;

  public IteratingInputStream(DataProvider dataProvider) {
    _header = dataProvider.getHeader();
    _footer = dataProvider.getFooter();
    _rowDelimiter = dataProvider.getRowDelimiter();
    _rowIterator = dataProvider.getRowIterator();
  }

  @Override
  public int read() throws IOException {
    while (_rowBufferIndex >= _rowBuffer.length) {
      // buffer "empty"; load next value
      _rowBufferIndex = 0;
      switch(_currentValueType) {
        case BEGIN:
          _rowBuffer = _header;
          _currentValueType = CurrentValueType.HEADER;
          break;
        case HEADER:
          if (_rowIterator.hasNext()) {
            _rowBuffer = _rowIterator.next();
            _currentValueType = CurrentValueType.ROW;
            _rowNum++;
            if ((_rowNum > 44350)) LOG.info("Loaded row " + _rowNum + ": " + new String(_rowBuffer));
          }
          else {
            _rowBuffer = _footer;
            _currentValueType = CurrentValueType.FOOTER;
          }
          break;
        case ROW_DELIMETER:
          _rowBuffer = _rowIterator.next();
          _currentValueType = CurrentValueType.ROW;
          _rowNum++;
          if ((_rowNum > 44350)) LOG.info("Loaded row " + _rowNum + ": " + new String(_rowBuffer));
          break;
        case ROW:
          if (_rowIterator.hasNext()) {
            _rowBuffer = _rowDelimiter;
            _currentValueType = CurrentValueType.ROW_DELIMETER;
          }
          else {
            _rowBuffer = _footer;
            _currentValueType = CurrentValueType.FOOTER;
          }
          break;
        case FOOTER:
          _rowBuffer = new byte[0];
          _currentValueType = CurrentValueType.END;
          break;
        case END:
          return -1;
      }
    }
    return Byte.toUnsignedInt(_rowBuffer[_rowBufferIndex++]);
  }

}
