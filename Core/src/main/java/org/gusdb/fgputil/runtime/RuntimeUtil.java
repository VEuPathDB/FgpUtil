package org.gusdb.fgputil.runtime;

import static org.gusdb.fgputil.functional.Functions.swallowAndGet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

  /**
   * Returns the PID of the passed process.
   *
   * @deprecated Use Process.getPid(), available in Java 9+.
   * @param p process
   * @return pid of process
   */
  @Deprecated
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

  /**
   * Synchronously executes a subprocess with the passed command, handling a variety of
   * I/O redirection options and process timout options.  If the subprocess fails to
   * complete before timeout, an empty optional is returned; otherwise the exit value of
   * the process is returned.
   *
   * @param command command used to execute the subprocess
   * @param environment supplemental environment variables (will inherit env from this process)
   * @param stdinFile optional file to send to subprocess's stdin stream
   * @param logger Logger to use to log subprocess output
   * @param logLevel log level used to log subprocess output
   * @param stdoutFile optional file to write subprocess stdout to.  If not specified,
   * stdout/stderr will both be written to the Logger
   * @param processTimeout optional duration after which subprocess will be determined to have
   * "timed out".  This method will cease I/O capture and forcibly kill the subprocess (to
   * avoid a zombie process), and return an empty optional.
   * @return exit value of the subprocess or empty if subprocess times out
   */
  public static Optional<Integer> executeSubprocessAndLogOutput(
      List<String> command,
      Map<String,String> environment,
      Optional<File> stdinFile,
      Logger logger,
      Level logLevel,
      Optional<File> stdoutFile,
      Optional<Duration> processTimeout) {
    Thread logMonitorThread = null;
    try {
      LOG.info("Starting subprocess with command: " + String.join(" ", command));

      // configure the process
      ProcessBuilder processBuilder = new ProcessBuilder().command(command);

      // if stdin file is passed, redirect stdin to it
      if (stdinFile.isPresent())
        processBuilder.redirectInput(stdinFile.get());

      // if stdout file is passed, write stdout to the file; otherwise merge with stderr
      if (stdoutFile.isPresent())
        processBuilder.redirectInput(stdoutFile.get());
      else
        processBuilder.redirectErrorStream(true);

      // set passed environment
      processBuilder.environment().putAll(environment);

      // start the process
      Process process = processBuilder.start();

      // start a thread to stream output to the passed Logger (if level allows)
      InputStream streamToLog = stdoutFile.isPresent() ? process.getErrorStream() : process.getInputStream();
      logMonitorThread = new Thread(() -> {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(streamToLog))) {
          String line;
          while ((line = reader.readLine()) != null) {
            if (Thread.interrupted()) return;
            logger.log(logLevel, ">> " + line);
          }
        }
        catch (IOException e) {
          LOG.error("Parent process warning: could not read subprocess output", e);
        }
      });
      logMonitorThread.start();

      // wait for process to finish in this thread
      boolean exitWithoutTimeout = processTimeout
          .map(duration -> swallowAndGet(() -> process.waitFor(duration.toMillis(), TimeUnit.MILLISECONDS)))
          .orElse(process.waitFor() - process.exitValue() == 0); // always true

      if (exitWithoutTimeout) {
        waitForLoggingThread(logMonitorThread);
        int exitValue = process.exitValue();
        LOG.info("Subprocess exited with exit code: " + exitValue);
        return Optional.of(exitValue);
      }
      else {
        // subprocess timed out before completion; kill to avoid zombie process
        int gracefulShutdownWindow = 500;
        LOG.info("Subprocess timed out before completion.  Attempting to shut down gracefully...");
        process.destroy();
        if (!process.waitFor(gracefulShutdownWindow, TimeUnit.MILLISECONDS)) {
          LOG.info("Subprocess did not shut down gracefully after " + gracefulShutdownWindow + "ms.  Forcibly terminating.");
          process.destroyForcibly(); // escalate
          process.waitFor(); // ensure it's actually gone
        }
        waitForLoggingThread(logMonitorThread);
        return Optional.empty();
      }
    }
    catch (RuntimeException e) {
      if (e.getCause() != null && e.getCause() instanceof InterruptedException) {
        throw new RuntimeException("This thread or logger thread was interrupted before subprocess handling could complete", e.getCause());
      }
      throw e;
    }
    catch (InterruptedException e) {
      throw new RuntimeException("This thread or logger thread was interrupted before subprocess handling could complete", e);
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

  private static void waitForLoggingThread(Thread logMonitorThread) throws InterruptedException {
    // Wait for the thread to finish processing the subprocess's output (max 500ms).
    //   This should not take long because the system output buffer is not that big and
    //   the thread has been streaming the data out throughout the subprocess lifespan.
    logMonitorThread.join(500);
  }
}
