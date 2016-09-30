package org.gusdb.fgputil;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

public class AutoCloseableList<T extends AutoCloseable> extends ArrayList<T> implements AutoCloseable {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(AutoCloseableList.class);

  public AutoCloseableList() {
    super();
  }

  public AutoCloseableList(Collection<T> source) {
    super();
    for (T obj : source) {
      add(obj);
    }
  }

  @Override
  public void close() {
    for (AutoCloseable item : this) {
      try {
        item.close();
      }
      catch (Exception e) {
        LOG.error("Unable to close item in AutoCloseableList", e);
      }
    }
  }
}
