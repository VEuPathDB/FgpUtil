package org.gusdb.fgputil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtil {
	
	public static String encrypt(String data) throws Exception,
			NoSuchAlgorithmException {
		// cannot encrypt null value
		if (data == null || data.length() == 0)
			throw new Exception("Cannot encrypt an empty/null string");

		MessageDigest digest = MessageDigest.getInstance("MD5");
		byte[] byteBuffer = digest.digest(data.toString().getBytes());
		// convert each byte into hex format
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < byteBuffer.length; i++) {
			int code = (byteBuffer[i] & 0xFF);
			if (code < 0x10)
				buffer.append('0');
			buffer.append(Integer.toHexString(code));
		}
		return buffer.toString();
	}
}
