package org.gusdb.fgputil.db.pool;

import org.apache.log4j.Logger;

public class ConnectionPoolLogger implements Runnable {

  private static final Logger LOG = Logger.getLogger(ConnectionPoolLogger.class);

  private final DatabaseInstance _dbInstance;
  private volatile boolean _shutdownFlag = false;

  public ConnectionPoolLogger(DatabaseInstance dbInstance) {
    _dbInstance = dbInstance;
  }

  @Override
  public void run() {
    ConnectionPoolConfig config = _dbInstance.getConfig();
    long intervalMs = config.getShowConnectionsInterval();
    long durationSecs = config.getShowConnectionsDuration();
    LOG.info("Connection Pool Logger started.  Will log every " + intervalMs +
        "milliseconds" + (durationSecs > 0 ? " for " + durationSecs +
            " seconds, then shut down." : "."));
    long startTime = System.currentTimeMillis();
    while (!Thread.currentThread().isInterrupted() && !_shutdownFlag) {
      LOG.info(new StringBuilder()
        .append("[").append(_dbInstance.getIdentifier()).append("]")
        .append(" Connections: Active = ").append(_dbInstance.getActiveCount())
        .append(", Idle = ").append(_dbInstance.getIdleCount())
        .toString());

      long elapsedSecs = (System.currentTimeMillis() - startTime) / 1000;
      if (durationSecs > 0 && elapsedSecs > durationSecs) {
        LOG.info("Times up!  Shutting down Connection Pool Logger.");
        break;
      }
      try {
        Thread.sleep(intervalMs * 1000);
      }
      catch (InterruptedException ex) {
        LOG.info("Connection Pool Logger interrupted.  Shutting down.");
        break;
      }
    }
    LOG.info("Connection Pool Logger is shut down.");
  }

  public void shutDown() {
    LOG.info("Signaling Connection Pool Logger to shut down.");
    _shutdownFlag = true;
  }
}
