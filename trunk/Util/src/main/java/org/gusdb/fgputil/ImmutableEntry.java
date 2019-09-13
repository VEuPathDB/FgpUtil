package org.gusdb.fgputil;

import java.util.Map.Entry;

public class ImmutableEntry<T,S> implements Entry<T,S> {

  private final T _key;
  private final S _value;

  public ImmutableEntry(T key, S value) {
    _key = key;
    _value = value;
  }

  @Override
  public T getKey() {
    return _key;
  }

  @Override
  public S getValue() {
    return _value;
  }

  @Override
  public S setValue(S value) {
    throw new UnsupportedOperationException("Attempt made to change value of immutable map entry.");
  }

}
