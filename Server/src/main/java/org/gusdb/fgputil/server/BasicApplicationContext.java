package org.gusdb.fgputil.server;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.web.ApplicationContext;

public abstract class BasicApplicationContext extends HashMap<String,Object> implements ApplicationContext {

  private final Map<String,String> _params;

  public BasicApplicationContext() {
    _params = new HashMap<>();
  }

  public void setInitParameter(String key, String value) {
    _params.put(key, value);
  }

  @Override
  public String getInitParameter(String key) {
    return _params.get(key);
  }

  @Override
  public String getRealPath(String path) {
    return path;
  }

}
