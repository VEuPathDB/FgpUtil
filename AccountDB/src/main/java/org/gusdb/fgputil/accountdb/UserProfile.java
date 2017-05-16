package org.gusdb.fgputil.accountdb;

import java.util.Date;
import java.util.Map;

public class UserProfile {

  public static final int MAX_EMAIL_LENGTH = 255;
  public static final int MAX_PROPERTY_VALUE_SIZE = 4000;

  private Long _userId;
  private String _email;
  private boolean _isGuest;
  private String _signature;
  private String _stableId;
  private Date _registerTime;
  private Date _lastLoginTime;
  private Map<String, String> _properties;

  public Long getUserId() {
    return _userId;
  }
  public void setUserId(Long userId) {
    _userId = userId;
  }
  public String getEmail() {
    return _email;
  }
  public void setEmail(String email) {
    _email = email;
  }
  public boolean isGuest() {
    return _isGuest;
  }
  public void setGuest(boolean isGuest) {
    _isGuest = isGuest;
  }
  public String getSignature() {
    return _signature;
  }
  public void setSignature(String signature) {
    _signature = signature;
  }
  public String getStableId() {
    return _stableId;
  }
  public void setStableId(String stableId) {
    _stableId = stableId;
  }
  public Date getRegisterTime() {
    return _registerTime;
  }
  public void setRegisterTime(Date registerTime) {
    _registerTime = registerTime;
  }
  public Date getLastLoginTime() {
    return _lastLoginTime;
  }
  public void setLastLoginTime(Date lastLoginTime) {
    _lastLoginTime = lastLoginTime;
  }
  public Map<String, String> getProperties() {
    return _properties;
  }
  public void setProperties(Map<String, String> properties) {
    _properties = properties;
  }
}
