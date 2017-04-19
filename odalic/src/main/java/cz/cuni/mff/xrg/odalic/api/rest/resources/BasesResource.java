package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.NavigableSet;
import java.util.Set;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.api.rest.values.KnowledgeBaseValue;
import cz.cuni.mff.xrg.odalic.bases.AdvancedBaseType;
import cz.cuni.mff.xrg.odalic.bases.AdvancedBaseTypesService;
import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.groups.Group;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * Knowledge bases resource definition.
 *
 * @author Václav Brodec
 *
 */
@Component
@Path("/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class BasesResource {

  private final BasesService basesService;
  private final UserService userService;
  private final AdvancedBaseTypesService advancedBaseTypesService;
  private final GroupsService groupsService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;


  @Autowired
  public BasesResource(final BasesService basesService, final UserService userService,
      final AdvancedBaseTypesService advancedBaseTypesService, final GroupsService groupsService) {
    Preconditions.checkNotNull(basesService);
    Preconditions.checkNotNull(userService);
    Preconditions.checkNotNull(advancedBaseTypesService);
    Preconditions.checkNotNull(groupsService);

    this.basesService = basesService;
    this.userService = userService;
    this.advancedBaseTypesService = advancedBaseTypesService;
    this.groupsService = groupsService;
  }

  @GET
  @Path("users/{userId}/bases")
  @Produces({MediaType.APPLICATION_JSON})
  public Response getBases(final @PathParam("userId") String userId,
      @QueryParam(value = "modifiable") @DefaultValue("false") final boolean modifiable) {
    Security.checkAuthorization(this.securityContext, userId);

    final NavigableSet<KnowledgeBase> bases;
    if (modifiable) {
      bases = this.basesService.getInsertSupportingBases(userId);
    } else {
      bases = this.basesService.getBases(userId);
    }

    return Reply.data(Response.Status.OK, bases, this.uriInfo).toResponse();
  }

  @GET
  @Path("bases")
  @Produces({MediaType.APPLICATION_JSON})
  public Response getBases(
      @QueryParam(value = "modifiable") @DefaultValue("false") final boolean modifiable) {
    return getBases(this.securityContext.getUserPrincipal().getName(), modifiable);
  }

  @GET
  @Path("users/{userId}/bases/{name}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response get(final @PathParam("userId") String userId,
      final @PathParam("name") String name) {
    Security.checkAuthorization(this.securityContext, userId);

    final KnowledgeBase base;
    try {
      base = this.basesService.getByName(userId, name);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("No base with the provided name exists!", e);
    }

    return Reply.data(Response.Status.OK, base, this.uriInfo).toResponse();
  }

  @GET
  @Path("bases/{name}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response get(final @PathParam("name") String name) {
    return get(this.securityContext.getUserPrincipal().getName(), name);
  }

  @PUT
  @Path("/users/{userId}/bases/{name}")
  @Produces({MediaType.APPLICATION_JSON})
  public Response put(final @PathParam("userId") String userId,
      final @PathParam("name") String name, final KnowledgeBaseValue baseValue)
      throws MalformedURLException, IllegalStateException, IllegalArgumentException {
    Security.checkAuthorization(this.securityContext, userId);

    if (baseValue == null) {
      throw new BadRequestException("No base definition provided!");
    }

    if ((baseValue.getName() != null) && !baseValue.getName().equals(name)) {
      throw new BadRequestException(
          "The name of the base in the payload is not the same as the name of the resource.");
    }

    final User owner = this.userService.getUser(userId);
    final AdvancedBaseType advancedType =
        this.advancedBaseTypesService.getType(baseValue.getAdvancedType());
    final Set<Group> selectedGroups = baseValue.getSelectedGroups().stream()
        .map(e -> this.groupsService.getGroup(userId, e)).collect(ImmutableSet.toImmutableSet());

    final KnowledgeBase base = new KnowledgeBase(owner, name, baseValue.getEndpoint(),
        baseValue.getDescription() == null ? "" : baseValue.getDescription(), baseValue.getTextSearchingMethod(), baseValue.getLanguageTag(),
        baseValue.getSkippedAttributes(), baseValue.getSkippedClasses(), selectedGroups,
        baseValue.isInsertEnabled(), baseValue.getInsertGraph(), baseValue.getUserClassesPrefix(),
        baseValue.getUserResourcesPrefix(), advancedType, baseValue.getAdvancedProperties());

    final KnowledgeBase baseById = this.basesService.verifyBaseExistenceByName(userId, name);

    final URL location =
        cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(this.uriInfo, name);

    if (baseById == null) {
      this.basesService.create(base);
      return Message.of("A new base has been created AT THE LOCATION you specified")
          .toResponse(Response.Status.CREATED, location, this.uriInfo);
    } else {
      this.basesService.replace(base);
      return Message
          .of("The base you specified has been fully updated AT THE LOCATION you specified.")
          .toResponse(Response.Status.OK, location, this.uriInfo);
    }
  }
}
