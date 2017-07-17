package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.NavigableSet;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.values.PasswordChangeValue;
import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.users.Credentials;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.Token;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * Sign-up resource definition.
 *
 * @author Václav Brodec
 */
@Component
@Path("/")
public final class UsersResource {

  private final UserService userService;
  private final ExecutionService executionService;
  private final TaskService taskService;
  private final FileService fileService;
  private final BasesService basesService;
  private final GroupsService groupsService;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public UsersResource(final UserService userService, final ExecutionService executionService,
      final TaskService taskService, final FileService fileService, final BasesService basesService, final GroupsService groupsService) {
    Preconditions.checkNotNull(userService, "The userService cannot be null!");
    Preconditions.checkNotNull(executionService, "The executionService cannot be null!");
    Preconditions.checkNotNull(taskService, "The taskService cannot be null!");
    Preconditions.checkNotNull(fileService, "The fileService cannot be null!");
    Preconditions.checkNotNull(basesService, "The basesService cannot be null!");
    Preconditions.checkNotNull(groupsService, "The groupsService cannot be null!");

    this.userService = userService;
    this.executionService = executionService;
    this.taskService = taskService;
    this.fileService = fileService;
    this.basesService = basesService;
    this.groupsService = groupsService;
  }

  @POST
  @Path("users/confirmations")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response activate(final Token token) throws IOException {
    try {
      this.userService.activateUser(token);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    return Message.of("Successfully activated!").toResponse(Response.Status.OK, this.uriInfo);
  }

  @POST
  @Path("users/authentications")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response authenticate(final Credentials credentials) {
    final User user;
    try {
      user = this.userService.authenticate(credentials);
    } catch (final Exception e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    final Token token = this.userService.issueToken(user);

    return Reply.data(Response.Status.OK, token, this.uriInfo).toResponse();
  }

  @POST
  @Path("users/passwords/confirmations")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response confirmPasswordChange(final Token token) {
    try {
      this.userService.confirmPasswordChange(token);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    return Message.of("Password reset!").toResponse(Response.Status.OK, this.uriInfo);
  }

  @DELETE
  @Path("users/{userId}")
  @Secured({Role.ADMINISTRATOR})
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteUser(final @PathParam("userId") String userId) {
    try {
      // In reverse order to initialization
      this.executionService.unscheduleAll(userId);
      this.taskService.deleteAll(userId);
      this.fileService.deleteAll(userId);
      this.basesService.deleteAll(userId);
      this.groupsService.deleteAll(userId);
      this.userService.deleteUser(userId);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    return Message.of("The user has been deleted.").toResponse(Status.OK, this.uriInfo);
  }

  @GET
  @Path("users/{userId}")
  @Secured({Role.ADMINISTRATOR, Role.USER})
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUser(final @PathParam("userId") String userId) {
    final User user;
    try {
      user = this.userService.getUser(userId);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    return Reply.data(Status.OK, user, this.uriInfo).toResponse();
  }

  @GET
  @Path("users")
  @Secured({Role.ADMINISTRATOR})
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUsers() {
    final NavigableSet<User> users = this.userService.getUsers();

    return Reply.data(Status.OK, users, this.uriInfo).toResponse();
  }

  @PUT
  @Path("users/{userId}/password")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response changePassword(final @PathParam("userId") String userId,
      final PasswordChangeValue passwordChangeValue) throws MalformedURLException {
    final User user;
    try {
      user = this.userService
          .authenticate(new Credentials(userId, passwordChangeValue.getOldPassword()));
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    try {
      this.userService.requestPasswordChange(user, passwordChangeValue.getNewPassword());
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    return Message
        .of("Password change requested.")
        .toResponse(Response.Status.OK, this.uriInfo);
  }

  @POST
  @Path("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response signUp(final Credentials credentials) throws IOException {
    try {
      this.userService.signUp(credentials);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    return Message
        .of("An account created. Activation may be required before the first use.")
        .toResponse(Response.Status.OK, this.uriInfo);
  }
}
