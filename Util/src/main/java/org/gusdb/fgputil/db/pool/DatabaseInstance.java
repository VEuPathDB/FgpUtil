package org.gusdb.fgputil.db.pool;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.platform.DBPlatform;

public class DatabaseInstance {
  
  private static final Logger LOG = Logger.getLogger(DatabaseInstance.class);
  
  private final String _name;
  private final ConnectionPoolConfig _dbConfig;
  private boolean _initialized = false;
  private DBPlatform _platform;
  private GenericObjectPool _connectionPool;
  private DataSource _dataSource;
  private String _defaultSchema;
  private ConnectionPoolLogger _logger;

  /**
   * Initialize connection pool. The driver should have been registered by the
   * platform implementation.
   */
  public DatabaseInstance(String name, ConnectionPoolConfig dbConfig) {
      _name = name;
      _dbConfig = dbConfig;
      _platform = _dbConfig.getPlatform().getPlatformInstance();
      _defaultSchema = DBPlatform.normalizeSchema(_dbConfig.getLogin());
  }
  
  public void initialize() {
    synchronized(this) {
      if (_initialized) {
        LOG.warn("Multiple calls to initialize().  Ignoring...");
        return;
      }
      else {
        LOG.info("DB Connection [" + _name + "]: " + _dbConfig.getConnectionUrl());
        _connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
            _dbConfig.getConnectionUrl(), _dbConfig.getLogin(), _dbConfig.getPassword());
  
        // create abandoned configuration
        boolean defaultReadOnly = false;
        boolean defaultAutoCommit = true;
        new PoolableConnectionFactory(connectionFactory, _connectionPool, null,
                _platform.getValidationQuery(), defaultReadOnly, defaultAutoCommit);
  
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
        _dataSource = dataSource;
  
        // start the connection monitor if needed
        if (_dbConfig.isShowConnections()) {
          _logger = new ConnectionPoolLogger(this);
          new Thread(_logger).start();
        }
        
        _initialized = true;
      }
    }
  }
  
  private void checkInit() {
    if (!_initialized) {
      throw new IllegalStateException("Instance must be initialized with " +
      		"initialize() before this method is called.");
    }
  }
  
  /**
   * If this DB is initialized, shuts down the connection pool, and (if
   * configured) the connection pool logger thread.  Resets initialized flag,
   * so this DB can be reinitialized if desired.
   * 
   * @throws Exception
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
}
