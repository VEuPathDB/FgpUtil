package org.gusdb.fgputil.db.leakmonitor;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.Date;
import java.util.Map;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;

public class UnclosedObjectInfo {

  private String _dbName;
  private CloseableObjectType<?> _type;
  private Date _timeOpened;
  private String _stackTrace;
  private String _stackTraceHash;

  public UnclosedObjectInfo(String dbName, CloseableObjectType<?> type) {
    this(dbName, type, null);
  }

  public UnclosedObjectInfo(String dbName, CloseableObjectType<?> type, Map<String, String> globalStacktraceMap) {
    _dbName = dbName;
    _type = type;
    _timeOpened = new Date();
    _stackTrace = FormatUtil.getCurrentStackTrace();
    _stackTraceHash = EncryptionUtil.encrypt(_stackTrace);
    // only add stack trace to global map if specified
    if (globalStacktraceMap != null) {
      globalStacktraceMap.put(_stackTraceHash, _stackTrace);
    }
  }

  public String getStackTraceHash() {
    return _stackTraceHash;
  }

  public String getStackTrace() {
    return _stackTrace;
  }

  public String getBasicInfo() {
    String timeOpenedStr = FormatUtil.formatDateTime(_timeOpened);
    double secondsOpen = ((double)(new Date().getTime() - _timeOpened.getTime())) / 1000;
    return new StringBuilder()
        .append(_type)
        .append(" for ").append(_dbName)
        .append(", open for ").append(secondsOpen)
        .append(" seconds, retrieved from pool at ").append(timeOpenedStr)
        .toString();
  }

  @Override
  public String toString() {
    return new StringBuilder()
      .append(getStackTraceHash()).append(": ")
      .append(getBasicInfo()).append(NL)
      .append(getStackTrace())
      .toString();
  }
}
