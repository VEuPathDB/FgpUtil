package org.gusdb.fgputil.json;

import java.io.IOException;
import java.io.Writer;

import org.json.JSONWriter;

public class JsonWriter extends JSONWriter implements AutoCloseable {

  private Writer _writer;

  public JsonWriter(Writer writer) {
    super(writer);
    _writer = writer;
  }

  @Override
  public void close() throws IOException {
    _writer.close();
  }

}
