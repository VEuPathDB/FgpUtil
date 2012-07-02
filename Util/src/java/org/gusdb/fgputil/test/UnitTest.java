package org.gusdb.fgputil.test;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple unit test framework (replace with JUnit as soon as is convenient)
 * 
 * @author rdoherty
 */
public class UnitTest {

	private static List<String> _errors = new ArrayList<String>();

	// some simple asserts rather than include jUnit
	protected static void assertTrue(boolean b) { if (!b) logFail(); }
	protected static void assertFalse(boolean b) { if (b) logFail(); }
	protected static void assertEqual(int a, int b) { if (a != b) logFail(); }
	protected static void assertUnequal(int a, int b) { if (a == b) logFail(); }
	protected static void assertEqual(Object a, Object b) { if (!a.equals(b)) logFail(); }
	protected static void assertUnequal(Object a, Object b) { if (a.equals(b)) logFail(); }
	protected static void assertEqual(String a, String b) { if (!a.equals(b)) logFail("\"" + a + "\" != \"" + b + "\""); }
	protected static void assertUnequal(String a, String b) { if (a.equals(b)) logFail("\"" + a + "\" == \"" + b + "\""); }

	protected static void assertFilesEqual(String filePath1, String filePath2) throws IOException {
		BufferedReader br1 = null, br2 = null;
		try {
			br1 = new BufferedReader(new FileReader(filePath1));
			br2 = new BufferedReader(new FileReader(filePath2));
			while (br1.ready()) {
				if (!br2.ready()) {
					logFail(filePath2 + " ended before " + filePath1);
					return;
				}
				String line1 = br1.readLine();
				String line2 = br2.readLine();
				//System.out.println("Comparing <" + line1 + "> with <" + line2 + ">.");
				if ((line1 == null && line2 != null) ||
				    (line1 != null && !line1.equals(line2))) {
					logFail("Files differ.");
					return;
				}
			}
			if (br2.ready()) {
				logFail(filePath1 + " ended before " + filePath2);
				return;
			}
		}
		finally {
			closeQuietly(br2);
			closeQuietly(br1);
		}
	}
	
	private static void logFail() {
		StackTraceElement ste = new Exception().getStackTrace()[2];
		_errors.add("Assert failed at: " + ste.getFileName() + ": line " + ste.getLineNumber());
	}
	
	private static void logFail(String msg) {
		StackTraceElement ste = new Exception().getStackTrace()[2];
		_errors.add("Assert failed at: " + ste.getFileName() + ": line " + ste.getLineNumber() + ": " + msg);
	}
	
	protected void printResults() {
		if (_errors.isEmpty()) {
			System.err.println("All tests successful!");
			return;
		}
		System.err.println("Some tests failed:");
		for (String s : _errors) {
			System.err.println(s);
		}
	}

	/**
	 * @param resourcePath resource name (including package, i.e. org.gusdb.fgputil.test.TestFile.txt)
	 * @return absolute path to the file
	 * @throws FileNotFoundException 
	 */
	public static String getResourceFilePath(String resourcePath) throws FileNotFoundException {
		URL url = UnitTest.class.getClassLoader().getResource(resourcePath);
		if (url == null) {
			throw new FileNotFoundException("Resource cannot be found on the classpath: " + resourcePath);
		}
		return url.getFile();
	}
	
	public static void closeQuietly(Closeable closeable) {
		try { if (closeable != null) closeable.close(); } catch (Exception e) { /* do nothing */ }
	}
}
