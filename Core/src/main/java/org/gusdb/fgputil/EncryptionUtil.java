package org.gusdb.fgputil;

import static java.util.Arrays.asList;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static org.gusdb.fgputil.FormatUtil.decodeUtf8EncodedBytes;
import static org.gusdb.fgputil.FormatUtil.getUtf8EncodedBytes;
import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.functional.Functions.getMapFromList;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import org.gusdb.fgputil.Tuples.TwoTuple;

public class EncryptionUtil {

  private EncryptionUtil() {}

  public static MessageDigest newMd5Digester() {
    try {
      return MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("This JVM no longer supports the MD5 encryption algorithm.", e);
    }
  }

  public static String convertToHex(byte[] bytes, boolean padZeroes) {
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
    StringBuilder buffer = new StringBuilder();
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

  /**
   * Encrypt produces an MD5 hash of the given string.
   *
   * @param data Data that will be hashed.
   * @param shortDigest A boolean flag which determines if the full hash should
   *                    be returned or just the first 8 bytes (will return less
   *                    if the MD5 hash is shorter than 8 bytes).
   * @return Either a full MD5 hash string or a short digest <= 8 bytes in
   * length.
   * @throws IllegalArgumentException when the input data is either null or an
   * empty string.
   */
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

  // pattern to check validity of incoming ID names (non-empty alpha-numeric string + '_')
  private static final Pattern VALID_ID_PATTERN = Pattern.compile("[.\\-_a-zA-Z0-9]+");

  public static String encodeMap(Map<String,String> map) {
    return printBase64Binary(getUtf8EncodedBytes(join(mapToList(map.entrySet(), entry ->
        validateStringToEncode(entry.getKey(), "keys") + "=" +
        validateStringToEncode(entry.getValue(), "values")), "|")));
  }

  private static String validateStringToEncode(String value, String typePlural) {
    if (!VALID_ID_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Cannot encode '" + value + "'.  Only non-empty alpha-numeric " +
          typePlural + " (plus period, hyphen, underscore) allowed.");
    }
    return value;
  }

  public static Map<String,String> decodeEncodedMap(String encodedMap) {
    return getMapFromList(asList(decodeUtf8EncodedBytes(parseBase64Binary(encodedMap)).split("\\|")), pair -> {
      String[] tokens = pair.split("=");
      if (tokens.length != 2) {
        throw new IllegalArgumentException("Value '" + encodedMap + "' cannot be decoded.");
      }
      return new TwoTuple<>(tokens[0], tokens[1]);
    });
  }
}
