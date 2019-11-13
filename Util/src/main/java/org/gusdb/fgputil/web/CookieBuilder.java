package org.gusdb.fgputil.web;

import javax.servlet.http.Cookie;
import javax.ws.rs.core.NewCookie;

public class CookieBuilder {

  private String _name;
  private String _value = "";
  private int _maxAge = -1;
  private String _path = "/";

  public CookieBuilder() {}

  public CookieBuilder(String name, String value) {
    _name = name;
    _value = value;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public String getValue() {
    return _value;
  }

  public void setValue(String value) {
    _value = value;
  }

  public int getMaxAge() {
    return _maxAge;
  }

  public void setMaxAge(int maxAge) {
    _maxAge = maxAge;
  }

  public String getPath() {
    return _path;
  }

  public void setPath(String path) {
    _path = path;
  }

  public Cookie toHttpCookie() {
    Cookie cookie = new Cookie(getName(), getValue());
    cookie.setMaxAge(getMaxAge());
    cookie.setPath(getPath());
    return cookie;
  }

  public NewCookie toJaxRsCookie() {
    return new NewCookie(
        getName(),
        getValue(),
        getPath(),
        null,
        NewCookie.DEFAULT_VERSION,
        null,
        getMaxAge(),
        false
    );
  }

}
