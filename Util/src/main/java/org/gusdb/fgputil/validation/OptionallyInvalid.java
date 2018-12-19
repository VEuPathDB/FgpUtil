package org.gusdb.fgputil.validation;

import java.util.function.Function;

import org.gusdb.fgputil.functional.Either;
import org.gusdb.fgputil.validation.ValidObjectFactory.Valid;

public class OptionallyInvalid<T extends Validateable<T>, S extends Valid<T>> extends Either<S, T> {

  public OptionallyInvalid(S successValue, T value) {
    super(successValue, value);
  }

  public <R extends Throwable> S getOrThrow(
      Function<T, R> exceptionSupplier) throws R {
    return left().orElseThrow(() -> exceptionSupplier.apply(getRight()));
  }
}
