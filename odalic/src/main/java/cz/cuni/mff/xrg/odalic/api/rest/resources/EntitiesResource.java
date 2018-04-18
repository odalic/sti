package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.net.URI;
import java.util.NavigableSet;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
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
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.entities.ClassProposal;
import cz.cuni.mff.xrg.odalic.entities.EntitiesService;
import cz.cuni.mff.xrg.odalic.entities.PropertyProposal;
import cz.cuni.mff.xrg.odalic.entities.ResourceProposal;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.users.Role;
import uk.ac.shef.dcs.kbproxy.ProxyException;
import uk.ac.shef.dcs.sti.STIException;

/**
 * Entities resource definition.
 *
 * @author Václav Brodec
 *
 */
@Component
@Path("/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class EntitiesResource {

  private final EntitiesService entitiesService;
  private final BasesService basesService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public EntitiesResource(final EntitiesService entitiesService, final BasesService basesService) {
    Preconditions.checkNotNull(entitiesService, "The entitiesService cannot be null!");
    Preconditions.checkNotNull(basesService, "The basesService cannot be null!");

    this.entitiesService = entitiesService;
    this.basesService = basesService;
  }

  @POST
  @Path("users/{userId}/bases/{base}/entities/classes")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(final @PathParam("userId") String userId,
      @PathParam("base") final String baseName, final ClassProposal proposal)
      throws ProxyException, STIException, IOException {
    Security.checkAuthorization(this.securityContext, userId);

    final KnowledgeBase base = getBase(userId, baseName);

    final Entity createdClass;
    try {
      createdClass = this.entitiesService.propose(base, proposal);
    } catch (final IllegalArgumentException e) {
      throw new WebApplicationException("The class already exists!", e, Response.Status.CONFLICT);
    }

    return Reply.data(Response.Status.OK, createdClass, this.uriInfo).toResponse();
  }

  @POST
  @Path("/bases/{base}/entities/classes")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(@PathParam("base") final String baseName, final ClassProposal proposal)
      throws ProxyException, STIException, IOException {
    return propose(this.securityContext.getUserPrincipal().getName(), baseName, proposal);
  }

  @POST
  @Path("users/{userId}/bases/{base}/entities/properties")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(final @PathParam("userId") String userId,
      final @PathParam("base") String baseName, final PropertyProposal proposal)
      throws ProxyException, STIException, IOException {
    Security.checkAuthorization(this.securityContext, userId);

    final KnowledgeBase base = getBase(userId, baseName);

    // FIXME: Client 'undefined' suffix instead of null bug workaround.
    final Entity createdProperty;
    if (proposal != null && proposal.getSuffix() != null && proposal.getSuffix().equals(URI.create("undefined"))) {
      try {
        createdProperty = this.entitiesService.propose(base, new PropertyProposal(proposal.getLabel(), proposal.getAlternativeLabels(), null, proposal.getSuperProperty(), proposal.getDomain(), proposal.getRange(), proposal.getType()));
      } catch (final IllegalArgumentException e) {
        throw new WebApplicationException("The property already exists!", e,
            Response.Status.CONFLICT);
      }
    } else {
      try {
        createdProperty = this.entitiesService.propose(base, proposal);
      } catch (final IllegalArgumentException e) {
        throw new WebApplicationException("The property already exists!", e,
            Response.Status.CONFLICT);
      }
    }
    
    return Reply.data(Response.Status.OK, createdProperty, this.uriInfo).toResponse();
  }

  @POST
  @Path("bases/{base}/entities/properties")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(final @PathParam("base") String baseName, final PropertyProposal proposal)
      throws ProxyException, STIException, IOException {
    return propose(this.securityContext.getUserPrincipal().getName(), baseName, proposal);
  }

  private KnowledgeBase getBase(final String userId, final String baseName) {
    try {
      return this.basesService.getByName(userId, baseName);
    } catch (final IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), e);
    }
  }

  @POST
  @Path("users/{userId}/bases/{base}/entities/resources")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(final @PathParam("userId") String userId,
      @PathParam("base") final String baseName, final ResourceProposal proposal)
      throws ProxyException, STIException, IOException {
    Security.checkAuthorization(this.securityContext, userId);

    final KnowledgeBase base = getBase(userId, baseName);

    final Entity createdEntity;
    try {
      createdEntity = this.entitiesService.propose(base, proposal);
    } catch (final IllegalArgumentException e) {
      throw new WebApplicationException("The resource already exists!", e,
          Response.Status.CONFLICT);
    }

    return Reply.data(Response.Status.OK, createdEntity, this.uriInfo).toResponse();
  }

  @POST
  @Path("bases/{base}/entities/resources")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(@PathParam("base") final String baseName, final ResourceProposal proposal)
      throws ProxyException, STIException, IOException {
    return propose(this.securityContext.getUserPrincipal().getName(), baseName, proposal);
  }

  @GET
  @Path("users/{userId}/bases/{base}/entities")
  @Produces({MediaType.APPLICATION_JSON})
  public Response search(final @PathParam("userId") String userId,
      @PathParam("base") final String baseName, @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit)
      throws ProxyException, STIException, IOException {
    return searchResources(userId, baseName, query, limit);
  }

  @GET
  @Path("bases/{base}/entities")
  @Produces({MediaType.APPLICATION_JSON})
  public Response search(@PathParam("base") final String baseName,
      @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit)
      throws ProxyException, STIException, IOException {
    return search(this.securityContext.getUserPrincipal().getName(), baseName, query, limit);
  }

  @GET
  @Path("users/{userId}/bases/{base}/entities/classes")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchClasses(final @PathParam("userId") String userId,
      @PathParam("base") final String baseName, @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit)
      throws ProxyException, STIException, IOException {
    Security.checkAuthorization(this.securityContext, userId);

    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }

    final KnowledgeBase base = getBase(userId, baseName);

    final NavigableSet<Entity> result;
    try {
      result = this.entitiesService.searchClasses(base, query, limit);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, result, this.uriInfo).toResponse();
  }

  @GET
  @Path("bases/{base}/entities/classes")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchClasses(@PathParam("base") final String baseName,
      @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit)
      throws ProxyException, STIException, IOException {
    return searchClasses(this.securityContext.getUserPrincipal().getName(), baseName, query, limit);
  }

  @GET
  @Path("users/{userId}/bases/{base}/entities/properties")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchProperties(final @PathParam("userId") String userId,
      @PathParam("base") final String baseName, @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit,
      @QueryParam("domain") final URI domain, @QueryParam("range") final URI range)
      throws ProxyException, STIException, IOException {
    Security.checkAuthorization(this.securityContext, userId);

    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }

    final KnowledgeBase base = getBase(userId, baseName);

    final NavigableSet<Entity> result;
    try {
      result = this.entitiesService.searchProperties(base, query, limit, domain, range);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, result, this.uriInfo).toResponse();
  }

  @GET
  @Path("bases/{base}/entities/properties")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchProperties(@PathParam("base") final String baseName,
      @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit,
      @QueryParam("domain") final URI domain, @QueryParam("range") final URI range)
      throws ProxyException, STIException, IOException {
    return searchProperties(this.securityContext.getUserPrincipal().getName(), baseName, query, limit, domain, range);
  }

  @GET
  @Path("users/{userId}/bases/{base}/entities/resources")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchResources(final @PathParam("userId") String userId,
      @PathParam("base") final String baseName, @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit)
      throws ProxyException, STIException, IOException {
    Security.checkAuthorization(this.securityContext, userId);

    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }

    final KnowledgeBase base = getBase(userId, baseName);

    final NavigableSet<Entity> result;
    try {
      result = this.entitiesService.searchResources(base, query, limit);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, result, this.uriInfo).toResponse();
  }

  @GET
  @Path("bases/{base}/entities/resources")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchResources(@PathParam("base") final String baseName,
      @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit)
      throws ProxyException, STIException, IOException {
    return searchResources(this.securityContext.getUserPrincipal().getName(), baseName, query,
        limit);
  }
}
