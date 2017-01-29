package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.util.NavigableSet;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * User resource definition.
 * 
 * @author Václav Brodec
 */
@Component
@Secured({Role.ADMINISTRATOR})
public final class UserResource {

  private final UserService userService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public UserResource(final UserService userService) {
    Preconditions.checkNotNull(userService);

    this.userService = userService;
  }

  @GET
  @Path("/users/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUsers() {
    final NavigableSet<User> users = this.userService.getUsers();
    
    return Reply.data(Status.OK, users, uriInfo).toResponse();
  }
}
