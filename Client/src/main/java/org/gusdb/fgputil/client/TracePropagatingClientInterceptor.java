package org.gusdb.fgputil.client;

import org.apache.logging.log4j.ThreadContext;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

public class TracePropagatingClientInterceptor implements ClientRequestFilter {
  private static final String TRACE_HEADER = "traceparent";
  private static final String TRACE_CONTEXT_KEY = "traceId";

  @Override
  public void filter(ClientRequestContext clientRequestContext) throws IOException {
    final String traceId = ThreadContext.get(TRACE_CONTEXT_KEY);
    if (ThreadContext.get(TRACE_CONTEXT_KEY) != null) {
      clientRequestContext.getHeaders().add(TRACE_HEADER, traceId);
    }
  }
}
