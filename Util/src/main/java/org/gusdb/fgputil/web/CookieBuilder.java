package org.gusdb.fgputil.web;

import java.util.Objects;

import javax.servlet.http.Cookie;
import javax.ws.rs.core.NewCookie;

public class CookieBuilder {

  private String _name;
  private String _value;
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

  public CookieBuilder setName(String name) {
    _name = name;
    return this;
  }

  public String getValue() {
    return _value;
  }

  public CookieBuilder setValue(String value) {
    _value = value;
    return this;
  }

  public int getMaxAge() {
    return _maxAge;
  }

  public CookieBuilder setMaxAge(int maxAge) {
    _maxAge = maxAge;
    return this;
  }

  public String getPath() {
    return _path;
  }

  public CookieBuilder setPath(String path) {
    Objects.requireNonNull(path);
    _path = path;
    return this;
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
