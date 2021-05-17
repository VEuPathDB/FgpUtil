package org.gusdb.fgputil.db.leakmonitor;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class UnclosedObjectMonitor<T> {

  private static final Logger LOG = Logger.getLogger(UnclosedObjectMonitor.class);

  public static class UnclosedObjectMonitorMap extends ArrayList<UnclosedObjectMonitor<?>> {

    public UnclosedObjectMonitorMap(String dbName, boolean recordAllStacktraces) {
      for (CloseableObjectType<?> type : CloseableObjectType.values()) {
        add(new UnclosedObjectMonitor<>(type, dbName, recordAllStacktraces));
      }
    }

    @SuppressWarnings("unchecked")
    public <T> UnclosedObjectMonitor<T> get(CloseableObjectType<T> key) {
      return (UnclosedObjectMonitor<T>) stream()
          .filter(obj -> obj.getType().equals(key))
          .findFirst().orElseThrow();
    }
  }

  private final CloseableObjectType<T> _type;
  private final String _dbName;
  private final boolean _recordAllStacktraces;
  private final Map<T, UnclosedObjectInfo> _unclosedObjectMap = new ConcurrentHashMap<>();
  private final Map<String, String> _globalStacktraceMap = new ConcurrentHashMap<>();
  private final AtomicInteger _numOpened = new AtomicInteger(0);
  private final AtomicInteger _numClosed = new AtomicInteger(0);

  private UnclosedObjectMonitor(CloseableObjectType<T> type, String dbName, boolean recordAllStacktraces) {
    _type = type;
    _dbName = dbName;
    _recordAllStacktraces = recordAllStacktraces;
  }

  public CloseableObjectType<T> getType() {
    return _type;
  }

  public void registerOpenedObject(T obj) {

    UnclosedObjectInfo info = (_recordAllStacktraces ?
        new UnclosedObjectInfo(_dbName, _type, _globalStacktraceMap) :
        new UnclosedObjectInfo(_dbName, _type));

    if (LOG.isDebugEnabled()) {
      // log hash for this object; let caller know what was opened
      LOG.debug("Opening " + _type.getName() + " associated with stacktrace hash " +
          info.getStackTraceHash() + " : " + info.getBasicInfo());
    }

    _unclosedObjectMap.put(obj, info);
    _numOpened.incrementAndGet();
  }

  public void unregisterClosedObject(T obj) {

    if (LOG.isDebugEnabled()) {
      // log hash for this object; let caller know what was closed
      UnclosedObjectInfo info = _unclosedObjectMap.get(obj);
      LOG.debug("Closing " + _type.getName() + " associated with stacktrace hash " +
          info.getStackTraceHash() + " : " + info.getBasicInfo());
    }

    _numClosed.incrementAndGet();
    _unclosedObjectMap.remove(obj);
  }

  public String getUnclosedObjectInfo() {
    // establish object names
    String typeName = _type.getName();
    String typeNamePlural = typeName + "s";

    // accumulate counts of stack traces
    Collection<UnclosedObjectInfo> rawInfoList = _unclosedObjectMap.values();
    Map<String, List<UnclosedObjectInfo>> countsMap = new HashMap<>();
    List<List<UnclosedObjectInfo>> countsList = new ArrayList<>();

    // getting map values should be thread safe; values are simple pojos
    for (UnclosedObjectInfo info : rawInfoList) {
      String hash = info.getStackTraceHash();
      List<UnclosedObjectInfo> counts = countsMap.get(hash);
      if (counts == null) {
        counts = new ArrayList<UnclosedObjectInfo>();
        countsMap.put(hash, counts);
        countsList.add(counts);
      }
      counts.add(info);
    }

    // sort by number of instances (descending)
    Collections.sort(countsList, (o1, o2) -> o2.size() - o1.size());

    // build output
    StringBuilder sb = new StringBuilder(NL)
        .append("================================").append(NL)
        .append(" Unclosed " + typeName + " Statistics ").append(NL)
        .append("================================").append(NL).append(NL)
        .append("  ").append(_numOpened.get()).append(" " + typeNamePlural + " opened").append(NL)
        .append("  ").append(_numClosed.get()).append(" " + typeNamePlural + " closed").append(NL)
        .append("  ").append(rawInfoList.size()).append(" currently open " + typeNamePlural).append(NL).append(NL);

    // if no unclosed objects exist, skip unclosed section
    if (!rawInfoList.isEmpty()) {

      for (List<UnclosedObjectInfo> infoList : countsList) {
        UnclosedObjectInfo firstInfo = infoList.get(0);
        sb.append("  ").append(infoList.size()).append(" : ").append(firstInfo.getStackTraceHash()).append(NL);
      }

      sb.append(NL)
        .append("======================").append(NL)
        .append(" Unclosed " + typeNamePlural).append(NL)
        .append("======================").append(NL).append(NL);
      for (List<UnclosedObjectInfo> infoList : countsList) {
        UnclosedObjectInfo firstInfo = infoList.get(0);
        sb.append(firstInfo.getStackTraceHash()).append(": ")
          .append(infoList.size()).append(" instances").append(NL).append(NL)
          .append("  Instance details:").append(NL).append(NL);
        for (UnclosedObjectInfo info : infoList) {
          sb.append("    ").append(info.getBasicInfo()).append(NL);
        }
        sb.append(NL)
          .append("  Stack trace:").append(NL).append(NL)
          .append("    ").append(firstInfo.getStackTrace()).append(NL);
      }
    }

    // show entire mapping of hash -> stacktrace
    if (_recordAllStacktraces) {
      sb.append(NL)
        .append("================================").append(NL)
        .append(" Historical Stacktrace Hash Map ").append(NL)
        .append("================================").append(NL).append(NL)
        .append(_globalStacktraceMap.size())
        .append(" distinct stack traces opened connections since initialization.")
        .append(NL).append(NL);
      for (Entry<String, String> hashMapping : _globalStacktraceMap.entrySet()) {
        sb.append(hashMapping.getKey()).append(NL).append("  ")
          .append(hashMapping.getValue()).append(NL).append(NL);
      }
    }

    return sb.toString();
  }

  public int getNumOpened() {
    return _numOpened.get();
  }

  public int getNumClosed() {
    return _numClosed.get();
  }

  public int getNumCurrentlyOpen() {
    return _unclosedObjectMap.values().size();
  }

}
