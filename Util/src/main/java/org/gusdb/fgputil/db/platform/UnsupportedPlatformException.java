package org.gusdb.fgputil.db.platform;

public class UnsupportedPlatformException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public static UnsupportedPlatformException createFromBadPlatform(String attemptedPlatformName) {
    String message =  "'" + attemptedPlatformName + "' is not a supported database platform. " +
    		"Supported platforms are: " + SupportedPlatform.getSupportedPlatformsString();
    return new UnsupportedPlatformException(message);
  }

  public UnsupportedPlatformException(String message) {
    super(message);
  }
  
  public UnsupportedPlatformException(String message, Exception cause) {
    super(message, cause);
  }
}
