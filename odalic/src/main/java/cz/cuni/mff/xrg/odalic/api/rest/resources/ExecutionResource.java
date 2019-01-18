package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.api.rest.values.ComputationInputValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.ComputationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.ExecutionValue;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInput;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import cz.cuni.mff.xrg.odalic.users.Role;

@Component
@Path("/")
public final class ExecutionResource {

  private final ExecutionService executionService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public ExecutionResource(final ExecutionService executionService) {
    Preconditions.checkNotNull(executionService, "The executionService cannot be null!");

    this.executionService = executionService;
  }

  @Secured({Role.ADMINISTRATOR, Role.USER})
  @DELETE
  @Path("tasks/{taskId}/execution")
  @Produces({MediaType.APPLICATION_JSON})
  public Response deleteExecutionForTaskId(final @PathParam("taskId") String taskId) {
    return deleteExecutionForTaskId(this.securityContext.getUserPrincipal().getName(), taskId);
  }

  @Secured({Role.ADMINISTRATOR, Role.USER})
  @DELETE
  @Path("users/{userId}/tasks/{taskId}/execution")
  @Produces({MediaType.APPLICATION_JSON})
  public Response deleteExecutionForTaskId(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId) {
    Security.checkAuthorization(this.securityContext, userId);

    try {
      this.executionService.cancelForTaskId(userId, taskId);
    } catch (final IllegalStateException e) {
      throw new WebApplicationException("The task has already finished!", e,
          Response.Status.CONFLICT);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task has not been scheduled or does not exist!", e);
    }

    return Message.of("Execution canceled.").toResponse(Response.Status.OK, this.uriInfo);
  }

  @Secured({Role.ADMINISTRATOR, Role.USER})
  @PUT
  @Path("tasks/{taskId}/execution")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putExecutionForTaskId(final @PathParam("taskId") String taskId,
      final ExecutionValue execution) throws IOException {
    return putExecutionForTaskId(this.securityContext.getUserPrincipal().getName(), taskId,
        execution);
  }

  @Secured({Role.ADMINISTRATOR, Role.USER})
  @PUT
  @Path("users/{userId}/tasks/{taskId}/execution")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putExecutionForTaskId(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId, final ExecutionValue execution) throws IOException {
    Security.checkAuthorization(this.securityContext, userId);

    if (execution == null) {
      throw new BadRequestException("The execution must be provided!");
    }

    try {
      this.executionService.submitForTaskId(userId, taskId);
    } catch (final IllegalStateException e) {
      throw new WebApplicationException("The task has already been scheduled!", e,
          Response.Status.CONFLICT);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The task does not exist!", e);
    }

    return Message.of("Execution submitted.").toResponse(Response.Status.OK, this.uriInfo);
  }

  @POST
  @Path("users/{userId}/computations")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postComputation(final @PathParam("userId") String userId,
      final ComputationValue computation)
      throws IOException, InterruptedException, ExecutionException {
    if (computation == null) {
      throw new BadRequestException("The computation spec must be provided!");
    }

    final ComputationInputValue computationInput = computation.getInput();
    if (computationInput == null) {
      throw new BadRequestException("The input must be provided!");
    }

    final Input input = new ListsBackedInput(computationInput.getIdentifier(),
        computationInput.getHeaders(), Arrays.stream(computationInput.getRows())
            .map(row -> ImmutableList.copyOf(row)).collect(ImmutableList.toImmutableList()));

    // TODO: Feedback not yet supported.

    final Result result;
    try {
      result = this.executionService.compute(userId, computation.getUsedBases(),
          computation.getPrimaryBase(), input,
          computation.isStatistical() == null ? false : computation.isStatistical(),
          new Feedback());
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    return Reply.data(Status.OK, result, uriInfo).toResponse();
  }
}
