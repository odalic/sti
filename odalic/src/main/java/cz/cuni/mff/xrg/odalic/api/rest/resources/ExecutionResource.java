package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.api.rest.values.ExecutionValue;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.users.Role;

@Component
@Path("/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class ExecutionResource {

  private ExecutionService executionService;
  
  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;
  
  @Autowired
  public ExecutionResource(ExecutionService executionService) {
    Preconditions.checkNotNull(executionService);
    
    this.executionService = executionService;
  }

  @PUT
  @Path("users/{userId}/tasks/{taskId}/execution")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putExecutionForTaskId(final @PathParam("userId") String userId, final @PathParam("taskId") String taskId, final ExecutionValue execution) throws IOException {
    Security.checkAuthorization(this.securityContext, userId);
    
    if (execution == null) {
      throw new BadRequestException("The execution must be provided!");
    }
    
    try {
      executionService.submitForTaskId(userId, taskId);
    } catch (final IllegalStateException e) {
      throw new WebApplicationException("The task has already been scheduled!", e, Response.Status.CONFLICT);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The task does not exist!", e);
    }
    
    return Message.of("Execution submitted.").toResponse(Response.Status.OK, uriInfo);
  }
  
  @PUT
  @Path("tasks/{taskId}/execution")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putExecutionForTaskId(final @PathParam("taskId") String taskId, final ExecutionValue execution) throws IOException {
    return putExecutionForTaskId(securityContext.getUserPrincipal().getName(), taskId, execution);
  }
  
  @DELETE
  @Path("users/{userId}/tasks/{taskId}/execution")
  @Produces({MediaType.APPLICATION_JSON})
  public Response deleteExecutionForTaskId(final @PathParam("userId") String userId, final @PathParam("taskId") String taskId) {
    Security.checkAuthorization(this.securityContext, userId);
    
    try {
      executionService.cancelForTaskId(userId, taskId);
    } catch (final IllegalStateException e) {
      throw new WebApplicationException("The task has already finished!", e, Response.Status.CONFLICT);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task has not been scheduled or does not exist!", e);
    }
    
    return Message.of("Execution canceled.").toResponse(Response.Status.OK, uriInfo);
  }
  
  @DELETE
  @Path("tasks/{taskId}/execution")
  @Produces({MediaType.APPLICATION_JSON})
  public Response deleteExecutionForTaskId(final @PathParam("taskId") String taskId) {
    return deleteExecutionForTaskId(securityContext.getUserPrincipal().getName(), taskId);
  }
}
