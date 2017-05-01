package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
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
import cz.cuni.mff.xrg.odalic.api.rest.values.GroupValue;
import cz.cuni.mff.xrg.odalic.groups.Group;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * Predicates and classes groups resource definition.
 *
 * @author Václav Brodec
 *
 */
@Component
@Path("/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class GroupsResource {

  private final GroupsService groupsService;
  private final UserService userService;

  @Context
  private SecurityContext securityContext;
  
  @Context
  private UriInfo uriInfo;


  @Autowired
  public GroupsResource(final GroupsService groupsService, final UserService userService) {
    Preconditions.checkNotNull(groupsService);
    Preconditions.checkNotNull(userService);

    this.groupsService = groupsService;
    this.userService = userService;
  }
  
  @GET
  @Path("users/{userId}/groups")
  @Produces({MediaType.APPLICATION_JSON})
  public Response getGroups(final @PathParam("userId") String userId) {
    Security.checkAuthorization(this.securityContext, userId);
    
    final SortedSet<Group> groups = this.groupsService.getGroups(userId);

    return Reply.data(Response.Status.OK, groups, this.uriInfo).toResponse();
  }
  
  @GET
  @Path("groups")
  @Produces({MediaType.APPLICATION_JSON})
  public Response getGroups() {
    return getGroups(this.securityContext.getUserPrincipal().getName());
  }
  
  @GET
  @Path("users/{userId}/groups/{groupId}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response get(final @PathParam("userId") String userId, final @PathParam("groupId") String groupId) {
    Security.checkAuthorization(this.securityContext, userId);
    
    final Group group;
    try {
      group = this.groupsService.getGroup(userId, groupId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("No group with the provided name exists!", e);
    }
    
    return Reply.data(Response.Status.OK, group, this.uriInfo).toResponse();
  }
  
  @GET
  @Path("groups/{groupId}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response get(final @PathParam("groupId") String groupId) {
    return get(this.securityContext.getUserPrincipal().getName(), groupId);
  }

  @PUT
  @Path("users/{userId}/groups/{groupId}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response put(final @PathParam("userId") String userId, final @PathParam("groupId") String groupId, final GroupValue groupValue) throws MalformedURLException, IllegalStateException, IllegalArgumentException {
    Security.checkAuthorization(this.securityContext, userId);
    
    if (groupValue == null) {
      throw new BadRequestException("No group definition provided!");
    }
    
    if ((groupValue.getId() != null) && !groupValue.getId().equals(groupId)) {
      throw new BadRequestException("The identifier of the predicates and classes group in the payload is not the same as the name of the resource.");
    }

    final Group groupById = this.groupsService.verifyGroupExistenceById(userId, groupId);

    final URL location =
        cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(this.uriInfo, groupId);
    
    final Group group = new Group(this.userService.getUser(userId), groupId, groupValue.getLabelPredicates(), groupValue.getDescriptionPredicates(), groupValue.getInstanceOfPredicates(), groupValue.getClassTypes(), groupValue.getPropertyTypes());
    
    if (groupById == null) {
      this.groupsService.create(group);
      return Message.of("A new group has been created AT THE LOCATION you specified")
          .toResponse(Response.Status.CREATED, location, this.uriInfo);
    } else {
      this.groupsService.replace(group);
      return Message
          .of("The group you specified has been fully updated AT THE LOCATION you specified.")
          .toResponse(Response.Status.OK, location, this.uriInfo);
    }
  }
  
  @PUT
  @Path("groups/{groupId}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response put(final @PathParam("groupId") String groupId, final GroupValue group) throws MalformedURLException, IllegalStateException, IllegalArgumentException {
    return put(this.securityContext.getUserPrincipal().getName(), groupId, group);
  }
  
  @DELETE
  @Path("groups/{groupId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteById(final @PathParam("groupId") String groupId) {
    return deleteById(this.securityContext.getUserPrincipal().getName(), groupId);
  }

  @DELETE
  @Path("users/{userId}/groups/{groupId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteById(final @PathParam("userId") String userId,
      final @PathParam("groupId") String groupId) {
    Security.checkAuthorization(this.securityContext, userId);

    try {
      this.groupsService.deleteById(userId, groupId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The group does not exist!", e);
    } catch (final IllegalStateException e) {
      throw new WebApplicationException(e.getMessage(), e, Response.Status.CONFLICT);
    }

    return Message.of("Group deleted.").toResponse(Response.Status.OK, this.uriInfo);
  }
  
  @GET
  @Path("users/{userId}/groups/detected")
  @Produces({MediaType.APPLICATION_JSON})
  public Response detectUsed(final @PathParam("userId") String userId, final @QueryParam("endpoint") URL endpoint) {
    if (endpoint == null) {
      throw new BadRequestException("Endpoint URL must be provided!");
    }
    
    final Set<Group> detectedGroups;
    try {
      detectedGroups = this.groupsService.detectUsed(userId, endpoint);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("No group with the provided name exists!", e);
    }
    
    return Reply.data(Response.Status.OK, detectedGroups, this.uriInfo).toResponse();
  }
  
  @GET
  @Path("groups/detected")
  @Produces({MediaType.APPLICATION_JSON})
  public Response detectUsed(final @QueryParam("endpointUrl") URL endpoint) {
    return detectUsed(this.securityContext.getUserPrincipal().getName(), endpoint);
  }
}
