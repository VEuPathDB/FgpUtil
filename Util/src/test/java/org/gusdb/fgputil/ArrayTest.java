package org.gusdb.fgputil;

import java.util.Arrays;

import org.junit.Test;

/**
 * This class tests the methods in ArrayUtil, but also tests the performance of
 * a variety of array concatenation techniques.
 * 
 * @author rdoherty
 */
public class ArrayTest {

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

      System.out.println("For total array size: " + (size*2) + ":");
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
    System.out.println (runTitle + " took " + time + "ms");
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
        System.out.println (runTitle + " took " + time + "ms");
        break;
      case Arraycopy:
        time = System.currentTimeMillis();
        String[] combined2 = new String[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, combined2, 0, arr1.length);
        System.arraycopy(arr2, 0, combined2, arr1.length, arr2.length);
        time = System.currentTimeMillis() - time;
        System.out.println (runTitle + " took " + time + "ms");
        break;
      case FgpUtil:
        time = System.currentTimeMillis();
        @SuppressWarnings("unused")
        String[] combined3 = ArrayUtil.concatenate(arr1, arr2);
        time = System.currentTimeMillis() - time;
        System.out.println (runTitle + " took " + time + "ms");
    }
  }
}
