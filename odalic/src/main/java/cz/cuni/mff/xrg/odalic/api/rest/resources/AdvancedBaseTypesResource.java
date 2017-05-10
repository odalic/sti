package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.util.SortedSet;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import cz.cuni.mff.xrg.odalic.bases.AdvancedBaseType;
import cz.cuni.mff.xrg.odalic.bases.AdvancedBaseTypesService;
import cz.cuni.mff.xrg.odalic.users.Role;

/**
 * Advanced knowledge base types resource definition.
 *
 * @author Václav Brodec
 *
 */
@Component
@Path("/advanced-base-types")
public final class AdvancedBaseTypesResource {

  private final AdvancedBaseTypesService advancedBaseTypesService;

  @Context
  private SecurityContext securityContext;
  
  @Context
  private UriInfo uriInfo;

  @Autowired
  public AdvancedBaseTypesResource(final AdvancedBaseTypesService advancedBaseTypesService) {
    Preconditions.checkNotNull(advancedBaseTypesService);

    this.advancedBaseTypesService = advancedBaseTypesService;
  }
  
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Secured({Role.ADMINISTRATOR, Role.USER})
  public Response getBaseTypes() {
    final SortedSet<AdvancedBaseType> types = this.advancedBaseTypesService.getTypes();

    return Reply.data(Response.Status.OK, types, this.uriInfo).toResponse();
  }
  
  @GET
  @Path("{name}")
  @Produces({MediaType.APPLICATION_JSON})
  @Secured({Role.ADMINISTRATOR, Role.USER})
  public Response getBaseType(final @PathParam("name") String name) {
    final AdvancedBaseType type = this.advancedBaseTypesService.verifyTypeExistenceByName(name);
    if (type == null) {
      throw new NotFoundException("Unknown type name!");
    }
    
    return Reply.data(Response.Status.OK, type, this.uriInfo).toResponse();
  }
}
