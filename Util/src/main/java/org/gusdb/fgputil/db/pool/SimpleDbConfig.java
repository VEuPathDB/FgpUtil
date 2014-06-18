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
