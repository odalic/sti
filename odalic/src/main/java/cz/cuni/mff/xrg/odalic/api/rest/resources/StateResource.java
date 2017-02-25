package cz.cuni.mff.xrg.odalic.api.rest.resources;

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
import cz.cuni.mff.xrg.odalic.api.rest.values.StateValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.States;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.users.Role;

/**
 * State resource definition.
 *
 * @author Václav Brodec
 *
 */
@Component
@Path("/")
public final class StateResource {

  private final ExecutionService executionService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public StateResource(final ExecutionService executionService) {
    Preconditions.checkNotNull(executionService);

    this.executionService = executionService;
  }

  @GET
  @Path("tasks/{taskId}/state")
  @Secured({Role.ADMINISTRATOR, Role.USER})
  @Produces({MediaType.APPLICATION_JSON})
  public Response getStateForTaskId(final @PathParam("taskId") String taskId) {
    return getStateForTaskId(this.securityContext.getUserPrincipal().getName(), taskId);
  }

  @GET
  @Path("users/{userId}/tasks/{taskId}/state")
  @Secured({Role.ADMINISTRATOR, Role.USER})
  @Produces({MediaType.APPLICATION_JSON})
  public Response getStateForTaskId(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId) {
    Security.checkAuthorization(this.securityContext, userId);

    final StateValue state;
    try {
      state = States.queryStateValue(this.executionService, userId, taskId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task does not exist!", e);
    }

    return Reply.data(Response.Status.OK, state, this.uriInfo).toResponse();
  }
}
