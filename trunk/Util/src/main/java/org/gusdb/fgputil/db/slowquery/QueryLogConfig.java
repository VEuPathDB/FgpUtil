package org.gusdb.fgputil.db.slowquery;

public interface QueryLogConfig {

  public double getBaseline();
  public double getSlow();
  public boolean isIgnoredSlow(String sql);
  public boolean isIgnoredBaseline(String sql);
  
}
