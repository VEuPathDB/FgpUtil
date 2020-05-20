package org.gusdb.fgputil;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

public class AutoCloseableListTest {

  private static Logger LOG = Logger.getLogger(AutoCloseableListTest.class);

  private static class CloseLogger implements AutoCloseable {
    @Override
    public void close() throws Exception {
      LOG.info("Closing me!!");
    }
  }

  @Test
  public void basicTest() {
    AutoCloseableList<CloseLogger> list = new AutoCloseableList<>();
    System.out.println("Test 1");
    for (int i = 0; i < 5; i++) {
      list.add(new CloseLogger());
    }
    try (AutoCloseableList<CloseLogger> tester = list) {
      // do nothing; just testing close
    }
  }

  @Test
  public void constructorTest() {
    List<CloseLogger> list = new ArrayList<>();
    System.out.println("Test 2");
    for (int i = 0; i < 5; i++) {
      list.add(new CloseLogger());
    }
    try (AutoCloseableList<CloseLogger> tester = new AutoCloseableList<>(list)) {
      // do nothing; just testing close
    }
  }
}
