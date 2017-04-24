package org.gusdb.fgputil.web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

/**
 * Facade over HttpServletRequest that simplifies some common calls to and manipulations of
 * HttpServletRequest.  The values are stored in memory in this class (not fetched from the
 * underlying servlet container.  Thus, this is a POJO that can be used successfully after
 * the lifecycle of the actual HTTP request.
 * 
 * @author rdoherty
 */
public class HttpRequestData implements RequestData {

  private final String _webAppBaseUrl;
  private final String _noContextUrl;
  private final String _requestUri;
  private final String _requestUrl;
  private final String _queryString;
  private final String _userAgent;
  private final String _referrer;
  private final String _ipAddress;
  private final String _remoteHost;
  private final String _serverName;
  private final HttpMethod _httpMethod;
  private final Map<String, String[]> _parameters;
  private final Map<String, Object> _attributes;

  public HttpRequestData(HttpServletRequest request) {
    _noContextUrl = new StringBuilder()
        .append(request.getScheme())
        .append("://")
        .append(request.getServerName())
        .append(request.getServerPort() == 80 ||
                request.getServerPort() == 443 ?
                "" : ":" + request.getServerPort())
        .toString();
    _webAppBaseUrl = new StringBuilder()
        .append(_noContextUrl)
        .append(request.getContextPath())
        .toString();
    _requestUri = request.getRequestURI();
    _requestUrl = request.getRequestURL().toString();
    _queryString = request.getQueryString();
    _userAgent = request.getHeader("User-Agent");
    _referrer = request.getHeader("Referer");
    _ipAddress = request.getRemoteAddr();
    _remoteHost = request.getRemoteHost();
    _serverName = request.getServerName();
    _httpMethod = HttpMethod.getValueOf(request.getMethod());
    _parameters = new HashMap<String, String[]>(request.getParameterMap());
    _attributes = new HashMap<>();
    Enumeration<String> attributeNames = request.getAttributeNames();
    while (attributeNames.hasMoreElements()) {
      String key = attributeNames.nextElement();
      _attributes.put(key, request.getAttribute(key));
    }
  }

  @Override
  public String getWebAppBaseUrl() {
    return _webAppBaseUrl;
  }

  @Override
  public String getNoContextUrl() {
    return _noContextUrl;
  }

  @Override
  public String getRequestUri() {
    return _requestUri;
  }

  @Override
  public String getRequestUrl() {
    return _requestUrl;
  }

  @Override
  public String getQueryString() {
    return _queryString;
  }

  @Override
  public String getFullRequestUrl() {
    return new StringBuilder(_requestUrl)
        .append(_queryString == null ? "" : "?" + _queryString)
        .toString();
  }

  @Override
  public String getUserAgent() {
    return _userAgent;
  }

  @Override
  public String getReferrer() {
    return _referrer;
  }

  @Override
  public String getIpAddress() {
    return _ipAddress;
  }

  @Override
  public Object getRequestAttribute(String key) {
    return _attributes.get(key);
  }

  @Override
  public String getRemoteHost() {
    return _remoteHost;
  }

  @Override
  public String getServerName() {
    return _serverName;
  }

  /**
    * name of host running application (this might be different from
    * any proxy webserver the terminal client is talking to)
  **/
  @Override
  public String getAppHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
     } catch (UnknownHostException e) {
      return "N/A";
     }
  }

  /**
    * IP address of host running application (this might be different from
    * any proxy webserver the terminal client is talking to)
  **/
  @Override
  public String getAppHostAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
     } catch (UnknownHostException e) {
      return "N/A";
     }
  }

  @Override
  public Map<String, String[]> getTypedParamMap() {
    return _parameters;
  }

  @Override
  public HttpMethod getMethod() {
    return _httpMethod;
  }
}
