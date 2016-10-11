package org.gusdb.fgputil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class IoUtil {

  private static final Logger LOG = Logger.getLogger(IoUtil.class);

  public static final int DEFAULT_ERROR_EXIT_CODE = 2;

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

  /**
   * Recursively removes the passed directory
   * 
   * @param directory directory to remove
   * @throws IOException if unable to delete directory tree
   */
  public static void deleteDirectoryTree(Path directory) throws IOException {
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }
      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
        if (e == null) {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
        else {
          // directory iteration failed
          throw e;
        }
      }
    });
  }

  /**
   * Checks if the passed directory exists and is writable and calls
   * System.exit() with the default exit error code if not
   * 
   * @param directoryName directory to check
   * @return File object for found, writable directory
   */
  public static File getWritableDirectoryOrDie(String directoryName) {
    File f = new File(directoryName);
    if (!f.isDirectory() || !f.canWrite()) {
      System.err.println("ERROR: " + f.getAbsolutePath()
          + " is not a writable directory.");
      System.exit(DEFAULT_ERROR_EXIT_CODE);
    }
    return f;
  }

  /**
   * Checks if the passed file exists and is readable and calls
   * System.exit() with the default exit error code if not
   * 
   * @param fileName directory to check
   * @return File object for found, writable directory
   */
  public static File getReadableFileOrDie(String fileName) {
    File f = new File(fileName);
    if (!f.isFile() || !f.canRead()) {
      System.err.println("ERROR: " + f.getAbsolutePath()
          + " is not a readable file.");
      System.exit(DEFAULT_ERROR_EXIT_CODE);
    }
    return f;
  }

  /**
   * Tries to close each of the passed Closeables, but does not throw error if
   * the close does not succeed.  Also ignores nulls.
   * 
   * @param closeable array of closable objects
   */
  public static void closeQuietly(Closeable... closeable) {
    for (Closeable each : closeable) {
      try { if (each != null) each.close(); } catch (Exception ex) { /* do nothing */ }
    }
  }

  /**
   * Transfers data from input stream to the output stream until no more data
   * is available, then closes input stream (but not output stream).
   * 
   * @param outputStream output stream data is written to
   * @param inputStream input stream data is read from
   * @throws IOException if problem reading/writing data occurs
   */
  public static void transferStream(OutputStream outputStream, InputStream inputStream)
      throws IOException {
    try {
      byte[] buffer = new byte[1024]; // send 1kb at a time
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    }
    finally {
      // only close input stream; container will close output stream
      inputStream.close();
    }
  }

  /**
   * Serializes a serializable object into a byte array and returns it
   * 
   * @param obj object to be serialized
   * @return serialized object
   * @throws IOException if unable to serialize
   */
  public static byte[] serialize(Serializable obj) throws IOException {
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         ObjectOutputStream objStream = new ObjectOutputStream(byteStream)) {
      objStream.writeObject(obj);
      return byteStream.toByteArray();
    }
  }

  /**
   * Deserializes a byte array into a Java object.
   * 
   * @param bytes serialized object
   * @return serializable object built from the passed bytes
   * @throws IOException if unable to convert bytes to object
   * @throws ClassNotFoundException if serialized object's class cannot be
   * found in the current classpath
   */
  public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
    try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
         ObjectInputStream objStream = new ObjectInputStream(byteStream)) {
      return objStream.readObject();
    }
  }

  /**
   * Read all available characters from the passed Reader and return them as a
   * String.
   * 
   * @param charReader Reader from which to read chars
   * @return String containing chars read from reader
   * @throws IOException if unable to read chars
   */
  public static String readAllChars(Reader charReader) throws IOException {
    if (charReader == null) return null;
    StringBuilder buffer = new StringBuilder();
    int c;
    while ((c = charReader.read()) > -1) {
      buffer.append((char)c);
    }
    return buffer.toString();
  }

  /**
   * Reads all available bytes from the passed input stream and returns them as
   * a byte array.
   * 
   * @param inputStream stream from which to read bytes
   * @return byte array containing bytes read from stream
   * @throws IOException if unable to read bytes
   */
  public static byte[] readAllBytes(InputStream inputStream) throws IOException {
    if (inputStream == null) return null;
    ByteArrayOutputStream byteCollector = new ByteArrayOutputStream();
    transferStream(byteCollector, inputStream);
    return byteCollector.toByteArray();
  }

  /**
   * Opens a series of files and places readers of them into an AutoCloseableList.  If
   * any of the files are unopenable for read, any already opened readers are closed and
   * an exception is thrown.
   * 
   * @param files list of paths of files to be opened for reading
   * @return list of readers that can be closed together
   * @throws FileNotFoundException if unable to open any of the files for read
   */
  public static AutoCloseableList<BufferedReader> openFilesForRead(List<Path> files) throws FileNotFoundException {
    AutoCloseableList<BufferedReader> list = new AutoCloseableList<>();
    try {
      for (Path p : files) {
        list.add(new BufferedReader(new FileReader(p.toFile())));
      }
      return list;
    }
    catch (FileNotFoundException e) {
      list.close();
      throw e;
    }
  }

  /**
   * Opens a series of files and places writers to them into an AutoCloseableList.  If
   * any of the files are unopenable for write, any already opened writers are closed and
   * an exception is thrown.
   * 
   * @param files list of paths of files to be opened for write
   * @return list of writers that can be closed together
   * @throws IOException if unable to open any of the files for write
   */
  public static AutoCloseableList<BufferedWriter> openFilesForWrite(List<Path> files) throws IOException {
    AutoCloseableList<BufferedWriter> list = new AutoCloseableList<>();
    try {
      for (Path p : files) {
        list.add(new BufferedWriter(new FileWriter(p.toFile())));
      }
      return list;
    }
    catch (IOException e) {
      list.close();
      throw e;
    }
  }

  /**
   * Create a directory at the given path and open rwx perms to all
   * 
   * @param directory path to directory
   * @throws IOException if unable to create directory or apply permissions
   */
  public static void createOpenPermsDirectory(Path directory) throws IOException {
    Files.createDirectory(directory);
    // apply file permissions after the fact in case umask restrictions prevent it during creation
    Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");
    try {
      Files.setPosixFilePermissions(directory, perms);
    }
    catch (UnsupportedOperationException ex) {
      // ignore it since it's not supported on Windows
      LOG.warn("Cannot set permissions to " + directory);
    }
  }

  public static Path createOpenPermsTempDir(String dirPrefix) throws IOException {
    return createOpenPermsTempDir(dirPrefix, null);
  }

  public static Path createOpenPermsTempDir(String dirPrefix, Path parentDir) throws IOException {
    Path resultingPath = Files.createTempDirectory(parentDir, dirPrefix);
    Files.setPosixFilePermissions(resultingPath, PosixFilePermissions.fromString("rwxrwxrwx"));
    return resultingPath;
  }
}
