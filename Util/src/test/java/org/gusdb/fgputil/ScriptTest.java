package org.gusdb.fgputil;

import static org.junit.Assert.assertEquals;

import javax.script.ScriptException;

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
      assertEquals(testCase.result, result);
    }
  }
  
  @Test
  public void generalScriptTest() throws Exception {
    Evaluator evaluator = Scripting.getScriptEvaluator(Language.JAVASCRIPT);
    for (Case testCase : TEST_CASES) {
      boolean result = (Boolean)evaluator.eval(testCase.expression);
      assertEquals(testCase.result, result);
    }
  }
  
  private Case[] JSON_CASES = {
      new Case("{ \"p1\": 3, \"p2\": 4 }", true),   // properly escaped quotes
      new Case("{ 'p1': 3, 'p2': 'blah' }", false), // single quote strings disallowed
      new Case("{ p1: 3, p2: 2 }", false)           // 'naked' field names disallowed 
  };
  
  @Test
  public void jsonVerifierTest() throws Exception {
    JavaScript evaluator = new JavaScript();
    for (Case testCase : JSON_CASES) {
      assertEquals(testCase.result, evaluator.isValidJson(testCase.expression));
    }
  }
  
  private String BAD_PARAM_EXPR = "(params.p1 < 4";
  private String GOOD_PARAM_EXPR = "(params.p1 == 3 && params.p2 < 4) || params.p1 > 5";
  
  private Case[] PARAM_CASES = {
      new Case("{ \"p1\": 3, \"p2\": 2 }", true),
      new Case("{ \"p1\": 3, \"p2\": 4 }", false),
      new Case("{ \"p1\": 4, \"p2\": 3 }", false),
      new Case("{ \"p1\": 6, \"p2\": 3 }", true)
  };
  
  @Test(expected = ScriptException.class) 
  public void failedParamScriptParseTest() throws Exception {
    JavaScript evaluator = new JavaScript();
    evaluator.isValidBooleanExpression(BAD_PARAM_EXPR);
  }
  
  @Test
  public void multipleBooleanRegistrationTest() throws Exception {
    JavaScript evaluator = new JavaScript();
    evaluator.isValidBooleanExpression(GOOD_PARAM_EXPR);
    evaluator.isValidBooleanExpression(GOOD_PARAM_EXPR);
    evaluator.evaluateBooleanExpression("true != false");
  }
  
  @Test
  public void paramScriptTest() throws Exception {
    JavaScript evaluator = new JavaScript();
    evaluator.isValidBooleanExpression(GOOD_PARAM_EXPR);
    for (Case testCase : PARAM_CASES) {
      boolean result = evaluator.evaluateBooleanExpression(GOOD_PARAM_EXPR, testCase.expression);
      assertEquals(testCase.result, result);
    }
  }
}
