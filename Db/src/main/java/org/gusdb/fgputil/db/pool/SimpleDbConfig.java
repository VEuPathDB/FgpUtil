package org.gusdb.fgputil.db.pool;

import static org.gusdb.fgputil.FormatUtil.NL;

import org.gusdb.fgputil.db.platform.SupportedPlatform;

/**
 * Basic DB configuration with simple driver config, no monitoring, and a
 * user-specified connection pool of size getConnectionPoolSize().  Any methods
 * in ConnectionPoolConfig can be overridden if more precise configuration is
 * required.
 * 
 * @author rdoherty
 */
public abstract class SimpleDbConfig implements ConnectionPoolConfig {

    /**
     * Creates a simple database configuration with connection pool of size 1.  This is for
     * single-threaded applications that simply want access to a database.
     * 
     * @param dbType platform of the DB to connect to
     * @param connectionUrl connection URL
     * @param username login name
     * @param password login password
     * @return configuration to create a database instance
     */
    public static SimpleDbConfig create(final SupportedPlatform dbType, final String connectionUrl,
        final String username, final String password) {
      return create(dbType, connectionUrl, username, password, (short)1);
    }

    /**
     * Creates a simple database configuration for a connection pool of the
     * desired size and the standard default fetch size (JDBC driver will decide).
     * 
     * @param dbType platform of the DB to connect to
     * @param connectionUrl connection URL
     * @param username login name
     * @param password login password
     * @param connectionPoolSize size of connection pool
     * @return configuration to create a database instance
     */
    public static SimpleDbConfig create(final SupportedPlatform dbType, final String connectionUrl,
        final String username, final String password, final int connectionPoolSize) {
      return create(dbType, connectionUrl, username, password, connectionPoolSize, 0);
    }

    /**
     * Creates a simple database configuration for a connection pool of the
     * desired size with a desired default fetch size.
     * 
     * @param dbType platform of the DB to connect to
     * @param connectionUrl connection URL
     * @param username login name
     * @param password login password
     * @param connectionPoolSize size of connection pool
     * @param defaultFetchSize fetch size "hint" to apply to statements when created
     * @return configuration to create a database instance
     */
    public static SimpleDbConfig create(final SupportedPlatform dbType, final String connectionUrl,
        final String username, final String password, final int connectionPoolSize, final int defaultFetchSize) {
      return new SimpleDbConfig() {
        @Override public SupportedPlatform getPlatformEnum() { return dbType; }
        @Override public int getConnectionPoolSize()         { return connectionPoolSize; }
        @Override public String getLogin()                   { return username; }
        @Override public String getPassword()                { return password; }
        @Override public String getConnectionUrl()           { return connectionUrl; }
        @Override public int getDefaultFetchSize()           { return defaultFetchSize; }
      };
    }
  
    public abstract int getConnectionPoolSize();
    
    @Override
    public String getDriverInitClass() {
        return DefaultDbDriverInitializer.class.getName();
    }

    @Override
    public int getMaxActive() {
        return getConnectionPoolSize();
    }

    @Override
    public int getMaxIdle() {
        return getConnectionPoolSize();
    }

    @Override
    public int getMinIdle() {
        return 1;
    }

    @Override
    public long getMaxWait() {
        return 1000;
    }

    @Override
    public boolean isShowConnections() {
        return false;
    }

    @Override
    public long getShowConnectionsInterval() {
        return 0;
    }

    @Override
    public long getShowConnectionsDuration() {
        return 0;
    }

    @Override
    public boolean getDefaultAutoCommit() {
      return true;
    }

    @Override
    public boolean getDefaultReadOnly() {
      return false;
    }
    
    @Override
    public String toString() {
      return toString(false);
    }
    
    public String toString(boolean showPassword) {
      return new StringBuilder(super.toString())
          .append(" {").append(NL)
          .append("  Platform:    ").append(getPlatformEnum()).append(NL)
          .append("  Connection:  ").append(getConnectionUrl()).append(NL)
          .append("  Login:       ").append(getLogin()).append(NL)
          .append("  Password:    ").append(showPassword ? getPassword() : "<hidden>").append(NL)
          .append("  MaxPoolSize: ").append(getConnectionPoolSize()).append(NL)
          .append("}").append(NL)
          .toString();
    }
}
