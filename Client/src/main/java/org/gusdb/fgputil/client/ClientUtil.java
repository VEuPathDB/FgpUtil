package org.gusdb.fgputil.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Function;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.web.HttpMethod;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientUtil {

  private static final Logger LOG = Logger.getLogger(ClientUtil.class);

  public static boolean LOG_RESPONSE_HEADERS = false;

  public static <T> T getResponseObject(String url, Class<T> responseObjectClass) throws IOException {
    return new ObjectMapper().readerFor(responseObjectClass).readValue(new URL(url));
  }

  public static ResponseFuture makeAsyncGetRequest(
      String url, String expectedResponseType) {
    LOG.info("Will send following GET request to " + url);
    return makeAsyncRequest(url, expectedResponseType,
      invoker -> invoker.get());
  }

  public static ResponseFuture makeAsyncPostRequest(
      String url, Object postBodyObject, String expectedResponseType) {
    String json = JsonUtil.serializeObject(postBodyObject);
    LOG.info("Will send following POST request to " + url + "\n" + json);
    return makeAsyncRequest(url, expectedResponseType,
      invoker -> invoker.post(Entity.entity(json, MediaType.APPLICATION_JSON)));
  }

  private static ResponseFuture makeAsyncRequest(String url, String expectedResponseType,
      Function<AsyncInvoker, Future<Response>> responseProducer) {
    MultivaluedMap<String,Object> headers = new MultivaluedHashMap<>();

    // add accept header using passed response type or ALL if null
    headers.add(HttpHeaders.ACCEPT, expectedResponseType == null
        ? MediaType.WILDCARD : expectedResponseType);

    // add jersey header tracing header if logging flag turned on
    if (LOG_RESPONSE_HEADERS) {
      headers.add("X-Jersey-Tracing-Accept", "any");
    }

    return new ResponseFuture(
      responseProducer.apply(
        ClientBuilder.newClient()
          .target(url)
          .request()
          .headers(headers)
          .async()), LOG_RESPONSE_HEADERS);
  }

  public static CloseableResponse makeRequest(String url, HttpMethod method, Optional<JSONObject> body, Map<String,String> headers) {
    Client client = ClientBuilder.newClient();
    WebTarget webTarget = client.target(url);
    Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
    switch(method) {
      case POST:
        return new CloseableResponse(invocationBuilder
            .headers(new MultivaluedHashMap<>(headers))
            .post(Entity.entity(body.map(JSONObject::toString)
            .orElseThrow(() -> new RuntimeException("Body is required for POST")), MediaType.APPLICATION_JSON)));
      case GET:
        body.ifPresent(b -> LOG.warn("JSONObject passed to method to generated GET request; it will be ignored."));
        return new CloseableResponse(invocationBuilder
            .headers(new MultivaluedHashMap<>(headers))
            .get());
      default:
        throw new RuntimeException("Only POST and GET methods are currently supported (not " + method + ").");
    }
  }

  public static String readSmallResponseBody(Response smallResponse) throws IOException {
    String responseBody = "";
    if (smallResponse.hasEntity()) {
      try (InputStream body = (InputStream)smallResponse.getEntity();
           ByteArrayOutputStream str = new ByteArrayOutputStream()) {
        IoUtil.transferStream(str, body);
        responseBody = str.toString();
      }
    }
    return responseBody;
  }
}
