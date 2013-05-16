package org.gusdb.fgputil.db;

public interface QueryLogConfig {

  public double getBaseline();
  public double getSlow();
  public boolean isIgnoredSlow(String sql);
  public boolean isIgnoredBaseline(String sql);
  
}
