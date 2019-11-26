package org.gusdb.fgputil.server;

import org.glassfish.grizzly.http.server.Session;
import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.collection.ReadOnlyMap;
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
  public ReadOnlyMap<String, Object> getAttributeMap() {
    return new ReadOnlyHashMap<>(_session.attributes());
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

  @Override
  public String getId() {
    return _session.getIdInternal();
  }

}
