package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.util.NavigableSet;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.users.Role;
import uk.ac.shef.dcs.sti.STIException;

/**
 * Knowledge bases resource definition.
 * 
 * @author Václav Brodec
 *
 */
@Component
@Path("bases")
public final class BasesResource {

  private final BasesService basesService;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public BasesResource(BasesService basesService) {
    Preconditions.checkNotNull(basesService);

    this.basesService = basesService;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Secured({Role.ADMINISTRATOR, Role.USER})
  public Response getBases(
      @QueryParam(value = "modifiable") @DefaultValue("false") boolean modifiable) throws STIException, IOException {
    final NavigableSet<KnowledgeBase> bases;
    if (modifiable) {
      bases = basesService.getInsertSupportingBases();
    } else {
      bases = basesService.getBases();
    }

    return Reply.data(Response.Status.OK, bases, uriInfo).toResponse();
  }
}
