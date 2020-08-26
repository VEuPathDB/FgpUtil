package org.gusdb.fgputil.db.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Default driver initializer class.
 * 
 * @author rdoherty
 */
public class DefaultDbDriverInitializer implements DbDriverInitializer {

  /**
   * This method simply initializes the driver passed using <code>Class.forName(String name)</code>,
   * and returns the connectionUrl, unmodified.
   */
  @Override
  public String initializeDriver(String driverClassName, String connectionUrl, Properties props) throws ClassNotFoundException {
    Class.forName(driverClassName);
    return connectionUrl;
  }

  /**
   * This method simply closes the connection and returns.
   */
  @Override
  public void closeConnection(Connection connection, ConnectionPoolConfig dbConfig) throws SQLException {
    connection.close();
  }

}
