package org.gusdb.fgputil.logging;

import org.apache.logging.log4j.ThreadContext;

/**
 * Manages thread-based data using Log4j's MDC mechanism.
 * 
 * See {@code MDCServletFilter}
 * 
 * @author rdoherty
 */
public class ThreadLocalLoggingVars {

  public static final String REQUEST_TIMER_KEY = "requestTimer";
  public static final String IP_ADDRESS_KEY = "ipAddress";
  public static final String REQUESTED_DOMAIN_KEY = "requestedDomain";
  public static final String SESSION_ID_KEY = "sessionId";
  public static final String SHORT_SESSION_ID_KEY = "shortSessionId";
  public static final String REQUEST_ID_KEY = "requestId";

  public static void setRequestStartTime(final long startTime) {
    ThreadContext.put(REQUEST_TIMER_KEY, String.valueOf(startTime));
  }

  public static String getRequestDuration() {
    return RequestDurationPatternConverter.getRequestDuration(
        ThreadContext.get(REQUEST_TIMER_KEY));
  }

  public static void setIpAddress(String ipAddress) {
    if (ipAddress != null) {
      ThreadContext.put(IP_ADDRESS_KEY, ipAddress);
    }
  }

  public static String getIpAddress() {
    return ThreadContext.get(IP_ADDRESS_KEY);
  }

  public static void setRequestId(String requestId) {
    if (requestId != null) {
      ThreadContext.put(REQUEST_ID_KEY, requestId);
    }
  }

  public static String getRequestId() {
    return ThreadContext.get(REQUEST_ID_KEY);
  }

  public static void setRequestedDomain(String domain) {
    if (domain != null) {
      ThreadContext.put(REQUESTED_DOMAIN_KEY, domain);
    }
  }

  public static String getRequestedDomain() {
    return ThreadContext.get(REQUESTED_DOMAIN_KEY);
  }

  public static void setSessionId(String sessionId) {
    if (sessionId != null) {
      ThreadContext.put(SESSION_ID_KEY, sessionId);
      ThreadContext.put(SHORT_SESSION_ID_KEY,
          sessionId.substring(0, Math.min(5, sessionId.length())));
    }
  }

  public static String getSessionId() {
    return ThreadContext.get(SESSION_ID_KEY);
  }

  public static String getShortSessionId() {
    return ThreadContext.get(SHORT_SESSION_ID_KEY);
  }

  public static ThreadContextBundle getThreadContextBundle() {
    return new ThreadContextBundle(
        getRequestDuration(),
        getIpAddress(),
        getRequestId(),
        getSessionId(),
        getShortSessionId(),
        getRequestedDomain());
  }

  public static void clearValues() {
    ThreadContext.remove(REQUEST_TIMER_KEY);
    ThreadContext.remove(IP_ADDRESS_KEY);
    ThreadContext.remove(REQUESTED_DOMAIN_KEY);
    ThreadContext.remove(SESSION_ID_KEY);
    ThreadContext.remove(SHORT_SESSION_ID_KEY);
    ThreadContext.remove(REQUEST_ID_KEY);
  }

  public static void setNonRequestThreadVars(String threadId) {
    ThreadLocalLoggingVars.setRequestId(threadId);
    ThreadLocalLoggingVars.setSessionId(threadId);
    ThreadLocalLoggingVars.setIpAddress("<no_ip_address>");
    ThreadLocalLoggingVars.setRequestStartTime(System.currentTimeMillis());
  }

  public static class ThreadContextBundle {

    private String _requestDuration;
    private String _ipAddress;
    private String _requestId;
    private String _sessionId;
    private String _shortSessionId;
    private String _requestedDomain;

    public ThreadContextBundle(String requestDuration, String ipAddress, String requestId, String sessionId,
        String shortSessionId, String requestedDomain) {
      _requestDuration = requestDuration;
      _ipAddress = ipAddress;
      _requestId = requestId;
      _sessionId = sessionId;
      _shortSessionId = shortSessionId;
      _requestedDomain = requestedDomain;
    }

    public String getRequestDuration() {
      return _requestDuration;
    }
    public String getIpAddress() {
      return _ipAddress;
    }
    public String getRequestId() {
      return _requestId;
    }
    public String getSessionId() {
      return _sessionId;
    }
    public String getShortSessionId() {
      return _shortSessionId;
    }
    public String getRequestedDomain() {
      return _requestedDomain;
    }
  }
}
