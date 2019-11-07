package org.gusdb.fgputil.web;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

public class HttpSessionProxy implements SessionProxy {

  private final HttpSession _session;

  public HttpSessionProxy(HttpSession session) {
    _session = session;
  }

  @Override
  public Object getAttribute(String key) {
    return _session.getAttribute(key);
  }

  @Override
  public Map<String, Object> getAttributeMap() {
    Map<String,Object> map = new HashMap<>();
    Enumeration<String> names = _session.getAttributeNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      map.put(name, _session.getAttribute(name));
    }
    return map;
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
    _session.invalidate();
  }

}
