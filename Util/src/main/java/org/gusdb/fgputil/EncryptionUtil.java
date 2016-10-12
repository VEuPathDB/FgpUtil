package org.gusdb.fgputil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtil {

  private static MessageDigest newMd5Digester() {
    try {
      return MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("This JVM no longer supports the MD5 encryption algorithm.", e);
    }
  }

  public static String encrypt(String data) {
    // cannot encrypt null or empty value
    if (data == null || data.isEmpty()) {
      throw new IllegalArgumentException("Cannot encrypt an empty/null string");
    }
    byte[] byteBuffer = newMd5Digester().digest(data.getBytes());
    // convert each byte into hex format
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < byteBuffer.length; i++) {
      int code = (byteBuffer[i] & 0xFF);
      if (code < 0x10)
        buffer.append('0');
      buffer.append(Integer.toHexString(code));
    }
    return buffer.toString();
  }
}
