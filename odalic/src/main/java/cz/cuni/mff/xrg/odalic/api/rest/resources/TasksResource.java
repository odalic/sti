package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NavigableSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.api.rdf.TaskRdfSerializationService;
import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.api.rest.values.ConfigurationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.StatefulTaskValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.TaskValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.States;
import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * Task resource definition.
 * 
 * @author Václav Brodec
 */
@Component
@Path("/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class TasksResource {

  private static final String TURTLE_MIME_TYPE = "text/turtle";

  private final UserService userService;
  private final TaskService taskService;
  private final FileService fileService;
  private final ExecutionService executionService;
  private final BasesService basesService;
  private final TaskRdfSerializationService taskSerializationService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public TasksResource(final UserService userService, final TaskService taskService,
      final FileService fileService, final ExecutionService executionService,
      final BasesService basesService, final TaskRdfSerializationService taskSerializationService) {
    Preconditions.checkNotNull(userService);
    Preconditions.checkNotNull(taskService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(executionService);
    Preconditions.checkNotNull(basesService);
    Preconditions.checkNotNull(taskSerializationService);

    this.userService = userService;
    this.taskService = taskService;
    this.fileService = fileService;
    this.executionService = executionService;
    this.basesService = basesService;
    this.taskSerializationService = taskSerializationService;
  }

  @GET
  @Path("users/{userId}/tasks")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTasks(final @PathParam("userId") String userId,
      final @QueryParam("states") Boolean states, final @QueryParam("orderedBy") String orderedBy) {
    Security.checkAuthorization(securityContext, userId);

    final NavigableSet<Task> tasks;
    if (orderedBy == null) {
      tasks = taskService.getTasksSortedByIdInAscendingOrder(userId);
    } else {
      switch (orderedBy) {
        case "id":
          tasks = taskService.getTasksSortedByIdInAscendingOrder(userId);
          break;
        case "created":
          tasks = taskService.getTasksSortedByCreatedInDescendingOrder(userId);
          break;
        default:
          throw new BadRequestException("Invalid sorting key!");
      }
    }

    if (states == null || (!states)) {
      return Reply.data(Response.Status.OK, tasks, uriInfo).toResponse();
    } else {
      final Stream<StatefulTaskValue> statefulTasksStream = tasks.stream()
          .map(e -> new StatefulTaskValue(e, States.queryStateValue(executionService, e)));

      return Reply
          .data(Response.Status.OK, statefulTasksStream.collect(Collectors.toList()), uriInfo)
          .toResponse(); // List is fine, as it serializes in the same way, and no monkeying with
                         // comparators is needed.
    }
  }

  @GET
  @Path("tasks")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTasks(final @QueryParam("states") Boolean states,
      final @QueryParam("orderedBy") String orderedBy) {
    return getTasks(securityContext.getUserPrincipal().getName(), states, orderedBy);
  }

  @GET
  @Path("users/{userId}/tasks/{taskId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTaskById(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId) {
    Security.checkAuthorization(securityContext, userId);

    final Task task;
    try {
      task = taskService.getById(userId, taskId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task does not exist!", e);
    }

    return Reply.data(Response.Status.OK, task, uriInfo).toResponse();
  }

  @GET
  @Path("tasks/{taskId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTaskById(final @PathParam("taskId") String taskId) {
    return getTaskById(securityContext.getUserPrincipal().getName(), taskId);
  }

  @PUT
  @Path("users/{userId}/tasks/{taskId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putTaskWithId(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId, final TaskValue taskValue)
      throws MalformedURLException {
    Security.checkAuthorization(securityContext, userId);

    if (taskValue == null) {
      throw new BadRequestException("No task definition provided!");
    }

    if (taskValue.getConfiguration() == null) {
      throw new BadRequestException("Configuration must be included!");
    }

    final ConfigurationValue configurationValue = taskValue.getConfiguration();

    if (taskValue.getId() != null && !taskValue.getId().equals(taskId)) {
      throw new BadRequestException("The ID in the payload is not the same as the ID of resource.");
    }

    final File input;
    try {
      input = fileService.getById(userId, configurationValue.getInput());
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The input file does not exist!", e);
    }

    final NavigableSet<KnowledgeBase> usedBases;
    if (configurationValue.getUsedBases() == null) {
      usedBases = basesService.getBases();
    } else {
      usedBases = configurationValue.getUsedBases();
    }

    final Configuration configuration;
    try {
      configuration = new Configuration(input, usedBases, configurationValue.getPrimaryBase(),
          configurationValue.getFeedback(), configurationValue.getRowsLimit(),
          configurationValue.isStatistical());
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    final Task task;
    try {
      task = new Task(userService.getUser(userId), taskId,
          taskValue.getDescription() == null ? "" : taskValue.getDescription(), configuration);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    final Task taskById = taskService.verifyTaskExistenceById(userId, taskId);

    final URL location =
        cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, taskId);

    if (taskById == null) {
      taskService.create(task);
      return Message.of("A new task has been created AT THE LOCATION you specified")
          .toResponse(Response.Status.CREATED, location, uriInfo);
    } else {
      if (!task.getConfiguration().equals(taskById.getConfiguration())) {
        executionService.unscheduleForTaskId(userId, taskId);
      }
      
      taskService.replace(task);
      return Message
          .of("The task you specified has been fully updated AT THE LOCATION you specified.")
          .toResponse(Response.Status.OK, location, uriInfo);
    }
  }

  @PUT
  @Path("tasks/{taskId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putTaskWithId(final @PathParam("taskId") String taskId, final TaskValue taskValue)
      throws MalformedURLException {
    return putTaskWithId(securityContext.getUserPrincipal().getName(), taskId, taskValue);
  }

  @DELETE
  @Path("users/{userId}/tasks/{taskId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteTaskById(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId) {
    Security.checkAuthorization(securityContext, userId);

    try {
      taskService.deleteById(userId, taskId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task does not exist!", e);
    }

    return Message.of("Task deleted.").toResponse(Response.Status.OK, uriInfo);
  }

  @DELETE
  @Path("tasks/{taskId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteTaskById(final @PathParam("taskId") String taskId) {
    return deleteTaskById(securityContext.getUserPrincipal().getName(), taskId);
  }

  @PUT
  @Path("users/{userId}/tasks/{taskId}")
  @Consumes(TURTLE_MIME_TYPE)
  @Produces(MediaType.APPLICATION_JSON)
  public Response importTaskId(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId, final InputStream body) throws IOException {
    Security.checkAuthorization(this.securityContext, userId);

    if (body == null) {
      throw new BadRequestException("The body cannot be null!");
    }

    final Task task;
    try {
      task = taskSerializationService.deserialize(body, userId, taskId, uriInfo.getBaseUri());
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    final Task taskById = taskService.verifyTaskExistenceById(userId, taskId);

    final URL location =
        cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, taskId);

    if (taskById == null) {
      taskService.create(task);
      return Message.of("A task has been imported AT THE LOCATION you specified")
          .toResponse(Response.Status.CREATED, location, uriInfo);
    } else {
      executionService.unscheduleForTaskId(userId, taskId);
      taskService.replace(task);
      return Message
          .of("The task you specified has been fully updated from the import AT THE LOCATION you specified.")
          .toResponse(Response.Status.OK, location, uriInfo);
    }
  }

  @PUT
  @Path("tasks/{taskId}")
  @Consumes(TURTLE_MIME_TYPE)
  @Produces(MediaType.APPLICATION_JSON)
  public Response importTaskId(final @PathParam("taskId") String taskId, final InputStream body) throws IOException {
    return importTaskId(securityContext.getUserPrincipal().getName(), taskId, body);
  }

  @GET
  @Path("users/{userId}/tasks/{taskId}")
  @Produces(TURTLE_MIME_TYPE)
  public Response exportTaskId(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId) {
    Security.checkAuthorization(this.securityContext, userId);

    final Task task;
    try {
      task = this.taskService.getById(userId, taskId);
    } catch (final IllegalArgumentException e) {
      return Response.status(Status.NOT_FOUND).build();
    }

    final String exportedTask = taskSerializationService.serialize(task, uriInfo.getBaseUri());
    
    return Response.ok(exportedTask).build();
  }

  @GET
  @Path("tasks/{taskId}/")
  @Produces(TURTLE_MIME_TYPE)
  public Response exportTaskId(final @PathParam("taskId") String taskId) {
    return exportTaskId(securityContext.getUserPrincipal().getName(), taskId);
  }
}
