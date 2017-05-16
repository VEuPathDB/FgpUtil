package org.gusdb.fgputil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class EncryptionUtil {

  private EncryptionUtil() {}

  private static MessageDigest newMd5Digester() {
    try {
      return MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("This JVM no longer supports the MD5 encryption algorithm.", e);
    }
  }

  private static String convertToHex(byte[] bytes, boolean padZeroes) {
    StringBuilder buffer = new StringBuilder();
    for (byte b : bytes) {
      int code = (b & 0xFF);
      if (padZeroes && code < 0x10) {
        buffer.append('0');
      }
      buffer.append(Integer.toHexString(code));
    }
    return buffer.toString();
  }

  /**
   * md5 checksum algorithm. encrypt(String) drops leading zeros of hex codes so
   * is not compatible with md5
   **/
  public static String md5(String str) {
    StringBuffer buffer = new StringBuffer();
    for (byte code : newMd5Digester().digest(str.getBytes())) {
      buffer.append(Integer.toString((code & 0xff) + 0x100, 16).substring(1));
    }
    return buffer.toString();
  }

  public static String encryptPassword(String password) {
    return convertToHex(newMd5Digester().digest(password.getBytes()), false);
  }

  public static String encrypt(String data) {
    return encrypt(data, false);
  }

  public static String encrypt(String data, boolean shortDigest) {
    // cannot encrypt null value
    if (data == null || data.length() == 0)
      throw new IllegalArgumentException("Cannot encrypt an empty/null string");

    byte[] byteBuffer = newMd5Digester().digest(data.getBytes());

    if (shortDigest) {
      // just take the first 8 bytes from MD5 hash
      byteBuffer = Arrays.copyOf(byteBuffer, Math.min(byteBuffer.length, 8));
    }

    return convertToHex(byteBuffer, true);
  }
}
