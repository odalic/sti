package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.net.URI;
import java.util.NavigableSet;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.entities.ClassProposal;
import cz.cuni.mff.xrg.odalic.entities.EntitiesService;
import cz.cuni.mff.xrg.odalic.entities.PropertyProposal;
import cz.cuni.mff.xrg.odalic.entities.ResourceProposal;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.sti.STIException;

/**
 * Entities resource definition.
 * 
 * @author Václav Brodec
 *
 */
@Component
@Path("{base}/entities")
public final class EntitiesResource {

  private final EntitiesService entitiesService;
  
  @Context
  private UriInfo uriInfo;

  @Autowired
  public EntitiesResource(EntitiesService entitiesService) {
    Preconditions.checkNotNull(entitiesService);
    this.entitiesService = entitiesService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response search(@PathParam("base") String base, @QueryParam("query") String query,
      @DefaultValue("20") @QueryParam("limit") Integer limit) throws KBProxyException, STIException, IOException {
    return searchResources(base, query, limit);
  }
  
  @GET
  @Path("classes")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchClasses(@PathParam("base") String base, @QueryParam("query") String query,
      @DefaultValue("20") @QueryParam("limit") Integer limit) throws KBProxyException, STIException, IOException {
    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }

    final NavigableSet<Entity> result;
    try {
      result = entitiesService.searchClasses(new KnowledgeBase(base), query, limit);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, result, uriInfo).toResponse();
  }
  
  @GET
  @Path("resources")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchResources(@PathParam("base") String base, @QueryParam("query") String query,
      @DefaultValue("20") @QueryParam("limit") Integer limit) throws KBProxyException, STIException, IOException {
    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }

    final NavigableSet<Entity> result;
    try {
      result = entitiesService.searchResources(new KnowledgeBase(base), query, limit);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, result, uriInfo).toResponse();
  }
  
  @GET
  @Path("properties")
  @Produces({MediaType.APPLICATION_JSON})
  public Response searchProperties(@PathParam("base") String base, @QueryParam("query") String query,
      @DefaultValue("20") @QueryParam("limit") Integer limit, @QueryParam("domain") URI domain, @QueryParam("range") URI range) throws KBProxyException, STIException, IOException {
    if (query == null) {
      throw new BadRequestException("Query not provided!");
    }
    
    final NavigableSet<Entity> result;
    try {
      result = entitiesService.searchProperties(new KnowledgeBase(base), query, limit, domain, range);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, result, uriInfo).toResponse();
  }

  @POST
  @Path("classes")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(@PathParam("base") String base, ClassProposal proposal) throws KBProxyException, STIException, IOException {
    final Entity createdClass;
    try {
      createdClass = this.entitiesService.propose(new KnowledgeBase(base), proposal);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, createdClass, uriInfo).toResponse();
  }

  @POST
  @Path("resources")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(@PathParam("base") String base, ResourceProposal proposal) throws KBProxyException, STIException, IOException {
    final Entity createdEntity;
    try {
      createdEntity = this.entitiesService.propose(new KnowledgeBase(base), proposal);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, createdEntity, uriInfo).toResponse();
  }
  
  @POST
  @Path("properties")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(@PathParam("base") String base, PropertyProposal proposal) throws KBProxyException, STIException, IOException {
    final Entity createdProperty;
    try {
      createdProperty = this.entitiesService.propose(new KnowledgeBase(base), proposal);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException(e);
    }

    return Reply.data(Response.Status.OK, createdProperty, uriInfo).toResponse();
  }
}
