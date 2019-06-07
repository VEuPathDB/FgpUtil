package org.gusdb.fgputil.db.platform;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.gusdb.fgputil.db.pool.DefaultDbDriverInitializer;

public enum SupportedPlatform {
  ORACLE(Oracle.class),
  POSTGRESQL(PostgreSQL.class);
  
  private Class<? extends DBPlatform> _platformClass;
  
  private SupportedPlatform(Class<? extends DBPlatform> platformClass) {
    _platformClass = platformClass;
  }
  
  public DBPlatform getPlatformInstance() {
    try {
      return _platformClass.getDeclaredConstructor().newInstance();
    }
    catch (IllegalAccessException | InstantiationException | IllegalArgumentException |
        InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new UnsupportedPlatformException("Unable to instantiate platform class " + _platformClass.getName(), e);
    }
  }
  
  public static SupportedPlatform toPlatform(String platformStr) {
    try {
      return valueOf(platformStr.toUpperCase());
    }
    catch (IllegalArgumentException e) {
      throw UnsupportedPlatformException.createFromBadPlatform(platformStr);
    }
  }

  public static String getSupportedPlatformsString() {
    List<SupportedPlatform> pList = Arrays.asList(values());
    StringBuilder sb = new StringBuilder(pList.get(0).name());
    for (int i = 1; i < pList.size(); i++) {
      sb.append(", ").append(pList.get(i).name());
    }
    return sb.toString();
  }
  
  public void register() throws ClassNotFoundException {
    new DefaultDbDriverInitializer().initializeDriver(
        getPlatformInstance().getDriverClassName(), "", null);
  }
}
