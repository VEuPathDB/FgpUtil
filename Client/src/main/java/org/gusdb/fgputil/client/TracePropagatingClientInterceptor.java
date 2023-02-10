package org.gusdb.fgputil.client;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

/**
 * Jersey client request interceptor used to propagate the trace ID from the thread context as a header in the
 * HTTP request.
 */
public class TracePropagatingClientInterceptor implements ClientRequestFilter {

  private static final String TRACE_HEADER = "traceparent";

  private String traceId;

  public TracePropagatingClientInterceptor(String traceId) {
    this.traceId = traceId;
  }

  @Override
  public void filter(ClientRequestContext clientRequestContext) throws IOException {
    if (traceId != null) {
      clientRequestContext.getHeaders().add(TRACE_HEADER, traceId);
    }
  }
}
