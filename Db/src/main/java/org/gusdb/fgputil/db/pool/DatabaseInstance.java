package org.gusdb.fgputil.db.pool;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.wrapper.DataSourceWrapper;

public class DatabaseInstance implements Wrapper, AutoCloseable {

  private static final Logger LOG = Logger.getLogger(DatabaseInstance.class);

  private static final Map<String, WeakReference<DatabaseInstance>> INITIALIZED_INSTANCES = new LinkedHashMap<>();

  private static final String ANONYMOUS_DB_ID_PREFIX = "UNNAMED_DB_INSTANCE_";
  private static final AtomicInteger ANONYMOUS_DB_ID_SEQ = new AtomicInteger(1);

  private boolean _initialized = false;

  // fields initialized by constructor
  private final ConnectionPoolConfig _dbConfig;
  private final DBPlatform _platform;
  private final String _defaultSchema;

  // fields initialized by initialize()
  private String _identifier;
  private BasicDataSource _connectionPool;
  private DataSourceWrapper _dataSource;
  private ConnectionPoolLogger _logger;

  /**
   * Creates an initialized connection pool with a default identifier. The driver
   * should have been registered by the platform implementation.  This constructor
   * does not test for a valid connection upon pool creation.
   * 
   * @param dbConfig configuration for this instance
   */
  public DatabaseInstance(ConnectionPoolConfig dbConfig) {
    this(dbConfig, getNextDbName(), false);
  }

  /**
   * Creates an initialized connection pool with a default identifier. The driver
   * should have been registered by the platform implementation.  If testOnInitialize
   * is true, a connection is fetched from the pool, which causes the platform's
   * validation query to be run.
   * 
   * @param dbConfig configuration for this instance
   * @param testOnInitialize if true, a connection is fetched and tested before return
   */
  public DatabaseInstance(ConnectionPoolConfig dbConfig, boolean testOnInitialize) {
    this(dbConfig, getNextDbName(), testOnInitialize);
  }

  /**
   * Creates an initialized connection pool with the given identifier. The driver
   * should have been registered by the platform implementation.  This constructor
   * does not test for a valid connection upon pool creation.
   * 
   * @param dbConfig configuration for this instance
   * @param identifier identifier for this instance
   * @throws IllegalArgumentException if identifier is null, empty, or already taken
   */
  public DatabaseInstance(ConnectionPoolConfig dbConfig, String identifier) {
    this(dbConfig, identifier, false);
  }

  /**
   * Creates an initialized connection pool with the given identifier. The driver
   * should have been registered by the platform implementation.  If testOnInitialize
   * is true, a connection is fetched from the pool, which causes the platform's
   * validation query to be run.
   * 
   * @param dbConfig configuration for this instance
   * @param identifier identifier for this instance
   * @param testOnInitialize if true, a connection is fetched and tested before return
   * @throws IllegalArgumentException if identifier is null, empty, or already taken
   */
  public DatabaseInstance(ConnectionPoolConfig dbConfig, String identifier, boolean testOnInitialize) {
    _dbConfig = dbConfig;
    _platform = _dbConfig.getPlatformEnum().getPlatformInstance();
    _defaultSchema = _platform.getDefaultSchema(_dbConfig.getLogin());
    reinitialize(identifier);
    if (testOnInitialize) runValidationQuery();
  }

