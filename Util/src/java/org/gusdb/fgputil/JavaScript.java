package org.gusdb.fgputil;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.gusdb.fgputil.Scripting.Language;

/**
 * Utility class that allows developers to write functions in JavaScript,
 * easily execute them, and cast results back to Java objects.
 * 
 * @author rdoherty
 */
public class JavaScript {

  /**
   * JavaScript engine that will execute our code
   */
  private ScriptEngine _engine;

  /**
   * Registers functions required to execute the methods below
   * 
   * @throws ScriptException if code is unable to be evaluated
   */
  private void registerFunctions() throws ScriptException {
    _engine.eval("function evalBool(expr) { return eval(expr); }");
  }

  /**
   * Evaluates the passed boolean expression and returns result as boolean
   * 
   * @param expression boolean expression in javascript
   * @return true or false
   * @throws ScriptException if expression syntax is incorrect
   */
  public boolean evaluateBooleanExpression(String expression) throws ScriptException {
    return callFunction("evalBool", Boolean.class, expression);
  }
  
  /**
   * Creates a JavaScript engine and registers required functions so public
   * methods can be called.
   */
  public JavaScript() {
    _engine = Scripting.getScriptEngine(Language.JAVASCRIPT);
    try {
      registerFunctions();
    }
    catch (ScriptException e) {
      throw new RuntimeException("Hard-coded function not parsable by script engine.", e);
    }
  }
  
  /**
   * Evaluates a given function by name, relaying the passed parameters, and
   * casting the result as the passed class.
   * 
   * @param functionName name of the function to call
   * @param returnType return type desired/expected
   * @param args arguments to pass to the javascript function
   * @return result of the executed function, cast as the type provided
   * @throws ScriptException if error occurs in execution of the script
   */
  @SuppressWarnings("unchecked")
  private <T> T callFunction(String functionName, Class<T> returnType, Object... args) throws ScriptException {
    try {
      return (T)((Invocable)_engine).invokeFunction(functionName, args);
    }
    catch (NoSuchMethodException e) {
      // this should never happen since function is defined above
      throw new RuntimeException("Function called that was not defined in script engine.", e);
    }
  }
  
}
