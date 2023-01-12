package org.gusdb.fgputil.functional;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.gusdb.fgputil.functional.FunctionalInterfaces.BiConsumerWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.BiFunctionWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.ConsumerWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.PredicateWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Procedure;
import org.gusdb.fgputil.functional.FunctionalInterfaces.ProcedureWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Reducer;
import org.gusdb.fgputil.functional.FunctionalInterfaces.ReducerWithException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.SupplierWithException;

/**
 * Provides a set of static exception management functions for wrapping, handling, and
 * easing use of exceptions, esp while using functional interfaces that do and do not
 * throw exceptions in their APIs.
 *
 * @author rdoherty
 */
public class ExceptionUtil {

  // static class
  private ExceptionUtil(){}

  /**
   * Throws an exception supplied by the passed supplier.  This allows the following pattern:
   * <pre>
   *   return condition ? someValue : doThrow(()-> new SomeException());
   * </pre>
   * @param <T> type of return value
   * @param <E> type of exception thrown
   * @param exceptionSupplier supplier of the exception
   * @return nothing
   * @throws E exception supplied
   */
  public static <T,E extends Throwable> T doThrow(Supplier<E> exceptionSupplier) throws E {
    throw exceptionSupplier.get();
  }

  /**
   * Returns the passed argument if it is already a RuntimeException, else
   * wraps the argument in a RuntimeException and returns the wrapper.
   *
   * @param e unknown exception
   * @return a runtime exception
   */
  public static RuntimeException ensureRuntimeException(Exception e) {
    return e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
  }

  /**
   * Takes a function that may or may not have checked exceptions and returns a new function that performs
   * the same operation but "swallows" any checked exception by wrapping it in a RuntimeException and
   * throwing that instead.  If calling code wishes to inspect the underlying exception it must catch the
   * RuntimeException and use getCause().
   *
   * @param f function to wrap
   * @return a new function that swallows checked exceptions
   */
  public static <S,T> Function<S,T> fSwallow(FunctionWithException<S,T> f) {
    return x -> {
      try {
        return f.apply(x);
      }
      catch (Exception e) {
        throw ensureRuntimeException(e);
      }
    };
  }

  /**
   * Takes a consumer that may or may not have checked exceptions and returns a
   * new consumer that performs the same operation but "swallows" any checked
   * exception by wrapping it in a RuntimeException and throwing that instead.
   * If calling code wishes to inspect the underlying exception it must catch
   * the RuntimeException and use getCause().
   *
   * @param c
   *   consumer to wrap
   *
   * @return a new consumer that swallows checked exceptions
   */
  public static <T> Consumer<T> cSwallow(ConsumerWithException<T> c) {
    return x -> {
      try {
        c.accept(x);
      }
      catch (Exception e) {
        throw ensureRuntimeException(e);
      }
    };
  }

  /**
   * Takes a bi-consumer that may or may not have checked exceptions and returns
   * a new bi-consumer that performs the same operation but "swallows" any checked
   * exception by wrapping it in a RuntimeException and throwing that instead.
   * If calling code wishes to inspect the underlying exception it must catch
   * the RuntimeException and use getCause().
   *
   * @param c
   *   bi-consumer to wrap
   *
   * @return a new bi-consumer that swallows checked exceptions
   */
  public static <T,S> BiConsumer<T,S> c2Swallow(BiConsumerWithException<T,S> c) {
    return (x, y) -> {
      try {
        c.accept(x, y);
      }
      catch (Exception e) {
        throw ensureRuntimeException(e);
      }
    };
  }

  /**
   * Takes a predicate that may or may not have checked exceptions and returns a
   * new predicate that performs the same operation but "swallows" any checked
   * exception by wrapping it in a RuntimeException and throwing that instead.
   * If calling code wishes to inspect the underlying exception it must catch
   * the RuntimeException and use getCause().
   *
   * @param f
   *   predicate to wrap
   *
   * @return a new predicate that swallows checked exceptions
   */
  public static <T> Predicate<T> pSwallow(PredicateWithException<T> f) {
    return x -> {
      try {
        return f.test(x);
      }
      catch (Exception e) {
        throw ensureRuntimeException(e);
      }
    };
  }

  /**
   * Takes a reducer that may or may not have checked exceptions and returns a new reducer that performs
   * the same operation but "swallows" any checked exception by wrapping it in a RuntimeException and
   * throwing that instead.  If calling code wishes to inspect the underlying exception it must catch the
   * RuntimeException and use getCause().
   *
   * @param r reducer to wrap
   * @return a new reducer that swallows checked exceptions
   */
  public static <S,T> Reducer<S,T> rSwallow(ReducerWithException<S,T> r) {
    return (accumulator, next) -> {
      try {
        return r.reduce(accumulator, next);
      }
      catch (Exception e) {
        throw ensureRuntimeException(e);
      }
    };
  }

