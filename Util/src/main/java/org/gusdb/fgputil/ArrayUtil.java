package org.gusdb.fgputil;

import java.util.Arrays;

public class ArrayUtil {

  /**
   * Concatenates the contents of a series of arrays into a single array.  This
   * implementation uses generics to allow type-safe concatenation of arrays
   * of any type.
   * 
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

}
