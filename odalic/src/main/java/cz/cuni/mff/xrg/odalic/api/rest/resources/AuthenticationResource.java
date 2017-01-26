package cz.cuni.mff.xrg.odalic.api.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.users.Credentials;
import cz.cuni.mff.xrg.odalic.users.Token;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * Task resource definition.
 * 
 * @author Václav Brodec
 */
@Component
@Path("/users/authenticated")
public final class AuthenticationResource {

  private final UserService userService;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public AuthenticationResource(final UserService userService) {
    Preconditions.checkNotNull(userService);

    this.userService = userService;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response authenticate(final Credentials credentials) {
    final User user;
    try {  
      user = userService.authenticate(credentials);
    } catch (final IllegalArgumentException e) {
      throw new NotAuthorizedException(e, (Object) null);
    }
    
    final Token token = userService.issueToken(user);
    
    return Reply.data(Response.Status.OK, token, uriInfo).toResponse();
  }
}
