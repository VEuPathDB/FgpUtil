package org.gusdb.fgputil;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

public class IoUtil {
  
  /**
   * Converts binary data into an input stream.  This can be used if the result
   * type is a stream, and the content to be returned already exists in memory
   * as a string.  This is simply a wrapper around the ByteArrayInputStream
   * constructor.
   * 
   * @param data data to be converted
   * @return stream representing the data
   */
  public static InputStream getStreamFromBytes(byte[] data) {
    return new ByteArrayInputStream(data);
  }
  
  /**
   * Converts a string into an open input stream.  This can be used if the
   * result type is a stream, and the content to be returned already exists in
   * memory as a string.
   * 
   * @param str string to be converted
   * @return input stream representing the string
   */
  public static InputStream getStreamFromString(String str) {
    return getStreamFromBytes(str.getBytes(Charset.defaultCharset()));
  }
  
  public static void deleteDir(File dir) {
    if (dir.exists()) {
      for (File f : dir.listFiles()) {
        if (f.isDirectory())
          deleteDir(f);
        else
          f.delete();
      }
      dir.delete();
    }
  }

  public static File getWritableDirectoryOrDie(String directoryName) {
    File f = new File(directoryName);
    if (!f.isDirectory() || !f.canWrite()) {
      System.err.println("ERROR: " + f.getAbsolutePath()
          + " is not a writable directory.");
      System.exit(2);
    }
    return f;

  }

  public static File getReadableFileOrDie(String fileName) {
    File f = new File(fileName);
    if (!f.isFile() || !f.canRead()) {
      System.err.println("ERROR: " + f.getAbsolutePath()
          + " is not a readable file.");
      System.exit(2);
    }
    return f;
  }
  
  public static void closeQuietly(Closeable... closeable) {
    for (Closeable each : closeable) {
      try { if (closeable != null) each.close(); } catch (Exception ex) { /* do nothing */ }
    }
  }
}
