package org.gusdb.fgputil.server;

import static org.gusdb.fgputil.functional.Functions.getMapFromKeys;

import java.util.List;
import java.util.Map;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.util.Header;
import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.collection.ReadOnlyMap;
import org.gusdb.fgputil.web.HttpMethod;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.fgputil.web.SessionProxy;

/**
 * Facade over Grizzly Request object that simplifies some common calls to and manipulations of
 * Request.
 * 
 * @author rdoherty
 */
public class GrizzlyRequestData implements RequestData {

  private final Request _request;

  public GrizzlyRequestData(Request request) {
    _request = request;
  }

  @Override
  public String getNoContextUri() {
    int port = _request.getServerPort();
    String portPart = port == 80 || port == 443 ? "" : ":" + port;
    return (
      _request.getScheme() + "://" +
      getServerName() + portPart
    );
  }

  @Override
  public String getContextUri() {
    return getNoContextUri() + _request.getContextPath();
  }

  @Override
  public String getQueryString() {
    return _request.getQueryString();
  }

  @Override
  public HttpMethod getMethod() {
    return HttpMethod.getValueOf(_request.getMethod().getMethodString());
  }

  @Override
  public Map<String, List<String>> getRequestParamMap() {
    Map<String,String[]> rawMap = _request.getParameterMap();
    return getMapFromKeys(rawMap.keySet(), key -> List.of(rawMap.get(key)));
  }

  @Override
  public SessionProxy getSession() {
    return new GrizzlySessionProxy(_request.getSession());
  }

  @Override
  public ReadOnlyMap<String, Object> getAttributeMap() {
    return new ReadOnlyHashMap<>(getMapFromKeys(_request.getAttributeNames(), _request::getAttribute));
  }

  @Override
  public Object getAttribute(String name) {
    return _request.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    _request.setAttribute(name, value);
  }

  @Override
  public Object removeAttribute(String name) {
    Object obj = _request.getAttribute(name);
    _request.removeAttribute(name);
    return obj;
  }

  @Override
  public String getUserAgent() {
    return _request.getHeader(Header.UserAgent);
  }

  @Override
  public String getReferrer() {
    return _request.getHeader(Header.Referer);
  }

  @Override
  public String getHeader(String name) {
    return _request.getHeader(name);
  }

  @Override
  public String getServerName() {
    return _request.getServerName();
  }

  @Override
  public String getRemoteHost() {
    return _request.getRemoteHost();
  }

  @Override
  public String getRemoteIpAddress() {
    return _request.getRemoteAddr();
  }
}
