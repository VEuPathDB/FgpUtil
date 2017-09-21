package org.gusdb.fgputil.cache;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.junit.Test;

public class CacheTest {

  private static final Logger LOG = Logger.getLogger(CacheTest.class);
  private static final boolean LOG_ON = false;

  private static class StringFetcher extends NoUpdateItemFetcher<Integer,String> {

    private final List<String> _opOrder;

    public StringFetcher(List<String> opOrder) {
      _opOrder = opOrder;
    }

    @Override
    public String fetchItem(Integer itemId) throws UnfetchableItemException {
      if (LOG_ON) LOG.info("Fetching item " + itemId);
      _opOrder.add("b" + itemId);
      return String.valueOf(itemId);
    }
  }

  private class Worker implements Runnable {

    private final ItemCache<Integer, String> _cache;
    private final List<String> _opOrder;
    private final AtomicInteger _completedCount;

    public Worker(ItemCache<Integer, String> cache, List<String> opOrder, AtomicInteger completedCount) {
      _cache = cache;
      _opOrder = opOrder;
      _completedCount = completedCount;
    }

    @Override
    public void run() {
      int[] idsToLookUp = { 1, 3, 5, 3, 4, 1, 5, 2, 3, 1, 7, 6, 3 };
      StringFetcher fetcher = new StringFetcher(_opOrder);
      for (int id : idsToLookUp) {
        if (LOG_ON) LOG.info("Asking for item " + id);
        _opOrder.add("a" + id);
        try {
          _cache.getItem(id, fetcher);
        }
        catch (UnfetchableItemException e) {
          // should never happen
          LOG.error("Error fetching", e);
        }
        int size = _cache.getSize();
        if (LOG_ON) LOG.info("Current size: " + size);
        _opOrder.add("s" + size);
        if (id == 4) {
          _cache.expireCachedItems(3);
          size = _cache.getSize();
          if (LOG_ON) LOG.info("Current size: " + size);
          _opOrder.add("s" + size);
        }
      }
      _completedCount.incrementAndGet();
    }
  }

  @Test
  public void singleThreadTest() {
    LOG.info("Starting single thread test");
    ItemCache<Integer,String> cache = new ItemCache<>(5, 3);
    List<String> opOrder = new Vector<>();
    AtomicInteger completedCount = new AtomicInteger(0);
    Executors.newSingleThreadExecutor().execute(new Worker(cache, opOrder, completedCount));
    while (completedCount.get() < 1) {}
    String expected = "[ a1,b1,s1,a3,b3,s2,a5,b5,s3,a3,s3,a4,b4,s4,s3,a1,s3,a5,s3,a2,b2,s4,a3,b3,s5,a1,s5,a7,b7,s3,a6,b6,s4,a3,s4 ]";
    String actual = FormatUtil.arrayToString(opOrder.toArray(), ",");
    LOG.info("Result: " + actual);
    assertEquals(expected, actual);
  }
  
  @Test
  public void multiThreadTest() {
    LOG.info("Starting multi-thread test");
    int numThreads = 100;
    ItemCache<Integer,String> cache = new ItemCache<>(4, 2);
    List<String> opOrder = new Vector<>();
    AtomicInteger completedCount = new AtomicInteger(0);
    Executor exec = Executors.newFixedThreadPool(10);
    for (int i = 0; i < numThreads; i++) {
      exec.execute(new Worker(cache, opOrder, completedCount));
    }
    while (completedCount.get() < numThreads) {}
    LOG.info("Number of ops: " + opOrder.size());
  }
}
