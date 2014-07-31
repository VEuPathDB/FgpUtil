package org.gusdb.fgputil.db.pool;

import java.sql.SQLException;
import java.sql.Wrapper;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.WrappedDataSource;
import org.gusdb.fgputil.db.platform.DBPlatform;

public class DatabaseInstance implements Wrapper {
  
  private static final Logger LOG = Logger.getLogger(DatabaseInstance.class);
  
  private final String _name;
  private final ConnectionPoolConfig _dbConfig;
  private boolean _initialized = false;
  private DBPlatform _platform;
  private GenericObjectPool _connectionPool;
  private WrappedDataSource _dataSource;
  private String _defaultSchema;
  private ConnectionPoolLogger _logger;

  /**
   * Initialize connection pool. The driver should have been registered by the
   * platform implementation.
   */
  public DatabaseInstance(String name, ConnectionPoolConfig dbConfig) {
      _name = name;
      _dbConfig = dbConfig;
      _platform = _dbConfig.getPlatformEnum().getPlatformInstance();
      _defaultSchema = _platform.getDefaultSchema(_dbConfig.getLogin());
  }
  
  public void initialize() {
    synchronized(this) {
      if (_initialized) {
        LOG.warn("Multiple calls to initialize().  Ignoring...");
        return;
      }
      else {
        LOG.info("DB Connection [" + _name + "]: " + _dbConfig.getConnectionUrl());
        
        _connectionPool = createConnectionPool(_dbConfig, _platform);
  
        // configure the connection pool
        _connectionPool.setMaxWait(_dbConfig.getMaxWait());
        _connectionPool.setMaxIdle(_dbConfig.getMaxIdle());
        _connectionPool.setMinIdle(_dbConfig.getMinIdle());
        _connectionPool.setMaxActive(_dbConfig.getMaxActive());
  
        // configure validationQuery tests
        _connectionPool.setTestOnBorrow(true);
        _connectionPool.setTestOnReturn(true);
        _connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
  
        PoolingDataSource dataSource = new PoolingDataSource(_connectionPool);
        dataSource.setAccessToUnderlyingConnectionAllowed(true);
        _dataSource = new WrappedDataSource(_name, dataSource);
  
        // start the connection monitor if needed
        if (_dbConfig.isShowConnections()) {
          LOG.info("Starting Connection Pool Logger for instance; " + _name);
          _logger = new ConnectionPoolLogger(this);
          new Thread(_logger).start();
        }
        
        _initialized = true;
      }
    }
  }
  
  private static GenericObjectPool createConnectionPool(ConnectionPoolConfig dbConfig, DBPlatform platform) {
    
    GenericObjectPool connectionPool = new GenericObjectPool(null);

    Properties props = new Properties();
    props.put("user", dbConfig.getLogin());
    props.put("password", dbConfig.getPassword());

    // initialize DB driver; (possibly modified) url will be returned, connection properties may also be modified
    String connectionUrl = initializeDbDriver(platform.getDriverClassName(), dbConfig.getDriverInitClass(), props, dbConfig.getConnectionUrl());

    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionUrl, props);

    // link connection factory to connection pool with assigned settings
    boolean defaultReadOnly = false;
    boolean defaultAutoCommit = true;
      
    // object is created only to link factory and pool
    new PoolableConnectionFactory(connectionFactory, connectionPool, null,
        platform.getValidationQuery(), defaultReadOnly, defaultAutoCommit);
    
    return connectionPool;
  }

  private static String initializeDbDriver(String driverClassName, String driverInitClassName,
      Properties props, String connectionUrl) {
    try {
      // check to see if user provided custom driver initializer
      if (driverInitClassName == null || driverInitClassName.isEmpty() ||
          driverInitClassName.equals(DefaultDbDriverInitializer.class.getName())) {
        // if none provided (or default), use the default driver initializer
        DbDriverInitializer initClassInstance = new DefaultDbDriverInitializer();
        LOG.debug("Initializing driver " + driverClassName + " using default initializer.");
        return initClassInstance.initializeDriver(driverClassName, connectionUrl, props);
      }
      else {
        // otherwise, try to instantiate user-provided implementation and call
        Class<?> initClass = Class.forName(driverInitClassName);
        if (!DbDriverInitializer.class.isAssignableFrom(initClass)) {
          throw new DbDriverInitException("Submitted DB Driver Initializer ( " + driverInitClassName + ") " +
              "is not an implementation of " + DbDriverInitializer.class.getName());
        }
        // provided class is the correct type; instantiate and call initialize method
        @SuppressWarnings("unchecked") // checked above
        DbDriverInitializer initClassInstance = ((Class<? extends DbDriverInitializer>)initClass).newInstance();
        LOG.debug("Initializing driver " + driverClassName + " using custom initializer: " + driverInitClassName);
        return initClassInstance.initializeDriver(driverClassName, connectionUrl, props);
      }
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new DbDriverInitException("Unable to instantiate custom DB Driver Initializer " +
          "class with name " + driverInitClassName, e);
    }
  }

  private void checkInit() {
    if (!_initialized) {
      throw new IllegalStateException("Instance must be initialized with " +
          "initialize() before this method is called.");
    }
  }
  
  public String getUnclosedConnectionInfo() {
    return _dataSource.dumpUnclosedConnectionInfo();
  }
  
  /**
   * If this DB is initialized, shuts down the connection pool, and (if
   * configured) the connection pool logger thread.  Resets initialized flag,
   * so this DB can be reinitialized if desired.
   * 
   * @throws Exception if error while closing connection pool
   */
  public void close() throws Exception {
    synchronized(this) {
      if (_initialized) {
        if (_dbConfig.isShowConnections()) {
          _logger.shutDown();
        }
        _connectionPool.close();
      }
      _initialized = false;
    }
  }

  public ConnectionPoolConfig getConfig() {
    return _dbConfig;
  }

  public String getName() {
    return _name;
  }

  public String getDefaultSchema() {
    return _defaultSchema;
  }

  public DBPlatform getPlatform() {
    return _platform;
  }
  
  public int getActiveCount() {
    checkInit();
    return _connectionPool.getNumActive();
  }

  public int getIdleCount() {
    checkInit();
    return _connectionPool.getNumIdle();
  }
  
  public DataSource getDataSource() {
    checkInit();
    return _dataSource;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }
}
