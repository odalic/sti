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
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.entities.ClassProposal;
import cz.cuni.mff.xrg.odalic.entities.EntitiesService;
import cz.cuni.mff.xrg.odalic.entities.PropertyProposal;
import cz.cuni.mff.xrg.odalic.entities.ResourceProposal;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.users.Role;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.sti.STIException;

/**
 * Entities resource definition.
 *
 * @author Václav Brodec
 *
 */
@Component
@Path("/bases/{base}/entities/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class EntitiesResource {

  private final EntitiesService entitiesService;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public EntitiesResource(final EntitiesService entitiesService) {
    Preconditions.checkNotNull(entitiesService);
    this.entitiesService = entitiesService;
  }

  @POST
  @Path("classes")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(@PathParam("base") final String base, final ClassProposal proposal)
      throws KBProxyException, STIException, IOException {
    final Entity createdClass;
    try {
      createdClass = this.entitiesService.propose(new KnowledgeBase(base), proposal);
    } catch (final IllegalArgumentException e) {
      throw new WebApplicationException("The class already exists!", e, Response.Status.CONFLICT);
    }

    return Reply.data(Response.Status.OK, createdClass, this.uriInfo).toResponse();
  }

  @POST
  @Path("properties")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(@PathParam("base") final String base, final PropertyProposal proposal)
      throws KBProxyException, STIException, IOException {
    final Entity createdProperty;
    try {
      createdProperty = this.entitiesService.propose(new KnowledgeBase(base), proposal);
    } catch (final IllegalArgumentException e) {
      throw new WebApplicationException("The property already exists!", e,
          Response.Status.CONFLICT);
    }

    return Reply.data(Response.Status.OK, createdProperty, this.uriInfo).toResponse();
  }

  @POST
  @Path("resources")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(@PathParam("base") final String base, final ResourceProposal proposal)
      throws KBProxyException, STIException, IOException {
    final Entity createdEntity;
    try {
      createdEntity = this.entitiesService.propose(new KnowledgeBase(base), proposal);
    } catch (final IllegalArgumentException e) {
      throw new WebApplicationException("The resource already exists!", e,
          Response.Status.CONFLICT);
    }

    return Reply.data(Response.Status.OK, createdEntity, this.uriInfo).toResponse();
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response search(@PathParam("base") final String base,
      @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit)
      throws KBProxyException, STIException, IOException {
    return searchResources(base, query, limit);
  }

  @GET
  @Path("classes")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchClasses(@PathParam("base") final String base,
      @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit)
      throws KBProxyException, STIException, IOException {
    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }

    final NavigableSet<Entity> result;
    try {
      result = this.entitiesService.searchClasses(new KnowledgeBase(base), query, limit);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, result, this.uriInfo).toResponse();
  }

  @GET
  @Path("properties")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchProperties(@PathParam("base") final String base,
      @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit,
      @QueryParam("domain") final URI domain, @QueryParam("range") final URI range)
      throws KBProxyException, STIException, IOException {
    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }

    final NavigableSet<Entity> result;
    try {
      result = this.entitiesService.searchProperties(new KnowledgeBase(base), query, limit, domain,
          range);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, result, this.uriInfo).toResponse();
  }

  @GET
  @Path("resources")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchResources(@PathParam("base") final String base,
      @QueryParam("query") final String query,
      @DefaultValue("20") @QueryParam("limit") final Integer limit)
      throws KBProxyException, STIException, IOException {
    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }

    final NavigableSet<Entity> result;
    try {
      result = this.entitiesService.searchResources(new KnowledgeBase(base), query, limit);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, result, this.uriInfo).toResponse();
  }
}
