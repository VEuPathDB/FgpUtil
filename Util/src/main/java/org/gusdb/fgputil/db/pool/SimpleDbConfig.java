package org.gusdb.fgputil.db.pool;

/**
 * Basic DB configuration with simple driver config, no monitoring, and a
 * user-specified connection pool of size getConnectionPoolSize().  Any methods
 * in ConnectionPoolConfig can be overridden if more precise configuration is
 * required.
 * 
 * @author rdoherty
 */
public abstract class SimpleDbConfig implements ConnectionPoolConfig {

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
    public short getMaxWait() {
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
