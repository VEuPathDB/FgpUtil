package org.gusdb.fgputil.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.json.JSONObject;

import java.io.IOException;

import static org.gusdb.fgputil.json.JsonUtil.Jackson;

public final class SchemaUtil {
  private SchemaUtil() {}

  public static ArrayNode validate(String schema, JSONObject value)
  throws IOException {
    return validate(Jackson.readTree(schema), value);
  }

  public static ArrayNode validate(JsonNode schema, JSONObject value) {
    return validate(schema, Jackson.convertValue(value, JsonNode.class));
  }

  public static ArrayNode validate(JsonNode schema, JsonNode value) {
    var out = Jackson.createArrayNode();
    try {
      var rep = JsonSchemaFactory.byDefault().getJsonSchema(schema).validate(value);

      for (var mes : rep) {
        out.add(mes.asJson());
      }
    } catch (ProcessingException e) {
      out.add(e.getProcessingMessage().asJson());
    }
    return out;
  }

}
