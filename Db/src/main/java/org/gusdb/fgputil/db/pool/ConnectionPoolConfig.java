package org.gusdb.fgputil.db.pool;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.json.JSONObject;


public interface ConnectionPoolConfig {

  String getLogin();
  String getPassword();
  String getConnectionUrl();
  SupportedPlatform getPlatformEnum();
  String getDriverInitClass();

  boolean getDefaultAutoCommit();
  boolean getDefaultReadOnly();
  int getDefaultFetchSize();
  
  int getMaxActive();
  int getMaxIdle();
  int getMinIdle();
  long getMaxWait();

  boolean isShowConnections();
  long getShowConnectionsInterval();
  long getShowConnectionsDuration();

  default JSONObject toJson() {
    return new JSONObject()
      .put("login", getLogin())
      .put("connectionUrl", getConnectionUrl())
      .put("platform", getPlatformEnum())
      .put("driverInitClass", getDriverInitClass())
      .put("defaultAutoCommit", getDefaultAutoCommit())
      .put("defaultReadOnly", getDefaultReadOnly())
      .put("defaultFetchSize", getDefaultFetchSize())
      .put("maxActive", getMaxActive())
      .put("maxIdle", getMaxIdle())
      .put("minIdle", getMinIdle())
      .put("maxWait", getMaxWait())
      .put("showConnections", isShowConnections())
      .put("showConnectionsInterval", getShowConnectionsInterval())
      .put("showConnectionsDuration", getShowConnectionsDuration());
  }
}
