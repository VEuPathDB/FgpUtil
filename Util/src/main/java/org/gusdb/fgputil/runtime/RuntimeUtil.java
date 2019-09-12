package org.gusdb.fgputil.runtime;

import java.lang.reflect.Field;

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
}
