package org.gusdb.fgputil;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

public class ArrayUtil {

  /**
   * Concatenates the contents of a series of arrays into a single array.  This
   * implementation uses generics to allow type-safe concatenation of arrays
   * of any type.
   * 
   * @param <T> type contained in the arrays
   * @param firstArray an array
   * @param moreArrays more arrays of the same type
   * @return a new array containing all the contents of the passed arrays
   */
  @SafeVarargs
  public static <T> T[] concatenate(T[] firstArray, T[]... moreArrays) {
    int endSize = firstArray.length;
    for (T[] arr : moreArrays) {
      endSize += arr.length;
    }
    T[] combinedArray = Arrays.copyOf(firstArray, endSize);
    int nextIndex = firstArray.length;
    for (T[] arr : moreArrays) {
      System.arraycopy(arr, 0, combinedArray, nextIndex, arr.length);
      nextIndex += arr.length;
    }
    return combinedArray;
  }

  /**
   * Returns a copy of array with eacy value in values appended to the end, in order.
   * 
   * @param <T> type contained in the arrays
   * @param array base array
   * @param values values to be appended
   * @return new array with values appended
   */
  @SafeVarargs
  public static <T> T[] append(T[] array, T... values) {
    return concatenate(array, values);
  }
  
  /**
   * Returns a copy of the passed array with the passed values inserted at the
   * index specified.  Note: while still O(N), this method executes two passes
   * through the array.
   * 
   * @param <T> type contained in the arrays
   * @param array base array
   * @param index index at which to insert values
   * @param values values to be inserted
   * @return new array with values inserted
   */
  @SafeVarargs
  public static <T> T[] insert(T[] array, int index, T... values) {
    T[] begin = Arrays.copyOfRange(array, 0, index);
    T[] end = Arrays.copyOfRange(array, index, array.length);
    return concatenate(begin, values, end);
  }

  /**
   * Converts a string into an immutable list of characters.
   * 
   * @param string
   * @return list of characters in the string
   */
  public static List<Character> asList(final String string) {
    return new AbstractList<Character>() {
       @Override public int size() { return string.length(); }
       @Override public Character get(int index) { return string.charAt(index); }
    };
  }
}
