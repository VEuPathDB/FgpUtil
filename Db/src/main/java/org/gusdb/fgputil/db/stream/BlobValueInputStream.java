package org.gusdb.fgputil.db.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;

import org.gusdb.fgputil.db.SqlUtils;

/**
 * Convenience class to wrap a binary data field in an input stream.  Doing so
 * enables the caller to stream a field of a result set to a destination using
 * only I/O APIs.
 * 
 * @author rdoherty
 */
public class BlobValueInputStream extends InputStream implements Wrapper {

  private Connection _connection;
  private Statement _statement;
  private ResultSet _resultSet;
  private InputStream _dataStream;

  /**
   * Creates an input stream of data from the passed field of the passed
   * result set.
   * 
   * @param connection Connection over which the data will be passed
   * @param statement Statement which produced the result set (may be null)
   * @param resultSet ResultSet from which to retrieve data
   * @param dataFieldName name of field containing binary data
   * @throws SQLException if column name is invalid or other DB problem occurs
   */
  public BlobValueInputStream(Connection connection, Statement statement, ResultSet resultSet, String dataFieldName)
      throws SQLException {
    _connection = connection;
    _statement = statement;
    _resultSet = resultSet;
    _dataStream = _resultSet.getBinaryStream(dataFieldName);
    if (_dataStream == null) { // NULL value in DB
      _dataStream = new ByteArrayInputStream(new byte[0]);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int read() throws IOException {
    return _dataStream.read();
  }

  /**
   * Closes the input stream.  Also closes the ResultSet, Statement, and
   * Connection associated with the streamed data.
   */
  @Override
  public void close() throws IOException {
    try {
      _dataStream.close();
    }
    catch (Exception e) {
      // do nothing; hopefully will be fixed when we close result set
    }
    finally {
      SqlUtils.closeQuietly(_resultSet, _statement, _connection);
    }
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException("This class does not wrap an instance of " + iface.getName());
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }
}
