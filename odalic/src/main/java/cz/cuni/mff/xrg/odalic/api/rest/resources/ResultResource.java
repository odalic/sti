package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import cz.cuni.mff.xrg.odalic.users.Role;

/**
 * Result resource definition.
 *
 * @author Václav Brodec
 *
 */
@Component
@Path("/")
public final class ResultResource {

  private final ExecutionService executionService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public ResultResource(final ExecutionService executionService) {
    Preconditions.checkNotNull(executionService);

    this.executionService = executionService;
  }

  @GET
  @Secured({Role.ADMINISTRATOR, Role.USER})
  @Path("tasks/{taskId}/result")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getResult(final @PathParam("taskId") String taskId)
      throws InterruptedException, ExecutionException {
    return getResult(this.securityContext.getUserPrincipal().getName(), taskId);
  }

  @GET
  @Secured({Role.ADMINISTRATOR, Role.USER})
  @Path("users/{userId}/tasks/{taskId}/result")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getResult(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId) throws InterruptedException, ExecutionException {
    Security.checkAuthorization(this.securityContext, userId);

    final Result resultForTaskId;
    try {
      resultForTaskId = this.executionService.getResultForTaskId(userId, taskId);
    } catch (final CancellationException e) {
      throw new NotFoundException("Result is not available, because the processing was canceled.",
          e);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task has not been scheduled or does not exist!", e);
    }

    return Reply.data(Response.Status.OK, resultForTaskId, this.uriInfo).toResponse();
  }
}
