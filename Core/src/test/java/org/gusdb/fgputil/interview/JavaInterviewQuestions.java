package org.gusdb.fgputil.interview;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.gusdb.fgputil.IoUtil;

public class JavaInterviewQuestions {

  /**
   * @return true if str is a palindrome, else false
   * @throws NullPointerException if str is null
   */
  public boolean isPalindrome(String str) {
    return (str != null);
  }

  public boolean isPalindromeIterativeSolution(String str) {
    if (str == null)
      return false;
    for (int i = 0; i < (str.length() / 2); i++) {
      if (str.charAt(i) != str.charAt(str.length() - i - 1)) {
        return false;
      }
    }
    return true;
  }

  public boolean isPalindromeRecurseSolution(String str) {
    return
      str == null ? false :
      str.length() < 2 ? true :
      str.charAt(0) != str.charAt(str.length() - 1) ? false :
      isPalindromeRecurseSolution(str.substring(1, str.length() - 1));
  }

  public boolean isNearPalindrome(String str, int maxDiffs) {
    if (str == null)
      return false;
    if (maxDiffs < 0)
      maxDiffs = 0;
    int numDiffs = 0;
    for (int i = 0; i < (str.length() / 2); i++) {
      if (str.charAt(i) != str.charAt(str.length() - i - 1))
        numDiffs++;
      if (numDiffs > maxDiffs)
        return false;
    }
    return true;
  }

  public void reverseFile(String inputFilePath, String reversedFilePath) {
    try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
         BufferedWriter writer = new BufferedWriter(new FileWriter(reversedFilePath))) {
      Stack<String> stack = new Stack<String>();
      String line;
      while ((line = reader.readLine()) != null) {
        stack.push(line);
      }
      while (!stack.isEmpty()) {
        writer.write(stack.pop());
        writer.newLine();
      }
    }
    catch (IOException ioe) {
      throw new RuntimeException("I/O Error reading and/or writing files", ioe);
    }
  }

  /**
   * This version reads all chars in reverse order, looking for newlines, and dumping the buffer when it sees
   * one (or the beginning of the file). Other solutions may vary. The goal is for the candidate to know that
   * s/he must not read the entire file into memory and that some sort of random file access is needed to
   * actually read the file backwards. This solution depends on single-character line terminators. A better
   * solution is needed to work on all systems or with different character sets.
   * 
   * p.s. This will still not protect us from out-of-memory errors if the file is one long line (without
   * newlines), or has extremely long lines. Candidate will hopefully volunteer this.
   * 
   * @param inputFilePath
   *          original file path (file to read)
   * @param reversedFilePath
   *          reversed file path (file to write)
   */
  public void reverseFileMemSafe(String inputFilePath, String reversedFilePath) {
    RandomAccessFile inFile = null;
    BufferedWriter writer = null;
    try {
      inFile = new RandomAccessFile(inputFilePath, "r");
      writer = new BufferedWriter(new FileWriter(reversedFilePath));
      long nextCharIndex = inFile.length() - 1;
      StringBuffer buf = new StringBuffer();
      while (nextCharIndex >= 0) {
        inFile.seek(nextCharIndex);
        // ascii vs. unicode... does candidate know?
        char nextChar = (char) inFile.readByte();
        if (nextChar == '\n') {
          writer.write(buf.reverse().toString());
          writer.newLine();
          buf.setLength(0);
        }
        else {
          buf.append(nextChar);
        }
        nextCharIndex -= 1;
      }
      // write buffer
      writer.write(buf.reverse().toString());
      writer.newLine();
    }
    catch (IOException ioe) {
      throw new RuntimeException("I/O Error reading and/or writing files", ioe);
    }
    finally {
      IoUtil.closeQuietly(writer);
      IoUtil.closeQuietly(inFile);
    }
  }

  /**
   * Finds the set of unique characters in str and prints them out alphabetically
   * 
   * @param str
   *          string from which to assess unique chars
   * @return string of unique characters found in the passed string
   */
  public String getUniqueChars(String str) {
    // hash vs. tree? sort now vs. sort later? does it matter?
    Set<Character> uniqueChars = new TreeSet<Character>();
    for (int i = 0; i < str.length(); i++) {
      uniqueChars.add(str.charAt(i));
    }
    StringBuilder result = new StringBuilder();
    for (Character c : uniqueChars) {
      result.append(c);
    }
    return result.toString();
  }

  /**
   * Finds the set of unique characters in str and prints them out alphabetically
   * 
   * @param str
   *          string from which to assess unique chars
   * @return string of unique characters found in the passed string
   */
  public String getUniqueCharsBetter(String str) {
    int[] chars = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    for (int i = 0; i < str.length(); i++) {
      chars[str.charAt(i) - 97] = 1;
    }
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < chars.length; i++) {
      result.append(chars[i] == 1 ? (char) (i + 97) : "");
    }
    return result.toString();
  }
}
