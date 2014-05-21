package org.gusdb.fgputil.db.pool;

import org.apache.log4j.Logger;

public class ConnectionPoolLogger implements Runnable {

  private static final Logger logger = Logger.getLogger(ConnectionPoolLogger.class);
  
  private final DatabaseInstance _dbInstance;
  private volatile boolean _shutdownFlag = false;
  
  public ConnectionPoolLogger(DatabaseInstance dbInstance) {
    _dbInstance = dbInstance;
  }
  
  @Override
  public void run() {
      ConnectionPoolConfig config = _dbInstance.getConfig();
      long interval = config.getShowConnectionsInterval();
      long duration = config.getShowConnectionsDuration();
      long startTime = System.currentTimeMillis();
      while (!Thread.currentThread().isInterrupted() && !_shutdownFlag) {
          StringBuffer display = new StringBuffer();
          display.append("[").append(_dbInstance.getName()).append("]");
          display.append(" Connections: Active = ").append(
                  _dbInstance.getActiveCount());
          display.append(", Idle = ").append(_dbInstance.getIdleCount());

          logger.info(display);
          long elapsed = (System.currentTimeMillis() - startTime) / 1000;
          if (duration > 0 && elapsed > duration) break;
          try {
              Thread.sleep(interval * 1000);
          } catch (InterruptedException ex) {
              break;
          }
      }
  }

  public void shutDown() {
    _shutdownFlag = true;
  }
}
