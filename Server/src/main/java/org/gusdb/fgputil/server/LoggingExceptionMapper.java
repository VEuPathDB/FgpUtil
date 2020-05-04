package org.gusdb.fgputil.server;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ParamException.PathParamException;

public class LoggingExceptionMapper implements ExceptionMapper<Exception> {

  private static final Logger LOG = Logger.getLogger(LoggingExceptionMapper.class);

  @Override
  public Response toResponse(Exception e) {

    try { throw(e); }

    catch (NotFoundException | PathParamException e404) {
      return logResponse(e, Response.status(Status.NOT_FOUND)
          .type(MediaType.TEXT_PLAIN).entity(e404.getMessage()).build());
    }

    catch (ForbiddenException e403) {
      return logResponse(e, Response.status(Status.FORBIDDEN)
          .type(MediaType.TEXT_PLAIN).entity(e403.getMessage()).build());
    }

    catch (BadRequestException e400) {
      return logResponse(e, Response.status(Status.BAD_REQUEST)
          .type(MediaType.TEXT_PLAIN).entity(e400.getMessage()).build());
    }

    catch (WebApplicationException eApp) {
      if(eApp.getCause() != null && eApp.getCause() instanceof Exception ) {
        return logResponse(e, this.toResponse((Exception) eApp.getCause()));
      }
      else {
        return logResponse(e, Response.status(eApp.getResponse().getStatus())
          .type(MediaType.TEXT_PLAIN).entity(eApp.getMessage()).build());
      }
    }

    // Some other exception that must be handled by the application; send error event
    catch (Exception other) {
      return logResponse(e, Response.status(Status.INTERNAL_SERVER_ERROR)
          .type(MediaType.TEXT_PLAIN).entity(other.getMessage()).build());
    }
  }
    /**
     * Logs both the thrown exception and the HTTP status code of the response
     * 
     * @param e exception being mapped
     * @param response response to be returned to the client
     * @return unmodified response
     */
    private Response logResponse(Exception e, Response response) {
      LOG.error("Caught service error [ responseCode: " + response.getStatus() + " ]", e);
      return response;
    }
}
