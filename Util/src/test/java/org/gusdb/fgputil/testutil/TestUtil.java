package org.gusdb.fgputil.testutil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.sql.DataSource;

import org.gusdb.fgputil.IoUtil;
import org.hsqldb.jdbc.JDBCDataSource;

/**
 * Unit test utilities
 * 
 * @author rdoherty
 */
public final class TestUtil {

  private TestUtil() { }
  
	public static void assertFilesEqual(String filePath1, String filePath2) throws IOException {
		BufferedReader br1 = null, br2 = null;
		try {
			br1 = new BufferedReader(new FileReader(filePath1));
			br2 = new BufferedReader(new FileReader(filePath2));
			while (br1.ready()) {
				if (!br2.ready()) {
					throw new AssertionError(filePath2 + " ended before " + filePath1);
				}
				String line1 = br1.readLine();
				String line2 = br2.readLine();
				//System.out.println("Comparing <" + line1 + "> with <" + line2 + ">.");
				if ((line1 == null && line2 != null) ||
				    (line1 != null && !line1.equals(line2))) {
					throw new AssertionError("Files differ.");
				}
			}
			if (br2.ready()) {
				throw new AssertionError(filePath1 + " ended before " + filePath2);
			}
		}
		finally {
			IoUtil.closeQuietly(br2);
			IoUtil.closeQuietly(br1);
		}
	}

	/**
	 * @param resourcePath resource name (including package, i.e. org.gusdb.fgputil.test.TestFile.txt)
	 * @return absolute path to the file
	 * @throws FileNotFoundException 
	 */
	public static String getResourceFilePath(String resourcePath) throws FileNotFoundException {
		URL url = TestUtil.class.getClassLoader().getResource(resourcePath);
		if (url == null) {
			throw new FileNotFoundException("Resource cannot be found on the classpath: " + resourcePath);
		}
		return url.getFile();
	}
	
	/**
	 * Establishes an in-memory database (currently HSQL) and returns a Java
	 * DataSource object representing it.
	 * 
	 * @return
	 */
  public static DataSource getTestDataSource(String name) {
    JDBCDataSource ds = new JDBCDataSource();
    ds.setDatabase("jdbc:hsqldb:mem:"+name);
    ds.setUser("SA");
    ds.setPassword("");
    return ds;
  }
}
