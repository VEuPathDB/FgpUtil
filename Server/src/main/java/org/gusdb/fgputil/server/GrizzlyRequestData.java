package org.gusdb.fgputil.server;

import java.util.Map;

import org.glassfish.grizzly.http.server.Request;
import org.gusdb.fgputil.web.HttpMethod;
import org.gusdb.fgputil.web.RequestData;

public class GrizzlyRequestData implements RequestData {

  private final Request _request;

  public GrizzlyRequestData(Request request) {
    _request = request;
  }

  @Override
  public String getRequestUri() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getNoContextUrl() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getWebAppBaseUrl() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRequestUrl() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getQueryString() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getFullRequestUrl() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getReferrer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getIpAddress() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getRequestAttribute(String key) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteHost() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getServerName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getAppHostName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getAppHostAddress() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getUserAgent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HttpMethod getMethod() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String[]> getTypedParamMap() {
    // TODO Auto-generated method stub
    return null;
  }

}
