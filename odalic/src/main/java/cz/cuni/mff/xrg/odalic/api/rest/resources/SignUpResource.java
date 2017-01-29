package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.net.MalformedURLException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
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
import cz.cuni.mff.xrg.odalic.api.rest.values.PasswordChangeValue;
import cz.cuni.mff.xrg.odalic.users.Credentials;
import cz.cuni.mff.xrg.odalic.users.Token;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * Sign-up resource definition.
 * 
 * @author Václav Brodec
 */
@Component
@Path("/users")
public final class SignUpResource {

  private static final String CHALLENGE_FORMAT =
      "Bearer realm=\"Odalic registration\", error=\"invalid_token\", error_description=\"%s\"";

  private final UserService userService;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public SignUpResource(final UserService userService) {
    Preconditions.checkNotNull(userService);

    this.userService = userService;
  }

  @POST
  @Path("signed-up")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response signUp(final Credentials credentials) throws MalformedURLException {
    try {
      userService.signUp(credentials);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e);
    }

    return Message
        .of("An account created. Please activate via the code sent to the provided e-mail before the first use.")
        .toResponse(Response.Status.OK, uriInfo);
  }

  @POST
  @Path("active")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response activate(Token token) {
    try {
      userService.activateUser(token);
    } catch (final IllegalArgumentException e) {
      throw new NotAuthorizedException(e, String.format(CHALLENGE_FORMAT, e.getMessage()));
    }

    return Message.of("Successfully activated!").toResponse(Response.Status.OK, uriInfo);
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response changePassword(final @PathParam("id") String id,
      final PasswordChangeValue passwordChangeValue) throws MalformedURLException {
    final User user;
    try {
      user = userService.authenticate(new Credentials(id, passwordChangeValue.getOldPassword()));
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e);
    }

    try {
      userService.requestPasswordChange(user, passwordChangeValue.getNewPassword());
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e);
    }

    return Message
        .of("Password change requested. Please confirm via the code sent to the provided e-mail.")
        .toResponse(Response.Status.OK, uriInfo);
  }

  @POST
  @Path("reset/confirmed")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response confirmPasswordChange(final Token token) {
    try {
      userService.confirmPasswordChange(token);
    } catch (final IllegalArgumentException e) {
      throw new NotAuthorizedException(e, String.format(CHALLENGE_FORMAT, e.getMessage()));
    }

    return Message.of("Password reset!").toResponse(Response.Status.OK, uriInfo);
  }
}
