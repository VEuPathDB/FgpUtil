package org.gusdb.fgputil;

import java.io.Closeable;
import java.io.File;

public class IoUtil {
	
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
  
  public static void closeQuietly(Closeable closeable) {
    try { if (closeable != null) closeable.close(); } catch (Exception ex) { /* do nothing */
      ex.printStackTrace();
    }
  }
}