  // since testOnBorrow and testOnReturn are set to true, need only fetch a connection to test
  private void runValidationQuery() {
    LOG.info("Testing connection to " + getIdentifier() + "...");
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      conn = _dataSource.getConnection();
      // See note on commented method below
      //logOracleConnecionProperies(conn);
      stmt = conn.createStatement();
      rs = stmt.executeQuery(_platform.getValidationQuery());
      LOG.info("DB Instance " + getIdentifier() + " validation query successfully executed.");
    }
    catch (Throwable e) {
      // validation failed; shut down pool and rethrow as runtime exception
      LOG.error("Validation query failed during DB instance initialization.  DB instance will be shut down.", e);
      try {
        close();
      }
      catch (Exception e2) {
        LOG.error("Unable to shut down connection pool resources after failing validation query.", e2);
      }
      throw new InitializationException("Trial fetch of connection with validation query execution failed", e);
    }
    finally {
      SqlUtils.closeQuietly(rs, stmt, conn);
    }
  }

  /**
   * This method was used to log Oracle connection properties to help evaluate
   * memory performance. If uncommented it will compile with the addition of the
   * Oracle driver to the fgputil-db pom, e.g.
   *  <dependency>
   *    <groupId>com.oracle</groupId>
   *    <artifactId>ojdbc8</artifactId>
   *    <version>12.2.0.1</version>
   *  </dependency>
   *
   * @param conn connection whose properties should be logged
   *
  private void logOracleConnecionProperies(Connection conn) {
    try {
      ConnectionWrapper connWrapper = (ConnectionWrapper)conn;
      DelegatingConnection<?> poolDelegate = (DelegatingConnection<?>)connWrapper.getUnderlyingConnection();
      PoolableConnection poolConn = (PoolableConnection)poolDelegate.getDelegate();
      OracleConnection oraConn = (OracleConnection)poolConn.getDelegate();
      LOG.info("Oracle Connection Properties: " + new JSONObject()
          .put("directMethods", new JSONObject()
              .put("defaultTimeZone", oraConn.getDefaultTimeZone())
              .put("implicitCachingEnabled", oraConn.getImplicitCachingEnabled())
              .put("explicitCachingEnabled", oraConn.getExplicitCachingEnabled())
              .put("statementCacheSize", oraConn.getStatementCacheSize())
              .put("needToPurgeStatementCache", oraConn.needToPurgeStatementCache()))
          .put("clientInfo", toJson(oraConn.getClientInfo()))
          //.put("connectionAttributes", toJson(oraConn.getConnectionAttributes()))
          .put("properties", toJson(oraConn.getProperties()))
          .toString(2));
    }
    catch(Exception e) {
      // log but ignore
      LOG.error("Could not read Oracle properties", e);
    }
  }

  private static JSONObject toJson(Properties properties) {
    JSONObject json = new JSONObject();
    for (Entry<Object,Object> entry : properties.entrySet()) {
      json.put((String)entry.getKey(), (String)entry.getValue());
    }
    return json;
  }
  */

  private static String getNextDbName() {
    return ANONYMOUS_DB_ID_PREFIX + ANONYMOUS_DB_ID_SEQ.getAndIncrement();
  }

  /**
   * Reinitializes the connection pool, data source, and (if configured) logger
   * for this instance.  Also registers the instance under the passed name so
   * it will appear in the map returned by getAllInstances()
   * 
   * @param identifier identifier to register this instance under
   * @return this instance
   * @throws IllegalArgumentException if name is invalid or already taken
   */
  public DatabaseInstance reinitialize(String identifier) {
    synchronized(this) {
      if (_initialized) {
        LOG.warn("Multiple calls to initialize().  Ignoring...");
        return this;
      }
      else {
        addInstance(this, identifier);
        _identifier = identifier;

        try {
          LOG.info("DB Connection Pool [" + _identifier + "]: " +
              _dbConfig.getLogin() + "@" + _dbConfig.getConnectionUrl());

          _connectionPool = createConnectionPool(_dbConfig, _platform);

          _dataSource = new DataSourceWrapper(_identifier, _connectionPool, _dbConfig);

          // start the connection monitor if needed
          if (_dbConfig.isShowConnections()) {
            LOG.info("Starting Connection Pool Logger for instance; " + _identifier);
            _logger = new ConnectionPoolLogger(this);
            new Thread(_logger).start();
          }

          _initialized = true;
          return this;
        }
        catch (Exception e) {
          removeInstance(this);
          _identifier = null;
          throw e;
        }
      }
    }
  }

  private static BasicDataSource createConnectionPool(ConnectionPoolConfig dbConfig, DBPlatform platform) {

    // initialize DB driver; (possibly modified) url will be returned, connection properties may also be modified
    Properties props = new Properties();
    String connectionUrl = initializeDbDriver(platform.getDriverClassName(), dbConfig.getDriverInitClass(), props, dbConfig.getConnectionUrl());

    // create connection pool and set basic properties
    BasicDataSource connectionPool = new BasicDataSource();
    connectionPool.setUrl(connectionUrl);
    connectionPool.setUsername(dbConfig.getLogin());
    connectionPool.setPassword(dbConfig.getPassword());
    connectionPool.setConnectionProperties(getPropertyString(props));

    // configure how connections are created
    connectionPool.setDefaultReadOnly(dbConfig.getDefaultReadOnly());
    connectionPool.setDefaultAutoCommit(dbConfig.getDefaultAutoCommit());

    // configure the connection pool
    connectionPool.setMaxWait(Duration.ofMillis(dbConfig.getMaxWait()));
    connectionPool.setMaxIdle(dbConfig.getMaxIdle());
    connectionPool.setMinIdle(dbConfig.getMinIdle());
    connectionPool.setMaxTotal(dbConfig.getMaxActive());

    // configure validationQuery tests
    connectionPool.setValidationQuery(platform.getValidationQuery());
    connectionPool.setTestOnBorrow(true);
    connectionPool.setTestOnReturn(true);
    connectionPool.setAccessToUnderlyingConnectionAllowed(true);

    return connectionPool;
  }

  private static String getPropertyString(Properties props) {
    StringBuilder str = new StringBuilder();
    for (Entry<Object,Object> prop : props.entrySet()) {
      str.append(prop.getKey()).append("=").append(prop.getValue()).append(";");
    }
    return str.toString();
  }

  private static String initializeDbDriver(String driverClassName, String driverInitClassName,
      Properties props, String connectionUrl) {
    try {
      DbDriverInitializer initClassInstance = DbDriverInitializer.getInstance(driverInitClassName);
      LOG.debug("Initializing driver " + driverClassName + " using initializer: " + initClassInstance.getClass().getName());
      return initClassInstance.initializeDriver(driverClassName, connectionUrl, props);
    }
    catch (ClassNotFoundException e) {
      throw new InitializationException("Unable to instantiate configured DB driver class '" + driverInitClassName + "'.", e);
    }
  }

  private static synchronized void addInstance(DatabaseInstance databaseInstance, String name) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException(
          "Cannot instantiate Database Instance with null or empty name");
    }
    if (INITIALIZED_INSTANCES.containsKey(name)) {
      throw new IllegalArgumentException(
          "Cannot instantiate Database Instance with name " + name + ".  Name already taken.");
    }
    INITIALIZED_INSTANCES.put(name, new WeakReference<DatabaseInstance>(databaseInstance));
  }

  private static synchronized void removeInstance(DatabaseInstance dbInstance) {
    INITIALIZED_INSTANCES.remove(dbInstance._identifier);
  }

  public static synchronized Map<String, DatabaseInstance> getAllInstances() {
    Map<String, DatabaseInstance> instanceMap = new LinkedHashMap<>();
    for (WeakReference<DatabaseInstance> ref : INITIALIZED_INSTANCES.values()) {
      DatabaseInstance db = ref.get();
      instanceMap.put(db.getIdentifier(), db);
    }
    return instanceMap;
  }

  /**
   * If this DB is initialized, shuts down the connection pool, and (if
   * configured) the connection pool logger thread.  Resets initialized flag,
   * so this DB can be reinitialized if desired.
   * 
   * @throws Exception if problem while shutting down DB instance
   */
  @Override
  public void close() throws Exception {
    synchronized(this) {
      removeInstance(this);
      if (_initialized) {
        if (_dbConfig.isShowConnections()) {
          _logger.shutDown();
        }
        _connectionPool.close();
        _initialized = false;
        LOG.info("DB Connection Pool CLOSED [" + _identifier + "]: " +
            _dbConfig.getLogin() + "@" + _dbConfig.getConnectionUrl());
      }
    }
  }

  @Override
  public void finalize() {
    if (_initialized) {
      try {
        // try to close this resource if not closed already
        close();
      }
      catch (Exception e) {
        LOG.warn("Unable to shut down DatabaseInstance in finalize", e);
      }
    }
  }

  public ConnectionPoolConfig getConfig() {
    // do not need to checkInit() since this is set in constructor
    return _dbConfig;
  }

  public DBPlatform getPlatform() {
    // do not need to checkInit() since this is set in constructor
    return _platform;
  }

  public String getDefaultSchema() {
    // do not need to checkInit() since this is set in constructor
    return _defaultSchema;
  }

  /**
   * Checks whether this instance has been initialized.
   * 
   * @throws IllegalStateException if not initialized
   */
  private void checkInit() {
    if (!_initialized) {
      throw new IllegalStateException("Instance must be initialized with " +
          "initialize(name) before this method is called.");
    }
  }

  public String getIdentifier() {
    checkInit();
    return _identifier;
  }

  public DataSource getDataSource() {
    checkInit();
    return _dataSource;
  }

  public String getUnclosedConnectionInfo() {
    checkInit();
    return _dataSource.dumpUnclosedObjectInfo();
  }

  public int getNumConnectionsOpened() {
    checkInit();
    return _dataSource.getNumConnectionsOpened();
  }

  public int getNumConnectionsClosed() {
    checkInit();
    return _dataSource.getNumConnectionsClosed();
  }

  public int getConnectionsCurrentlyOpen() {
    checkInit();
    return _dataSource.getConnectionsCurrentlyOpen();
  }

  /**
   * @return the number of instances currently borrowed from this pool.
   */
  public int getActiveCount() {
    checkInit();
    return _connectionPool.getNumActive();
  }

  /**
   * @return the number of instances currently idle in this pool
   */
  public int getIdleCount() {
    checkInit();
    return _connectionPool.getNumIdle();
  }

  /**
   * @return the minimum number of objects allowed in the pool before the 
   * evictor thread (if active) spawns new objects
   */
  public int getMinIdle() {
    checkInit();
    return _connectionPool.getMinIdle();
  }

  /**
   * @return the cap on the number of "idle" instances in the pool.
   */
  public int getMaxIdle() {
    checkInit();
    return _connectionPool.getMaxIdle();
  }

  /**
   * @return the minimum amount of time an object may sit idle in the pool 
   * before it is eligible for eviction by the idle object evictor (if any).
   */
  public long getMinEvictableIdleTimeMillis() {
    checkInit();
    return _connectionPool.getMinEvictableIdleDuration().toMillis();
  }

  /**
   * @return the number of milliseconds to sleep between runs of the idle object evictor thread.
   */
  public long getTimeBetweenEvictionRunsMillis() {
    checkInit();
    return _connectionPool.getDurationBetweenEvictionRuns().toMillis();
  }

  /**
   * When true, objects will be validated before being returned by the borrowObject() method.
   * 
   * @return true if objects will be validated before being returned by the #borrowObject() method, else false
   */
  public boolean getTestOnBorrow() {
    checkInit();
    return _connectionPool.getTestOnBorrow();
  }

  /**
   * When true, objects will be validated before being returned to the pool within the returnObject(T).
   * 
   * @return true if objects will be validated before being returned by the #returnObject(T) method, else false
   */
  public boolean getTestOnReturn() {
    checkInit();
    return _connectionPool.getTestOnReturn();
  }

  /**
   * When true, objects will be validated by the idle object evictor (if any).
   * 
   * @return true if objects will be validated by the idle object evictor, else false
   */
  public boolean getTestWhileIdle() {
    return _connectionPool.getTestWhileIdle();
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
