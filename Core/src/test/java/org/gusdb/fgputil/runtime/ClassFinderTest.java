package org.gusdb.fgputil.runtime;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ClassFinderTest {

  @Test
  public void doTest() {
    String testPackage = "org.gusdb.fgputil";
    List<Class<Exception>> exceptionClasses = ClassFinder.getClassesBySubtype(Exception.class, testPackage);
    // we know we have at least four exceptions in FgpUtil; may add more over time
    Assert.assertTrue(exceptionClasses.size() >= 4);
    for (Class<Exception> exceptionClass : exceptionClasses) {
      String className = exceptionClass.getName();
      Assert.assertTrue(className.startsWith(testPackage));
      System.out.println(className);
    }
  }

}
