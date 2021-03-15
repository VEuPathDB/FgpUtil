package org.gusdb.fgputil.server;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
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
      // initialize based on the config file
      Configurator.initialize(
        new JsonConfiguration((LoggerContext)LogManager.getContext(),
          new ConfigurationSource(
            LoggingInitializer.class.getResourceAsStream("/log4j2.json"))));

      // override gusdb level if env var exists
      getEnvDeclaredLevel(LOG4J2_GUS_LOG_LEVEL)
        .ifPresent(level -> Configurator.setLevel("org.gusdb", level));

      // override root level if env var exists
      getEnvDeclaredLevel(LOG4J2_ROOT_LOG_LEVEL)
        .ifPresent(level -> Configurator.setRootLevel(level));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
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
