package org.gusdb.fgputil;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Script {

  private ScriptEngine _engine;
  
  public Script() {
    ScriptEngineManager factory = new ScriptEngineManager();
    _engine = factory.getEngineByName("JavaScript");
    try {
      // register functions required to execute the methods below
      _engine.eval("function evalBool(expr) { return eval(expr); }");
    }
    catch (ScriptException e) {
      throw new RuntimeException("Hard-coded function not parsable by script engine.", e);
    }
  }
  
  public boolean evaluateBooleanExpression(String expression) throws ScriptException {
    try {
      Invocable inv = (Invocable) _engine;
      Boolean result = (Boolean)inv.invokeFunction("evalBool", expression);
      return result;
    } catch (NoSuchMethodException e) {
      // this should never happen since function is defined above
      throw new RuntimeException("Function called that was not defined in script engine.", e);
    }
  }
  
}
