package org.gusdb.fgputil.web;

import java.util.List;

import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.collection.ReadOnlyMap;

public class RequestSnapshot {

  private final String _contextUri;
  private final String _remoteHost;
  private final String _referrer;
  private final String _userAgent;
  private final String _serverName;
  private final ReadOnlyHashMap<String, Object> _attributeMap;
  private final ReadOnlyHashMap<String, List<String>> _requestParamMap;

  public RequestSnapshot(RequestData requestData) {
    _contextUri = requestData.getContextUri();
    _remoteHost = requestData.getRemoteHost();
    _referrer = requestData.getReferrer();
    _userAgent = requestData.getUserAgent();
    _serverName = requestData.getServerName();
    // NOTE: shallow copies!
    _attributeMap = new ReadOnlyHashMap<>(requestData.getAttributeMap());
    _requestParamMap = new ReadOnlyHashMap<>(requestData.getRequestParamMap());
  }

  public String getContextUri() {
    return _contextUri;
  }

  public String getRemoteHost() {
    return _remoteHost;
  }

  public String getReferrer() {
    return _referrer;
  }

  public String getUserAgent() {
    return _userAgent;
  }

  public String getServerName() {
    return _serverName;
  }

  public ReadOnlyMap<String, Object> getAttributes() {
    return _attributeMap;
  }

  public ReadOnlyMap<String, List<String>> getRequestParamMap() {
    return _requestParamMap;
  }


}
