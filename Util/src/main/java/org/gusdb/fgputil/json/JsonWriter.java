package org.gusdb.fgputil.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.json.JSONWriter;

public class JsonWriter extends JSONWriter implements AutoCloseable {

  public JsonWriter(Writer writer) {
    super(writer);
  }

  public JsonWriter(OutputStream out) {
    super(new BufferedWriter(new OutputStreamWriter(out)));
  }

  @Override
  public void close() throws IOException {
    ((Writer)writer).close();
  }

}