  /**
   * Takes a no-arg function that may or may not have checked exceptions and returns a new no-arg function
   * that performs the same operation but "swallows" any checked exception by wrapping it in a
   * RuntimeException and throwing that instead.  If calling code wishes to inspect the underlying exception
   * it must catch the RuntimeException and use getCause().
   *
   * @param f function to wrap
   * @return a new function that swallows checked exceptions
   */
  public static <T> Supplier<T> sSwallow(SupplierWithException<T> f) {
    return () -> {
      try {
        return f.get();
      }
      catch (Exception e) {
        throw ensureRuntimeException(e);
      }
    };
  }

  /**
   * Takes a 2-arg function that may or may not have checked exceptions and returns a new 2-arg function
   * that performs the same operation but "swallows" any checked exception by wrapping it in a
   * RuntimeException and throwing that instead.  If calling code wishes to inspect the underlying exception
   * it must catch the RuntimeException and use getCause().
   *
   * @param f function to wrap
   * @return a new function that swallows checked exceptions
   */
  public static <R,S,T> BiFunction<R,S,T> f2Swallow(BiFunctionWithException<R,S,T> f) {
    return (obj1, obj2) -> {
      try {
        return f.apply(obj1, obj2);
      }
      catch (Exception e) {
        throw ensureRuntimeException(e);
      }
    };
  }

  /**
   * Takes a procedure (no args, no return value) that may or may not have checked exceptions
   * and returns a procedure that performs the same operation but "swallows" any checked exception
   * by wrapping it in a RuntimeException and throwing that instead.  If calling code wishes to
   * inspect the underlying exception it must catch the RuntimeException and use getCause().
   *
   * @param p procedure to wrap
   * @return a new procedure that swallows checked exceptions
   */
  public static Procedure pSwallow(ProcedureWithException p) {
    return () -> {
      try {
        p.perform();
      }
      catch (Exception e) {
        throw ensureRuntimeException(e);
      }
    };
  }

  /**
   * Attempts to retrieve a value from the passed supplier.  If an exception occurs, a default value
   * is returned and the exception is buried.
   *
   * @param f supplier function to execute
   * @param defaultValue value to return if supplier fails
   */
  public static <T> T defaultOnException(SupplierWithException<T> f, T defaultValue) {
    try {
      return f.get();
    }
    catch (Exception e) {
      return defaultValue;
    }
  }

  /**
   * Tries to get the next value from the passed supplier.  If successful,
   * returns an optional containing the supplied value; if not and an exception
   * is thrown, returns an empty optional.
   *
   * @param supplier supplier with exception
   * @return optional of supplied value, or empty optional if exception thrown
   */
  public static <S> Optional<S> optionalOnException(SupplierWithException<S> supplier) {
    try {
      return Optional.of(supplier.get());
    }
    catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Takes a supplier that may throw an exception, and a mapper from that exception to a desired exception;
   * calls the supplier, throwing a mapped exception if something goes wrong.
   *
   * @param s supplier
   * @param exceptionMapper exception mapper
   * @return value supplied by supplier if successful
   * @throws S mapped exception if supplier is not successful
   */
  public static <T, S extends Exception> T mapException(SupplierWithException<T> s, Function<Exception, S> exceptionMapper) throws S {
    try {
      return s.get();
    }
    catch (Exception e) {
      throw exceptionMapper.apply(e);
    }
  }

  /**
   * Takes a procedure that may throw an exception, and a mapper from that exception to a desired exception;
   * calls the procedure, throwing a mapped exception if something goes wrong.
   *
   * @param p procedure
   * @param exceptionMapper exception mapper
   * @throws S mapped exception if supplier is not successful
   */
  public static <S extends Exception> void mapException(ProcedureWithException f, Function<Exception, S> exceptionMapper) throws S {
    try {
       f.perform();
    }
    catch (Exception e) {
      throw exceptionMapper.apply(e);
    }
  }

  /**
   * Calls the passed function with the passed value and returns true if no exception is thrown, else false.
   *
   * @param function function to be called
   * @param inputValue value to pass to the function
   * @return false if exception thrown, else true
   */
  public static <T> boolean executesWithoutException(Function<T, ?> function, T inputValue) {
    try { function.apply(inputValue); return true; } catch(Exception e) { return false; }
  }

  /**
   * Calls the passed supplier and returns true if no exception is thrown, else false.
   *
   * @param f supplier to be called
   * @return false if exception thrown, else true
   */
  public static <T> boolean executesWithoutException(SupplierWithException<T> f) {
    try { f.get(); return true; } catch(Exception e) { return false; }
  }
}
