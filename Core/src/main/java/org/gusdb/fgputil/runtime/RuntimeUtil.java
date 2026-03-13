package org.gusdb.fgputil.runtime;

import static org.gusdb.fgputil.functional.Functions.swallowAndGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class RuntimeUtil {

  private static final Logger LOG = Logger.getLogger(RuntimeUtil.class);

  public static synchronized long getPid(Process p) {
    int pid = -1;
    try {
      if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
        Field f = p.getClass().getDeclaredField("pid");
        f.setAccessible(true);
        pid = f.getInt(p);
        f.setAccessible(false);
      }
      else {
        LOG.warn("Process PID requested from non-unix process of type: " + p.getClass().getName());
      }
    }
    catch (Exception e) {
      LOG.warn("Unable to look up UNIX process pid", e);
      pid = -1;
    }
    return pid;
  }

  public static void executeAndLogOutput(List<String> command, Map<String,String> environment,
      Logger logger, Level logLevel, Optional<Duration> processTimeout, boolean killOnTimeout) {
    Thread logMonitorThread = null;
    try {
      LOG.info("Starting subprocess with command: " + String.join(" ", command));
      // start the process
      ProcessBuilder processBuilder = new ProcessBuilder()
          .command(command)
          .redirectErrorStream(true);
      processBuilder.environment().putAll(environment);
      Process process = processBuilder.start();

      // start a thread to stream output to the passed Logger (if level allows)
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      logMonitorThread = new Thread(() -> {
        try {
          String line;
          while ((line = reader.readLine()) != null) {
            logger.log(logLevel, ">> " + line);
          }
        }
        catch (IOException e) {
          logger.log(logLevel, "Parent process warning: could not read subprocess output", e);
        }
      });
      logMonitorThread.start();

      // wait for process to finish in this thread
      boolean exitWithoutTimeout = processTimeout
          .map(duration -> swallowAndGet(() -> process.waitFor(duration.toMillis(), TimeUnit.MILLISECONDS)))
          .orElse(process.waitFor() - process.exitValue() == 0); // always true

      // wait for the thread to finish processing the subprocess's output
      logMonitorThread.join();

      if (exitWithoutTimeout) {
        LOG.info("Subprocess exited with exit code: " + process.exitValue());
        if (process.exitValue() != 0) {
          throw new RuntimeException("Subprocess exited with error code " + process.exitValue());
        }
      }
      else {
        // subprocess timed out before completion; kill if requested
        if (killOnTimeout) {
          int gracefulShutdownWindow = 500;
          LOG.info("Subprocess timed out before completion.  Attempting to shut down gracefully...");
          process.destroy();
          ThreadUtil.sleep(gracefulShutdownWindow);
          if (process.isAlive()) {
            LOG.info("Subprocess did not shut down gracefully after " + gracefulShutdownWindow + "ms.  Forcibly terminating.");
            process.destroyForcibly();
          }
        }
        throw new RuntimeException("Subprocess timed out before completion");
      }
    }
    catch (InterruptedException e) {
      throw new RuntimeException("Subprocess was interrupted before completion", e);
    }
    catch (IOException e) {
      throw new RuntimeException("Error occurred while executing subprocess", e);
    }
    finally {
      // kill log monitor thread if still active
      if (logMonitorThread != null && logMonitorThread.isAlive()) {
        logMonitorThread.interrupt();
      }
    }
  }
}
