package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
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
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.FeedbackService;
import cz.cuni.mff.xrg.odalic.users.Role;

@Component
@Path("/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class FeedbackResource {

  private final FeedbackService feedbackService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;


  @Autowired
  public FeedbackResource(FeedbackService feedbackService) {
    Preconditions.checkNotNull(feedbackService);

    this.feedbackService = feedbackService;
  }

  @PUT
  @Path("users/{userId}/tasks/{taskId}/configuration/feedback")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFeedbackForTaskId(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId, final Feedback feedback) {
    Security.checkAuthorization(securityContext, userId);

    if (feedback == null) {
      throw new BadRequestException("The feedback must be specified!");
    }

    try {
      feedbackService.setForTaskId(userId, taskId, feedback);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The task that the feedback is made to does not exist!", e);
    }

    return Message.of("Feedback set.").toResponse(Response.Status.OK, uriInfo);
  }

  @PUT
  @Path("tasks/{taskId}/configuration/feedback")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFeedbackForTaskId(final @PathParam("taskId") String id, Feedback feedback) {
    return putFeedbackForTaskId(securityContext.getUserPrincipal().getName(), id, feedback);
  }

  @GET
  @Path("users/{userId}/tasks/{taskId}/configuration/feedback")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFeedbackForTaskId(final @PathParam("userId") String userId,
      @PathParam("taskId") String taskId) {
    Security.checkAuthorization(securityContext, userId);

    final Feedback feedbackForTaskId;
    try {
      feedbackForTaskId = feedbackService.getForTaskId(taskId, userId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task does not exist!", e);
    }

    return Reply.data(Response.Status.OK, feedbackForTaskId, uriInfo).toResponse();
  }

  @GET
  @Path("tasks/{taskId}/configuration/feedback")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFeedbackForTaskId(final @PathParam("taskId") String taskId) {
    return getFeedbackForTaskId(securityContext.getUserPrincipal().getName(), taskId);
  }

  @GET
  @Path("users/{userId}/tasks/{taskId}/configuration/feedback/input")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJsonDataById(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId) throws IOException {
    final Input inputForTaskId;
    try {
      inputForTaskId = feedbackService.getInputForTaskId(userId, taskId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task does not exist!", e);
    }

    if (inputForTaskId == null) {
      throw new NotFoundException("The input snapshot does not exist yet!");
    }

    return Reply.data(Response.Status.OK, inputForTaskId, uriInfo).toResponse();
  }

  @GET
  @Path("tasks/{taskId}/configuration/feedback/input")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJsonDataById(final @PathParam("taskId") String taskId) throws IOException {
    return getJsonDataById(securityContext.getUserPrincipal().getName(), taskId);
  }
}
