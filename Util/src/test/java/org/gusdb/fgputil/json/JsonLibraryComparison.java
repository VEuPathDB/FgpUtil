package org.gusdb.fgputil.json;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import org.json.JSONObject;
import org.junit.Test;

public class JsonLibraryComparison {

  private static final boolean SHOW_JSON_OUTPUT = false;

  private static final String NL = System.lineSeparator();

  private static JSONObject parseOrgJson(String json) {
    return new JSONObject(json);
  }
  
  private static String prettyPrintOrgJson(JSONObject json) {
    return json.toString(2);
  }

  private static JsonObject parseJavaxJson(String json) {
    return Json.createReader(new StringReader(json)).readObject();
  }

  private static String prettyPrintJavaxJson(JsonObject json) {
    StringWriter stringWriter = new StringWriter();
    Map<String, Object> properties = new HashMap<String, Object>(1);
    properties.put(JsonGenerator.PRETTY_PRINTING, true);
    Json.createWriterFactory(properties).createWriter(stringWriter).writeObject(json);
    return stringWriter.toString();
  }

  @Test
  public void jsonPerformanceTest() {
    String json = "{ \"stuff1\": { \"a\": 1, \"b\": true, \"c\": [] }, \"stuff2\": { \"a\": { \"b\": { \"c\": true } } } }";
    compareJsonLibs(json);
    String bigJson = "{ \"0\": " + json;
    for (int i = 1; i < 1000; i++) {
      bigJson += ", \"" + i + "\":" + json;
      
    }
    bigJson += "}";
    compareJsonLibs(bigJson);
  }
    
  public void compareJsonLibs(String origJson) {

    long startOrg = System.currentTimeMillis();
    JSONObject orgObj = parseOrgJson(origJson);
    long printOrg = System.currentTimeMillis();
    String resultOrg = prettyPrintOrgJson(orgObj);

    long startJavax = System.currentTimeMillis();
    JsonObject javaxObj = parseJavaxJson(origJson);
    long printJavax = System.currentTimeMillis();
    String resultJavax = prettyPrintJavaxJson(javaxObj);

    long end = System.currentTimeMillis();

    String result = new StringBuilder()
        .append("===== org.json =====").append(NL)
        .append("Parse: ").append(printOrg - startOrg).append("ms").append(NL)
        .append("Print: ").append(startJavax - printOrg).append("ms").append(NL)
        .append("Result: ").append(SHOW_JSON_OUTPUT ? resultOrg : "").append(NL)
        .append(NL)
        .append("===== javax.json =====").append(NL)
        .append("Parse: ").append(printJavax - startJavax).append("ms").append(NL)
        .append("Print: ").append(end - printJavax).append("ms").append(NL)
        .append("Result: ").append(SHOW_JSON_OUTPUT ? resultJavax : "").append(NL)
        .append(NL)
        .toString();

    System.out.println(result);
  }
}
