package org.gusdb.fgputil.db.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provides interface so users can specify a custom database driver initializer.
 * 
 * @author rdoherty
 */
public interface DbDriverInitializer {

  /**
   * This method should initialize the driver named by <code>driverClassName</code>.
   * It is to be connected to by the <code>connectionUrl</code> using the properties
   * specified in <code>props</code>.  This method may modify the properties as
   * needed by a proxy driver or set of drivers, and can also modify the connection
   * URL that will be used to connect by returning a new URL.
   * 
   * @param driverClassName class name of driver to be initialized
   * @param connectionUrl URL that will be used to connect to this database
   * @param props initial connection properties
   * @return modified URL to be used to connect instead of the passed URL
   * @throws ClassNotFoundException if driver class specified cannot be found
   */
  public String initializeDriver(String driverClassName, String connectionUrl, Properties props) throws ClassNotFoundException;

  /**
   * This method should perform any pre-close maintenance of a connection and then
   * close the connection.  Exception handling can also be customized to catch and
   * ignore expected problems but throw unexpected ones.
   * 
   * @param connection connection to close
   * @param dbConfig configuration of the database instance that generated this connection
   */
  public void closeConnection(Connection connection, ConnectionPoolConfig dbConfig) throws SQLException;

  /**
   * Check that passed class name is a subclass of DbDriverInitializer, then create
   * an instance and return it.
   * 
   * @param driverInitClassName name of class to instantiate
   * @return new instance of the passed class
   */
  @SuppressWarnings("unchecked") // class name type is checked below
  public static DbDriverInitializer getInstance(String driverInitClassName) {
    try {
      // check to see if user provided custom driver initializer
      if (driverInitClassName == null || driverInitClassName.isEmpty() ||
          driverInitClassName.equals(DefaultDbDriverInitializer.class.getName())) {
        // if none provided (or default), use the default driver initializer
        DbDriverInitializer initClassInstance = new DefaultDbDriverInitializer();
        return initClassInstance;
      }
      else {
        // otherwise, try to instantiate user-provided implementation and call
        Class<?> initClass = Class.forName(driverInitClassName);
        if (!DbDriverInitializer.class.isAssignableFrom(initClass)) {
          throw new InitializationException("Submitted DB Driver Initializer ( " + driverInitClassName + ") " +
              "is not an implementation of " + DbDriverInitializer.class.getName());
        }
        // provided class is the correct type; instantiate and call initialize method
        return ((Class<? extends DbDriverInitializer>)initClass).newInstance();
      }
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new InitializationException("Unable to instantiate custom DB Driver Initializer " +
          "class with name " + driverInitClassName, e);
    }
  }
}
