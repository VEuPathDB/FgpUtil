package org.gusdb.fgputil.db.pool;

import org.gusdb.fgputil.db.platform.SupportedPlatform;


public interface ConnectionPoolConfig {

  public String getLogin();
  public String getPassword();
  public String getConnectionUrl();
  public SupportedPlatform getPlatformEnum();
  public String getDriverInitClass();
  
  public short getMaxActive();
  public short getMaxIdle();
  public short getMinIdle();
  public short getMaxWait();
  
  public boolean isShowConnections();
  public long getShowConnectionsInterval();
  public long getShowConnectionsDuration();

}
