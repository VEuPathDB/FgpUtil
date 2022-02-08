package org.gusdb.fgputil.web;

import static org.gusdb.fgputil.FormatUtil.urlDecodeUtf8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.gusdb.fgputil.IoUtil;

/**
 * Provides utility to parse URL-encoded form serialization, i.e. a data
 * value submitted as part of a 'application/x-www-form-urlencoded' POST
 * request, or a GET request's param string (after the '&' character).
 *
 * @author rdoherty
 */
public class UrlEncodedForm extends HashMap<String,List<String>> {

  public UrlEncodedForm(InputStream requestBody) {
    this(readString(requestBody));
  }

  private static String readString(InputStream requestBody) {
    try (Reader in = new InputStreamReader(requestBody)) {
      return IoUtil.readAllChars(in);
    }
    catch (IOException e) {
      throw new RuntimeException("Unable to read input stream", e);
    }
  }

  public UrlEncodedForm(String requestBody) {
    for (String param : requestBody.split("&")) {
      int equalIndex = param.indexOf('=');
      int nameEndIndex = equalIndex == -1 ? param.length() : equalIndex;
      String name = urlDecodeUtf8(param.substring(0, nameEndIndex));
      List<String> values = get(name);
      if (values == null) {
        values = new ArrayList<>();
        put(name, values);
      }
      values.add(equalIndex == -1 ? "true" :
          urlDecodeUtf8(param.substring(equalIndex + 1, param.length())));
    }
  }

  public Optional<List<String>> getParamValues(String key) {
    return Optional.ofNullable(get(key));
  }

  public Optional<String> getFirstParamValue(String key) {
    return Optional.ofNullable(get(key)).flatMap(list ->
        list.isEmpty() ? Optional.empty() : Optional.of(list.get(0)));
  }

}
