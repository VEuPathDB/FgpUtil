package org.gusdb.fgputil.test;

import static org.junit.Assert.assertEquals;

import org.gusdb.fgputil.JavaScript;
import org.gusdb.fgputil.Scripting;
import org.gusdb.fgputil.Scripting.Evaluator;
import org.gusdb.fgputil.Scripting.Language;
import org.junit.Test;

public class ScriptTest {

  private class Case {
    public String expression;
    public boolean result;
    public Case(String expr, boolean res) {
      expression = expr;
      result = res;
    }
  }
  
  private Case[] TEST_CASES = {
      new Case("true", true),
      new Case("false", false),
      new Case("true && false", false),
      new Case("false || true", true),
      new Case("(true && false) || (true && (true || false))", true),
      new Case("!true", false),
      new Case("true && !(true && !(true && false))", false),
      new Case("((1 + 5) / 6 + 4) * 5 == 25", true),
      new Case("6 / 3 + 5 == 7 && true", true),
      new Case("6 / 3 + 5 == 7 && (true && false)", false),
      new Case("'ryan' != ''", true),
      new Case("'ryan' == 'ry' + 'an'", true),
      new Case("'jam' + 'es' != 'james'", false),
      new Case("'' == ''", true)
  };
  
  @Test
  public void javascriptTest() throws Exception {
    JavaScript script = new JavaScript();
    for (Case testCase : TEST_CASES) {
      boolean result = script.evaluateBooleanExpression(testCase.expression);
      assertEquals(result, testCase.result);
    }
  }
  
  @Test
  public void generalScriptTest() throws Exception {
    Evaluator evaluator = Scripting.getScriptEvaluator(Language.JAVASCRIPT);
    for (Case testCase : TEST_CASES) {
      boolean result = (Boolean)evaluator.eval(testCase.expression);
      assertEquals(result, testCase.result);
    }
  }
  
}
