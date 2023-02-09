package org.gusdb.fgputil.client;

import org.apache.log4j.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

/**
 * Jersey client request interceptor used to propagate the trace ID from the thread context as a header in the
 * HTTP request.
 */
public class TracePropagatingClientInterceptor implements ClientRequestFilter {
  private static final Logger LOG = Logger.getLogger(TracePropagatingClientInterceptor.class);
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
