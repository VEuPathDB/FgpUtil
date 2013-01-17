package org.gusdb.fgputil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Scripting engine utility to allow Java code to arbitrarily call JavaScript
 * (and potentially other language) code.
 * 
 * @author rdoherty
 */
public class Scripting {

  /**
   * Enumeration of the scripting languages we support.  Developers should
   * update this list as they add registered interpreters to FgpUtil.
   * 
   * @author rdoherty
   */
  public static enum Language {
    JAVASCRIPT("JavaScript");
    
    private String _key;

    private Language(String languageKey) {
      _key = languageKey;
    }
    
    public String getLanguageKey() { return _key; }
  }
  
  public static class Evaluator {
    
    private ScriptEngine _engine;
    
    public Evaluator(ScriptEngine engine) {
      _engine = engine;
    }
    
    public Object eval(String script) throws ScriptException {
      if (script == null) return null;
      return _engine.eval(script);
    }
  }
  
  private static ScriptEngineManager _factory = new ScriptEngineManager();

  public static ScriptEngine getScriptEngine(Language language) {
    return _factory.getEngineByName(language.getLanguageKey());
  }
  
  public static Evaluator getScriptEvaluator(Language language) {
    return new Evaluator(getScriptEngine(language));
  }
}
