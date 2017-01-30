package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTableService;
import cz.cuni.mff.xrg.odalic.users.Role;

/**
 * Definition of the resource providing a part of the result in the form of table annotations.
 * 
 * @author Václav Brodec
 * 
 * @see AnnotatedTable format of the annotations
 */
@Component
@Path("/")
public final class AnnotatedTableResource {

  private final AnnotatedTableService annotatedTableService;
  
  @Context
  private SecurityContext securityContext;

  @Autowired
  public AnnotatedTableResource(AnnotatedTableService annotatedTableService) {
    Preconditions.checkNotNull(annotatedTableService);

    this.annotatedTableService = annotatedTableService;
  }

  @GET
  @Path("users/{userId}/tasks/{taskId}/result/annotated-table")
  @Produces(MediaType.APPLICATION_JSON)
  @Secured({Role.ADMINISTRATOR, Role.USER})
  public Response getAnnotatedTable(final @PathParam("userId") String userId, final @PathParam("taskId") String taskId)
      throws InterruptedException, ExecutionException, CancellationException, IOException {
    Security.checkAuthorization(this.securityContext, userId);
    
    final AnnotatedTable table;
    try {
      table = annotatedTableService.getAnnotatedTableForTaskId(userId, taskId);
    } catch (final CancellationException | ExecutionException e) {
      throw new NotFoundException(
          "Annotated table is not available, because the processing did not finish. Check the result first!", e);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The task has not been scheduled or does not exist!", e);
    }

    return Response.ok(table).build();
  }
  
  @GET
  @Path("tasks/{taskId}/result/annotated-table")
  @Produces(MediaType.APPLICATION_JSON)
  @Secured({Role.ADMINISTRATOR, Role.USER})
  public Response getAnnotatedTable(final @PathParam("taskId") String taskId)
      throws InterruptedException, ExecutionException, CancellationException, IOException {
    return getAnnotatedTable(securityContext.getUserPrincipal().getName(), taskId);
  }
}
