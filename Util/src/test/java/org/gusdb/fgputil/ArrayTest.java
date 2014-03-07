package org.gusdb.fgputil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;

/**
 * This class tests the methods in ArrayUtil, but also tests the performance of
 * a variety of array concatenation techniques.
 * 
 * @author rdoherty
 */
public class ArrayTest {
  
  private String[] S_EMPTY = new String[]{};
  private String[] S_SINGLE = new String[]{ "a" };
  private String[] S_SOME = new String[]{ "b", "c", "d", "e" };
  private String[] S_NULLS = new String[]{ null, null };

  private String[][] S_CASES = new String[][]{ S_EMPTY, S_SINGLE, S_SOME, S_NULLS };
  
  @Test
  public void testStrAppend() throws Exception {
    for (String[] array : S_CASES) {
      testStrCase(ArrayUtil.append(array), array, S_EMPTY);
      testStrCase(ArrayUtil.append(array, "a"), array, S_SINGLE);
      testStrCase(ArrayUtil.append(array, "b", "c", "d", "e"), array, S_SOME);
      testStrCase(ArrayUtil.append(array, null, null), array, S_NULLS);
    }
  }
  
  @Test
  public void testStrConcatenate() throws Exception {
    for (int i = 0; i < S_CASES.length; i++) {
      testStrCase(ArrayUtil.concatenate(S_CASES[i]), S_CASES[i]);
      for (int j = 0; j < S_CASES.length; j++) {
        testStrCase(ArrayUtil.concatenate(S_CASES[i], S_CASES[j]), S_CASES[i], S_CASES[j]);
        for (int k = 0; k < S_CASES.length; k++) {
          testStrCase(ArrayUtil.concatenate(S_CASES[i], S_CASES[j], S_CASES[k]), S_CASES[i], S_CASES[j], S_CASES[k]);
        }
      }
    }
  }
  
  @Test
  public void testStrInsert() throws Exception {
    for (String[] begin : S_CASES) {
      for (String[] end : S_CASES) {
        testStrCase(ArrayUtil.insert(ArrayUtil.concatenate(begin, end), begin.length), begin, S_EMPTY, end);
        testStrCase(ArrayUtil.insert(ArrayUtil.concatenate(begin, end), begin.length, "a"), begin, S_SINGLE, end);
        testStrCase(ArrayUtil.insert(ArrayUtil.concatenate(begin, end), begin.length, "b", "c", "d", "e"), begin, S_SOME, end);
        testStrCase(ArrayUtil.insert(ArrayUtil.concatenate(begin, end), begin.length, null, null), begin, S_NULLS, end);
      }
    }
  }

  private void testStrCase(String[] result, String[]... arraysToCat) {
    // make sure the result contains the same elements as all the concatenated arrays
    int index = 0;
    for (String[] array : arraysToCat) {
      for (int i = 0; i < array.length; i++, index++) {
        if (array[i] == null)
          assertNull(result[index]);
        else
          assertEquals(array[i], result[index]);
      }
    }
  }
  
  /****************************************************************************/
  
  private Integer[] I_EMPTY = new Integer[]{};
  private Integer[] I_SINGLE = new Integer[]{ 1 };
  private Integer[] I_SOME = new Integer[]{ 2, 3, 4, 5 };
  
  private Integer[][] I_CASES = new Integer[][]{ I_EMPTY, I_SINGLE, I_SOME };
  
  @Test
  public void testIntAppend() throws Exception {
    for (Integer[] array : I_CASES) {
      testIntCase(ArrayUtil.append(array), array, I_EMPTY);
      testIntCase(ArrayUtil.append(array, 1), array, I_SINGLE);
      testIntCase(ArrayUtil.append(array, 2, 3, 4, 5), array, I_SOME);
    }
  }
  
  @Test
  public void testIntConcatenate() throws Exception {
    for (int i = 0; i < I_CASES.length; i++) {
      testIntCase(ArrayUtil.concatenate(I_CASES[i]), I_CASES[i]);
      for (int j = 0; j < I_CASES.length; j++) {
        testIntCase(ArrayUtil.concatenate(I_CASES[i], I_CASES[j]), I_CASES[i], I_CASES[j]);
        for (int k = 0; k < I_CASES.length; k++) {
          testIntCase(ArrayUtil.concatenate(I_CASES[i], I_CASES[j], I_CASES[k]), I_CASES[i], I_CASES[j], I_CASES[k]);
        }
      }
    }
  }
  
