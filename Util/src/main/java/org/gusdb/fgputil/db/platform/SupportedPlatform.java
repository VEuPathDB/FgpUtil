package org.gusdb.fgputil.db.platform;

import java.util.Arrays;
import java.util.List;

public enum SupportedPlatform {
  ORACLE(Oracle.class),
  POSTGRES(PostgreSQL.class);
  
  private Class<? extends DBPlatform> _platformClass;
  
  private SupportedPlatform(Class<? extends DBPlatform> platformClass) {
    _platformClass = platformClass;
  }
  
  public DBPlatform getPlatformInstance() {
    try {
      return _platformClass.newInstance();
    }
    catch (IllegalAccessException | InstantiationException e) {
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
}
