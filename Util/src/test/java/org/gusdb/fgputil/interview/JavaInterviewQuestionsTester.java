package org.gusdb.fgputil.interview;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.gusdb.fgputil.UnitTestBase;
import org.junit.Test;

public class JavaInterviewQuestionsTester extends UnitTestBase {
	
	private static final String REVERSABLE_FILE_NAME = "org/gusdb/fgputil/test/reversableTextFile.txt";
	private static final String REVERSED_FILE_NAME_CORRECT = "org/gusdb/fgputil/test/reversedTextFile.txt";
	private static final String REVERSED_FILE_PATH = "/tmp/reversedTextFile.txt";
	
	@Test
	public void testPalindrome() {
		JavaInterviewQuestions q = new JavaInterviewQuestions();
		assertFalse(q.isPalindrome(null));
		assertTrue(q.isPalindrome(""));
		assertTrue(q.isPalindrome("aba"));
		assertTrue(q.isPalindrome("abba"));
		assertTrue(q.isPalindrome("quvicoekeocivuq"));
		assertFalse(q.isPalindrome("abca"));
		assertFalse(q.isPalindrome("uiopoui"));
		assertFalse(q.isPalindrome("yuijuhiuy"));
	}

	@Test
	public void testNearPalindrome() {
		JavaInterviewQuestions q = new JavaInterviewQuestions();
		assertFalse(q.isNearPalindrome(null, 500));
		assertTrue(q.isNearPalindrome("", 0));
		assertTrue(q.isNearPalindrome("aba", 0));
		assertTrue(q.isNearPalindrome("abba", 5));
		assertTrue(q.isNearPalindrome("quvicoekeocivuq", -5));
		assertFalse(q.isNearPalindrome("abca", 0));
		assertTrue(q.isNearPalindrome("abca", 1));
		assertFalse(q.isNearPalindrome("abcd", 1));
		assertTrue(q.isNearPalindrome("abcd", 2));
		assertTrue(q.isNearPalindrome("abcda", 1));
		assertFalse(q.isNearPalindrome("uiopoui", 1));
		assertTrue(q.isNearPalindrome("uiopoui", 2));
		assertFalse(q.isNearPalindrome("yuijuhiuy",0));
		assertTrue(q.isNearPalindrome("yuijuhiuy",1));
		assertFalse(q.isNearPalindrome("abracadabra",2));
		assertTrue(q.isNearPalindrome("abracadabra",3));
		assertTrue(q.isNearPalindrome("abracadabra",20));
	}
	
	@Test
	public void testReverseFile() {
		JavaInterviewQuestions q = new JavaInterviewQuestions();
		try {
			String inputFilePath = getResourceFilePath(REVERSABLE_FILE_NAME);
			String verificationFilePath = getResourceFilePath(REVERSED_FILE_NAME_CORRECT);
			
			// easy way
			q.reverseFile(inputFilePath, REVERSED_FILE_PATH);
			assertFilesEqual(REVERSED_FILE_PATH, verificationFilePath);
			new File(REVERSED_FILE_PATH).delete();
			
			// memory-safe way
			q.reverseFileMemSafe(inputFilePath, REVERSED_FILE_PATH);
			assertFilesEqual(REVERSED_FILE_PATH, verificationFilePath);
			new File(REVERSED_FILE_PATH).delete();
		}
		catch (IOException ioe) {
			throw new RuntimeException("I/O Error", ioe);
		}
	}
	
	@Test
	public void testUniqueChars() {
		JavaInterviewQuestions q = new JavaInterviewQuestions();
		String[][] testSet = {
				{ "tttttthhhhthhhthhth", "ht" },
				{ "aniapoisydflkasndoipvine", "adefiklnopsvy" },
				{ "abddebcaecd", "abcde" },
				{ "fasd", "adfs" }
		};
		for (String[] testPair : testSet) {
			assertEquals(q.getUniqueChars(testPair[0]), testPair[1]);
			assertEquals(q.getUniqueCharsBetter(testPair[0]), testPair[1]);
		}
	}
}
