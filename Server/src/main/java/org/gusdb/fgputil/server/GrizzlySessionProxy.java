package org.gusdb.fgputil.server;

import java.util.Map;

import org.glassfish.grizzly.http.server.Session;
import org.gusdb.fgputil.web.SessionProxy;

public class GrizzlySessionProxy implements SessionProxy {

  private final Session _session;

  public GrizzlySessionProxy(Session session) {
    _session = session;
  }

  @Override
  public Object getAttribute(String key) {
    return _session.getAttribute(key);
  }

  @Override
  public Map<String, Object> getAttributeMap() {
    return _session.attributes();
  }

  @Override
  public void setAttribute(String key, Object value) {
    _session.setAttribute(key, value);
  }

  @Override
  public void removeAttribute(String key) {
    _session.removeAttribute(key);
  }

  @Override
  public Object getUnderlyingSession() {
    return _session;
  }

  @Override
  public void invalidate() {
    _session.setSessionTimeout(0);
  }

}
