package org.gusdb.fgputil;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

public class AutoCloseableListTest {

  private static final Logger LOG = Logger.getLogger(AutoCloseableListTest.class);

  private static class CloseLogger implements AutoCloseable {
    @Override
    public void close() {
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
    //noinspection EmptyTryBlock
    try (AutoCloseableList<CloseLogger> ignored = list) {
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
    //noinspection EmptyTryBlock
    try (AutoCloseableList<CloseLogger> ignored = new AutoCloseableList<>(list)) {
      // do nothing; just testing close
    }
  }
}
