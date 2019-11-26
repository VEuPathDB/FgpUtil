package org.gusdb.fgputil.web;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;


/**
 * Provides standard information that came in on the current request
 * 
 * @author rdoherty
 */
public interface RequestData {

  default RequestSnapshot getSnapshot() {
    return new RequestSnapshot(this);
  }

  /**
   * @return fully qualified URI of the host/port, without an application context
   */
  String getNoContextUri();

  /**
   * @return fully qualified URI of the base application
   */
  String getContextUri();

  /**
   * @return fully qualified, complete URI of the request, including query params if present
   */
  default String getFullRequestUri() {
    String queryString = getQueryString();
    return getContextUri() + (queryString == null || queryString.isEmpty() ? "" : "?" + queryString);
  }

  /**
   * @return query string of the request URI
   */
  String getQueryString();

  /**
   * @return HTTP method of this request
   */
  HttpMethod getMethod();

  /**
   * @return map of request parameters (query if GET, form-encoded if POST)
   */
  Map<String,List<String>> getRequestParamMap();

  /**
   * @return current session
   */
  SessionProxy getSession();

  /**
   * @return map of attributes attached to this request
   */
  Map<String, Object> getAttributeMap();

  /**
   * Adds an attribute to the underlying request object
   */
  void setAttribute(String name, Object value);

  /**
   * @return user-agent header value
   */
  String getUserAgent();

  /**
   * @return referer header value
   */
  String getReferrer();

  String getServerName();

  String getRemoteHost();

  String getRemoteIpAddress();

  /**
   * name of host running application (this might be different from
   * any proxy webserver the terminal client is talking to)
   **/
  default String getLocalHost() {
    try {
      return InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e) {
      return "N/A";
    }
  }

  /**
   * IP address of host running application (this might be different from
   * any proxy webserver the terminal client is talking to)
   **/
  default String getLocalIpAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    }
    catch (UnknownHostException e) {
      return "N/A";
    }
  }
}
