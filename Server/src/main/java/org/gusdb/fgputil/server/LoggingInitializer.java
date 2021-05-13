package org.gusdb.fgputil.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.json.JsonConfiguration;

public class LoggingInitializer {

  public static final String LOG4J2_GUS_LOG_LEVEL = "LOG4J2_GUS_LOG_LEVEL";
  public static final String LOG4J2_ROOT_LOG_LEVEL = "LOG4J2_ROOT_LOG_LEVEL";

  /**
   * Routes all logging through Log4J2 and applies the configuration from
   * resources.
   *
   * This is needed to hijack the default logging done by other libraries and
   * force them through log4j.
   */
  public static void initialize() {
    try {
      // try to find an application-defined config file
      InputStream config = LoggingInitializer.class.getResourceAsStream("/log4j2.json");

      boolean usedDefaultConfig = false;
      if (config == null) {
        // if application does not define a config, use default
        config = LoggingInitializer.class.getResourceAsStream("/default/log4j2.json");
        usedDefaultConfig = true;
      }

      // skip config if still unable to find
      boolean ableToInitialize = false;
      if (config != null) {
        // initialize based on the config file
        Configurator.initialize(
            new JsonConfiguration((LoggerContext)LogManager.getContext(),
                new ConfigurationSource(config)));
        ableToInitialize = true;
      }

      // override gusdb level if env var exists
      getEnvDeclaredLevel(LOG4J2_GUS_LOG_LEVEL)
        .ifPresent(level -> Configurator.setLevel("org.gusdb", level));

      // override root level if env var exists
      getEnvDeclaredLevel(LOG4J2_ROOT_LOG_LEVEL)
        .ifPresent(level -> Configurator.setRootLevel(level));

      // tell the user what happened
      Logger log = LogManager.getLogger(LoggingInitializer.class);
      if (!ableToInitialize) {
        log.warn("Unable to find application log4j.json at classpath root or " +
            "FgpUtil version at /default/log4j2.json.  Will use Log4j2 default settings.");
      }
      else if (usedDefaultConfig) {
        log.warn("Unable to find application log4j.json at classpath root. " +
            "Used FgpUtil default config at /default/log4j2.json");
      }
      else {
        log.info("Log4j2 successfully initialized.");
      }
    }
    catch (IOException e) {
      // problems with logging config should not hose the entire service
      Logger log = LogManager.getLogger(LoggingInitializer.class);
      log.error("Could not initialize Log4j2; default configuration will be used.", e);
    }
  }

  private static Optional<Level> getEnvDeclaredLevel(String envVarName) {
    return Optional.ofNullable(System.getenv(envVarName)).flatMap(levelStr -> {
      try {
        return Optional.of(Level.valueOf(levelStr));
      }
      catch (IllegalArgumentException e) {
        System.err.println("Logging override '" + envVarName +
            "' supplied unrecognized level '" + levelStr + "' and will be ignored.");
        return Optional.empty();
      }
    });
  }

}
