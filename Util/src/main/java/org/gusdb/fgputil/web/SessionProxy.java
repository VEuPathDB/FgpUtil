package org.gusdb.fgputil.web;

import java.util.Map;

public interface SessionProxy {

  Object getAttribute(String key);

  Map<String, Object> getAttributeMap();

  void setAttribute(String key, Object value);

  void removeAttribute(String key);

  Object getUnderlyingSession();

  void invalidate();

}
