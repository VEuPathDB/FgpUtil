package org.gusdb.fgputil.validation;

/**
 * Provides a way to declare that an object is valid or invalid.
 * 
 * @author rdoherty
 *
 */
public class ValidObjectFactory {

  public static abstract class Valid<T extends Validateable<T>> {

    private final T _validatedObject;

    private Valid(T validatedObject) {
      _validatedObject = validatedObject;
    }

    public T get() {
      return _validatedObject;
    }
  }

  public static class SyntacticallyValid<T extends Validateable<T>> extends Valid<T> {
    private SyntacticallyValid(T validatedObject) {
      super(validatedObject);
    }
  }

  public static class DisplayablyValid<T extends Validateable<T>> extends SyntacticallyValid<T> {
    private DisplayablyValid(T validatedObject) {
      super(validatedObject);
    }
  }

  public static class SemanticallyValid<T extends Validateable<T>> extends DisplayablyValid<T> {
    private SemanticallyValid(T validatedObject) {
      super(validatedObject);
    }
  }

  public static class RunnableObj<T extends Validateable<T>> extends SemanticallyValid<T> {
    private RunnableObj(T validatedObject) {
      super(validatedObject);
    }
  }

  /**
   * Attempts to wrap a Validateable object in a SyntacticallyValid wrapper.  If the passed object is not
   * syntactically valid, throws an exception.
   * 
   * @param validatedObject syntactically valid object
   * @return wrapper around passed object
   * @throws ValidObjectWrappingException if passed object is not syntactically valid
   */
  public static <T extends Validateable<T>> SyntacticallyValid<T> getSyntacticallyValid(T validatedObject) {
    ValidationBundle validation = validatedObject.getValidationBundle();
    if (validation.getStatus().isValid()) {
      switch(validation.getLevel()) {
        case SYNTACTIC:   return new SyntacticallyValid<>(validatedObject);
        case DISPLAYABLE: return new DisplayablyValid<>(validatedObject);
        case SEMANTIC:    return new SemanticallyValid<>(validatedObject);
        case RUNNABLE:    return new RunnableObj<>(validatedObject);
        default: /* drop through to exception */
      }
    }
    throw new ValidObjectWrappingException(validatedObject.getValidationBundle());
  }

  /**
   * Attempts to wrap a Validateable object in a DisplayablyValid wrapper.  If the passed object is not
   * displayably valid, throws an exception.
   * 
   * @param validatedObject displayably valid object
   * @return wrapper around passed object
   * @throws ValidObjectWrappingException if passed object is not displayably valid
   */
  public static <T extends Validateable<T>> DisplayablyValid<T> getDisaplayablyValid(T validatedObject) {
    ValidationBundle validation = validatedObject.getValidationBundle();
    if (validation.getStatus().isValid()) {
      switch(validation.getLevel()) {
        case DISPLAYABLE: return new DisplayablyValid<>(validatedObject);
        case SEMANTIC:    return new SemanticallyValid<>(validatedObject);
        case RUNNABLE:    return new RunnableObj<>(validatedObject);
        default: /* drop through to exception */
      }
    }
    throw new ValidObjectWrappingException(validatedObject.getValidationBundle());
  }

  /**
   * Attempts to wrap a Validateable object in a SemanticallyValid wrapper.  If the passed object is not
   * semantically valid, throws an exception.
   * 
   * @param validatedObject semantically valid object
   * @return wrapper around passed object
   * @throws ValidObjectWrappingException if passed object is not semantically valid
   */
  public static <T extends Validateable<T>> SemanticallyValid<T> getSemanticallyValid(T validatedObject) {
    ValidationBundle validation = validatedObject.getValidationBundle();
    if (validation.getStatus().isValid()) {
      switch(validation.getLevel()) {
        case SEMANTIC: return new SemanticallyValid<>(validatedObject);
        case RUNNABLE: return new RunnableObj<>(validatedObject);
        default: /* drop through to exception */
      }
    }
    throw new ValidObjectWrappingException(validatedObject.getValidationBundle());
  }

  /**
   * Attempts to wrap a Validateable object in a Runnable wrapper.  If the passed object is not
   * runnable, throws an exception.
   * 
   * @param validatedObject runnable object
   * @return wrapper around passed object
   * @throws ValidObjectWrappingException if passed object is not runnable
   */
  public static <T extends Validateable<T>> RunnableObj<T> getRunnable(T validatedObject) {
    ValidationBundle validation = validatedObject.getValidationBundle();
    if (validation.getStatus().isValid() &&
        validation.getLevel().equals(ValidationLevel.RUNNABLE)) {
      return new RunnableObj<>(validatedObject);
    }
    throw new ValidObjectWrappingException(validatedObject.getValidationBundle());
  }
}
