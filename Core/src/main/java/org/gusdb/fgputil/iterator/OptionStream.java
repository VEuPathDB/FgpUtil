package org.gusdb.fgputil.iterator;

import java.util.Optional;

public interface OptionStream<T> {

  Optional<T> next();

}
