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
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.FeedbackService;

@Component
@Path("/tasks/{id}/configuration/feedback")
public final class FeedbackResource {

  private final FeedbackService feedbackService;
  
  @Context
  private UriInfo uriInfo;
  
  @Autowired
  public FeedbackResource(FeedbackService feedbackService) {
    Preconditions.checkNotNull(feedbackService);
    
    this.feedbackService = feedbackService;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFeedbackForTaskId(@PathParam("id") String id, Feedback feedback) {
    if (feedback == null) {
      throw new BadRequestException("The feedback must be specified!");
    }
    
    try {
      feedbackService.setForTaskId(id, feedback);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The task that the feedback is made to does not exist!", e);
    }
    
    return Message.of("Feedback set.").toResponse(Response.Status.OK, uriInfo);
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFeedbackForTaskId(@PathParam("id") String taskId) {
    final Feedback feedbackForTaskId;
    try {
      feedbackForTaskId = feedbackService.getForTaskId(taskId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task does not exist!", e);
    }
    
    return Reply.data(Response.Status.OK, feedbackForTaskId, uriInfo).toResponse();
  }
  
  @GET
  @Path("/input")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJsonDataById(@PathParam("id") String id) throws IOException {
    final Input inputForTaskId;
    try {
      inputForTaskId = feedbackService.getInputForTaskId(id);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task does not exist!", e);
    }
    
    if (inputForTaskId == null) {
      throw new NotFoundException("The input snapshot does not exist yet!");
    }
    
    return Reply.data(Response.Status.OK, inputForTaskId, uriInfo).toResponse();
  }
}
