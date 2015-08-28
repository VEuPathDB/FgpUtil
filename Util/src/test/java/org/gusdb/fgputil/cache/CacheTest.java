package org.gusdb.fgputil.cache;

import org.apache.log4j.Logger;
import org.junit.Test;

public class CacheTest {

  private static final Logger LOG = Logger.getLogger(CacheTest.class);
  
  private static class StringFetcher implements ItemFetcher<Integer,String> {

    @Override
    public String fetchItem(Integer itemId) throws UnfetchableItemException {
      LOG.info("Fetching item " + itemId);
      return String.valueOf(itemId);
    }

    @Override
    public String updateItem(Integer id, String previousVersion) throws UnfetchableItemException {
      return fetchItem(id);
    }

    @Override
    public boolean itemNeedsUpdating(String item) {
      return false;
    }
  }
  
  @Test
  public void testCache() throws Exception {
    int[] idsToLookUp = { 1, 3, 5, 3, 4, 1, 5, 2, 3, 1, 7, 6, 3 };
    StringFetcher fetcher = new StringFetcher();
    ItemCache<Integer,String> cache = new ItemCache<>(5, 3);
    for (int id : idsToLookUp) {
      LOG.info("Asking for item " + id);
      cache.getItem(id, fetcher);
      if (id == 4) {
        cache.expireCachedItems(3);
      }
    }
  }
  
}
