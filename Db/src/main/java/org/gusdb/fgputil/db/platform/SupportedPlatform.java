package org.gusdb.fgputil.db.platform;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.gusdb.fgputil.db.pool.DefaultDbDriverInitializer;
import org.veupathdb.lib.ldap.Platform;

public enum SupportedPlatform {
  ORACLE(Oracle.CONNECTION_URL_PREFIX, Oracle.class, Platform.ORACLE, 1521),
  POSTGRESQL(PostgreSQL.CONNECTION_URL_SCHEME, PostgreSQL.class, Platform.POSTGRES, 5432);

  private final String _connectionUrlPrefix;
  private Class<? extends DBPlatform> _platformClass;
  private Platform _ldapPlatform;
  private int _defaultPort;

  private SupportedPlatform(String connectionUrlPrefix, Class<? extends DBPlatform> platformClass, Platform ldapPlatform, int defaultPort) {
    _connectionUrlPrefix = connectionUrlPrefix;
    _platformClass = platformClass;
    _ldapPlatform = ldapPlatform;
    _defaultPort = defaultPort;
  }

  public int getDefaultPort() {
    return _defaultPort;
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

  public static SupportedPlatform fromConnectionUrl(String connectionUrl) {
    for (SupportedPlatform platform : values()) {
      if (connectionUrl.startsWith(platform._connectionUrlPrefix)) {
        return platform;
      }
    }
    throw UnsupportedPlatformException.createFromBadPlatform("Unsupported connection URL: " + connectionUrl);
  }

  public static SupportedPlatform fromLdapPlatform(Platform ldapPlatform) {
    for (SupportedPlatform platform : values()) {
      if (ldapPlatform == platform._ldapPlatform) return platform;
    }
    throw UnsupportedPlatformException.createFromBadPlatform("Unsupported LDAP Platform enum: " + ldapPlatform);
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