  @Test
  public void testIntInsert() throws Exception {
    for (Integer[] begin : I_CASES) {
      for (Integer[] end : I_CASES) {
        testIntCase(ArrayUtil.insert(ArrayUtil.concatenate(begin, end), begin.length), begin, I_EMPTY, end);
        testIntCase(ArrayUtil.insert(ArrayUtil.concatenate(begin, end), begin.length, 1), begin, I_SINGLE, end);
        testIntCase(ArrayUtil.insert(ArrayUtil.concatenate(begin, end), begin.length, 2, 3, 4, 5), begin, I_SOME, end);
      }
    }
  }
  
  private void testIntCase(Integer[] result, Integer[]... arraysToCat) {
    // make sure the result contains the same elements as all the concatenated arrays
    int index = 0;
    for (Integer[] array : arraysToCat) {
      for (int i = 0; i < array.length; i++, index++) {
        assertEquals(array[i], result[index]);
      }
    }
  }

  /*****************************************************************************
   * 
   * The code below does not test the FgpUtil arrays class but compares the
   * performance of various ways to concatenate arrays.
   * 
   ****************************************************************************/
  
  private static final boolean DISPLAY_TIMES = false;
  
  private final int[] ARRAY_SIZES =
      new int[]{ 5, 50, 500, 5000, 50000, 500000, 5000000 };
  
  private interface ArrayCombiner {
    public <T> T[] combine(T[] arr1, T[] arr2);
  }
  
  private static class CombineArraysWithListBuilder implements ArrayCombiner {
    @SuppressWarnings("unchecked")
    @Override public <T> T[] combine(T[] arr1, T[] arr2) {
      return (T[]) new ListBuilder<T>().addAll(Arrays.asList(arr1))
          .addAll(Arrays.asList(arr2)).toList().toArray(new Object[arr1.length+arr2.length]);
    }
  }
  
  private static class CombineArraysWithArraycopy implements ArrayCombiner {
    @SuppressWarnings("unchecked")
    @Override public <T> T[] combine(T[] arr1, T[] arr2) {
      T[] combined = (T[]) new Object[arr1.length + arr2.length];
      System.arraycopy(arr1, 0, combined, 0, arr1.length);
      System.arraycopy(arr2, 0, combined, arr1.length, arr2.length);
      return combined;
    }
  }
  
  private enum Technique { ListBuilder, Arraycopy, FgpUtil }
  
  @Test
  public void concatenationSpeedTest() throws Exception {
    for (int size: ARRAY_SIZES) {
      final String[] blah1 = new String[size];
      final String[] blah2 = new String[size];
      Arrays.fill(blah1, "a");
      Arrays.fill(blah2, "b");

      if (DISPLAY_TIMES) System.err.println("For total array size: " + (size*2) + ":");
      printCombineTime("  Run #1", blah1, blah2, new CombineArraysWithListBuilder());
      printCombineTime("  Run #2", blah1, blah2, new CombineArraysWithArraycopy());
      printCombineTime("  Run #3", blah1, blah2, Technique.ListBuilder);
      printCombineTime("  Run #4", blah1, blah2, Technique.Arraycopy);
      printCombineTime("  Run #5", blah1, blah2, Technique.FgpUtil);
    }
  }

  private <T> void printCombineTime(String runTitle, T[] arr1, T[] arr2, ArrayCombiner arrayCombiner) {
    long time = System.currentTimeMillis();
    arrayCombiner.combine(arr1, arr2);
    time = System.currentTimeMillis() - time;
    if (DISPLAY_TIMES) System.err.println(runTitle + " took " + time + "ms");
  }
  
  private void printCombineTime(String runTitle, String[] arr1, String[] arr2, Technique technique) {
    long time;
    switch (technique) {
      case ListBuilder:
        time = System.currentTimeMillis();
        @SuppressWarnings("unused")
        String[] combined1 = new ListBuilder<String>().addAll(Arrays.asList(arr1))
            .addAll(Arrays.asList(arr2)).toList().toArray(new String[arr1.length+arr2.length]);
        time = System.currentTimeMillis() - time;
        if (DISPLAY_TIMES) System.err.println(runTitle + " took " + time + "ms");
        break;
      case Arraycopy:
        time = System.currentTimeMillis();
        String[] combined2 = new String[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, combined2, 0, arr1.length);
        System.arraycopy(arr2, 0, combined2, arr1.length, arr2.length);
        time = System.currentTimeMillis() - time;
        if (DISPLAY_TIMES) System.err.println (runTitle + " took " + time + "ms");
        break;
      case FgpUtil:
        time = System.currentTimeMillis();
        @SuppressWarnings("unused")
        String[] combined3 = ArrayUtil.concatenate(arr1, arr2);
        time = System.currentTimeMillis() - time;
        if (DISPLAY_TIMES) System.err.println (runTitle + " took " + time + "ms");
    }
  }
}
