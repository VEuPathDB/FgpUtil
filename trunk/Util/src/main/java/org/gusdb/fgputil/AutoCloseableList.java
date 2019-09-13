package org.gusdb.fgputil;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * Provides an implementation of List for AutoCloseables that is AutoCloseable itself and
 * closes all its members on close.  It can be used as a normal list (ArrayList implementation)
 * or you can construct as needed from any existing Collection<AutoCloseable>.
 * 
 * @author rdoherty
 */
public class AutoCloseableList<T extends AutoCloseable> extends ArrayList<T> implements AutoCloseable {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(AutoCloseableList.class);

  public AutoCloseableList() {
    super();
  }

  public AutoCloseableList(Collection<T> source) {
    super();
    addAll(source);
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
