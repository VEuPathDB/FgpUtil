package org.gusdb.fgputil;

import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Debugging utility that traces and logs method calls as a tree.
 * <p>
 * Example Output:
 * <pre>
 * MyClass#someMethod("abc", 1)
 *   |- MyClass#otherMethod(1)
 *   |    |- OtherClass#getProp(1) -> "def"
 *   |- MyClass#otherMethod() -> "def"
 * MyClass#someMethod() -> "def"
 * </pre>
 */
public class TraceLog {

  /**
   * Names of packages that TraceLog may be enabled in.
   *
   * All subpackages of the packages in this list will be included.
   */
  private static final String[] packages = {
  };

  /**
   * Specific classes that TraceLog may be enabled for.
   *
   * Array inputs should be the full name of the class as output by {@link
   * Class#getName()}
   */
  private static final String[] classes = {
  };

  private static final String CONTINUATION_PREFIX = "  |  ";

  private static final String NODE_PREFIX = "  |- ";

  /**
   * Cache of continuation strings at varying lengths.
   */
  private static final List<String> CONTINUATIONS = new ArrayList<>();

  private static final Map<Long, Stack<String>> CALL_STACKS = new HashMap<>();

  private final Class<?> target;

  /**
   * Controls whether or not the trace logger will do anything when called.
   *
   * When set to false all logging methods will return immediately.
   */
  private final boolean enabled;

  private final Logger log;

  public TraceLog(final Class<?> target) {
    this.target = target;
    this.log = Logger.getLogger(target);

    final String name = target.getName();

    for (final String cl : classes) {
      if (cl.equals(name)) {
        this.enabled = true;
        return;
      }
    }

    for (final String pack : packages) {
      if (name.startsWith(pack)) {
        this.enabled = true;
        return;
      }
    }

    this.enabled = false;
  }

  /**
   * Begin a trace for the calling method.
   *
   * @param args
   *    calling method input arguments
   *
   * @return This TraceLog instance
   */
  public TraceLog start(Object... args) {
    if (!enabled)
      return this;

    final String method = getMethodName();
    final Stack<String> stack = requireStack();
    final int depth = stack.size();

    stack.push(method);
    log.info(indent(method, depth) + stringArgs(args));
    return this;
  }

  /**
   * Passthrough log for getters that call no other methods.
   *
   * @param out
   *   Return value
   * @param <T>
   *   Return value type
   *
   * @return given return value
   */
  public <T> T getter(final T out) {
    if (!enabled)
      return out;

    log.info(indent(getMethodName(), requireStack().size()) + "() -> " + valToLogString(out));
    return out;
  }

  /**
   * Logs a message for the current trace element
   *
   * @param msg
   *    message to log
   *
   * @return this TraceLog instance
   */
  public TraceLog log(final String msg) {
    if (enabled)
      log.info(indent(getMethodName(), requireStack().size()) + "(): " + msg);
    return this;
  }

  /**
   * Closes the current trace element and passes the given value through.
   *
   * @param ret
   *   return value
   * @param <T>
   *   return value type
   *
   * @return given return value
   */
  public <T> T end(final T ret) {
    if (!enabled)
      return ret;

    final String method = getMethodName();
    final Stack<String> stack = requireStack();

    if (!stack.peek().equals(method)) {
      log.info("???? " + method);
      return ret;
    }

    stack.pop();
    log.info(indent(method, stack.size()) + "() -> " + valToLogString(ret));

    if (stack.isEmpty())
      dropStack();
    return ret;
  }

  /**
   * Closes the current trace element.
   */
  public void end() {
    if (!enabled)
      return;

    final String method = getMethodName();
    final Stack<String> stack = requireStack();

    if (!stack.peek().equals(method)) {
      log.info("???? " + method);
      return;
    }

    stack.pop();
    log.info(indent(method, stack.size()) + "() ->");

    if (stack.isEmpty())
      dropStack();
  }


  private String getMethodName() {
    return target.getSimpleName() + "#"
      + Thread.currentThread().getStackTrace()[3].getMethodName();
  }

  private static String indent(final String line, final int depth) {
    if (depth == 0)
      return line;

    if (depth == 1)
      return NODE_PREFIX + line;

    final int n = depth - 2;

    String prefix;
    synchronized (CONTINUATIONS) {
      if (CONTINUATIONS.size() == n)
        CONTINUATIONS.add(repeatPrefix(depth - 1));
      prefix = CONTINUATIONS.get(n);
    }

    return prefix + NODE_PREFIX + line;
  }

  private static Stack<String> requireStack() {
    synchronized (CALL_STACKS) {
      return CALL_STACKS.computeIfAbsent(
        Thread.currentThread().getId(), k -> new Stack<>());
    }
  }

  private static void dropStack() {
    synchronized (CALL_STACKS) {
      CALL_STACKS.remove(Thread.currentThread().getId());
    }
  }

  private static String repeatPrefix(final int n) {
    if (n == 0)
      return "";

    final StringBuilder out = new StringBuilder(CONTINUATION_PREFIX);
    for (int i = 1; i < n; i++)
      out.append(CONTINUATION_PREFIX);
    return out.toString();
  }

  private static String stringArgs(Object[] args) {
    if (args.length == 0)
      return "()";

    return Arrays.stream(args)
      .map(Object::toString)
      .map(TraceLog::safeLength)
      .collect(Collectors.joining(", ", "(", ")"));
  }

  private static String valToLogString(final Object obj) {
    return safeLength(obj.toString());
  }

  private static String safeLength(final String val) {
    return val.length() > 256 ? "<value too long for log>" : val;
  }
}
