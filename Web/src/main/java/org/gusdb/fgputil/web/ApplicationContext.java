package org.gusdb.fgputil.web;

import java.io.Closeable;
import java.util.Map;

public interface ApplicationContext extends Map<String,Object>, Closeable {

  String getInitParameter(String key);

  String getRealPath(String path);

}
