package org.gusdb.fgputil.db.wrapper;

import java.sql.Statement;

import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor.UnclosedObjectMonitorMap;

public interface AnyStatementWrapper extends Statement {

  UnclosedObjectMonitorMap getUnclosedObjectMonitorMap();

}
