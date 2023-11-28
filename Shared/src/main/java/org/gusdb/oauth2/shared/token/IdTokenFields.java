package org.gusdb.oauth2.shared.token;

import java.util.HashSet;
import java.util.Set;

public enum IdTokenFields {
    iss, // issuer of this token
    sub, // subject (unique user ID)
    aud, // audience (client ID of consumer)
    azp, // authorized parth (same as aud in our case)
    auth_time, // time of authentication (Unix integer seconds)
    iat, // time of issuance (Unix integer seconds)
    exp, // time of expiration (Unix integer seconds)
    nonce, // string value linking original auth request with ID token
    email, // user's email
    email_verified, // whether email is verified
    preferred_username, // human-friendly display name for the user (may or may not be unique/stable)
    is_guest; // whether the user represented by this token is a guest vs registered user
  
  public static Set<String> getNames() {
    Set<String> names = new HashSet<>();
    for (IdTokenFields val : values()) {
      names.add(val.name());
    }
    return names;
  }
}
