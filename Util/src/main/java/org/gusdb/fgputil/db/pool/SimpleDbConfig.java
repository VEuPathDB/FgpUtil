package org.gusdb.fgputil.db.pool;

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
     * Creates a simple database configuration for a connection pool of the desired size.
     * 
     * @param dbType platform of the DB to connect to
     * @param connectionUrl connection URL
     * @param username login name
     * @param password login password
     * @param connectionPoolSize size of connection pool
     * @return configuration to create a database instance
     */
    public static SimpleDbConfig create(final SupportedPlatform dbType, final String connectionUrl,
        final String username, final String password, final short connectionPoolSize) {
      return new SimpleDbConfig() {
        @Override public SupportedPlatform getPlatformEnum() { return dbType; }
        @Override public short getConnectionPoolSize()       { return connectionPoolSize; }
        @Override public String getLogin()                   { return username; }
        @Override public String getPassword()                { return password; }
        @Override public String getConnectionUrl()           { return connectionUrl; }
      };
    }
  
    public abstract short getConnectionPoolSize();
    
    @Override
    public String getDriverInitClass() {
        return DefaultDbDriverInitializer.class.getName();
    }

    @Override
    public short getMaxActive() {
        return getConnectionPoolSize();
    }

    @Override
    public short getMaxIdle() {
        return getConnectionPoolSize();
    }

    @Override
    public short getMinIdle() {
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

}
