package org.gusdb.fgputil.web;

import org.apache.log4j.Logger;

public enum HttpMethod {
  HEAD,
  POST,
  PUT,
  GET,
  PATCH,
  DELETE,
  CONNECT,
  OPTIONS,
  TRACE,
  UNRECOGNIZED;

  private static final Logger LOG = Logger.getLogger(HttpMethod.class);

  public static HttpMethod getValueOf(String method) {
    for (HttpMethod value : values()) {
      if (value.name().equalsIgnoreCase(method)) return value;
    }
    LOG.warn("HTTP Method submitted ('" + method + "') which does not represent a recognized HTTP method.");
    return UNRECOGNIZED;
  }

}
