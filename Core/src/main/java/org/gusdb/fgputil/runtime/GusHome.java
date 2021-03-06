package org.gusdb.fgputil.runtime;

/**
 * Static class which manages the value of GUS_HOME throughout the application. The value is retrieved one of
 * three ways:
 * 
 * 1. Via Java system property (set by passing the -DGUS_HOME=your_path option to the JVM) 2. If the system
 * property is not set, the GUS_HOME environment variable will be used 3. The {@link #overrideWith(String)}
 * method is called with a ServletContext containing a GUS_HOME initialization parameter. This call will
 * override either of the two values above.
 * 
 * If neither the system property nor the environment variable is set, and webInit() is not called, GUS_HOME
 * will be null.
 * 
 * @author rdoherty
 */
public class GusHome {

  private static final String GUS_HOME_SYSTEM_PROPERTY = "GUS_HOME";
  private static final String GUS_HOME_ENV_VARIABLE = "GUS_HOME";

  private static String GUS_HOME;

  static {
    GUS_HOME = System.getProperty(GUS_HOME_SYSTEM_PROPERTY);
    if (GUS_HOME == null) {
      GUS_HOME = System.getenv(GUS_HOME_ENV_VARIABLE);
    }
  }

  private GusHome() {}

  public static String getGusHome() {
    return GUS_HOME;
  }

  public static String overrideWith(String gusHome) {
    GUS_HOME = gusHome;
    return GUS_HOME;
  }
}
