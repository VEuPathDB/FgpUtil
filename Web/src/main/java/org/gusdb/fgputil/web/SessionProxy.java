package org.gusdb.fgputil.web;

import org.gusdb.fgputil.collection.ReadOnlyMap;

public interface SessionProxy {

  Object getAttribute(String key);

  ReadOnlyMap<String, Object> getAttributeMap();

  void setAttribute(String key, Object value);

  void removeAttribute(String key);

  Object getUnderlyingSession();

  void invalidate();

  String getId();

}
