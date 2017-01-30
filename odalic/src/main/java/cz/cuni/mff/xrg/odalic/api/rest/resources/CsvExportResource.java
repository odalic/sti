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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.csvexport.CsvExportService;
import cz.cuni.mff.xrg.odalic.users.Role;

/**
 * Definition of the resource providing a complementary part of the result (extended CSV data) to
 * the annotations represented by {@link AnnotatedTable}.
 * 
 * @author Václav Brodec
 *
 */
@Component
@Path("/")
public final class CsvExportResource {

  public static final String TEXT_CSV_MEDIA_TYPE = "text/csv";

  @Context
  private SecurityContext securityContext;
  
  private final CsvExportService csvExportService;

  @Autowired
  public CsvExportResource(CsvExportService csvExportService) {
    Preconditions.checkNotNull(csvExportService);

    this.csvExportService = csvExportService;
  }

  @GET
  @Path("users/{userId}/tasks/{taskId}/result/csv-export")
  @Produces(TEXT_CSV_MEDIA_TYPE)
  @Secured({Role.ADMINISTRATOR, Role.USER})
  public Response getCsvExport(final @PathParam("userId") String userId, final @PathParam("taskId") String taskId) throws InterruptedException, IOException {
    Security.checkAuthorization(this.securityContext, userId);
    
    final String csvContent;
    try {
      csvContent = csvExportService.getExtendedCsvForTaskId(userId, taskId);
    } catch (final CancellationException | ExecutionException e) {
      throw new NotFoundException(
          "The underlying CSV is not available, because the processing did not finish. Check the result first!");
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("No such underlying CSV found!", e);
    }

    return Response.ok(csvContent).build();
  }
  
  @GET
  @Path("tasks/{taskId}/result/csv-export")
  @Produces(TEXT_CSV_MEDIA_TYPE)
  @Secured({Role.ADMINISTRATOR, Role.USER})
  public Response getCsvExport(final @PathParam("taskId") String taskId) throws InterruptedException, IOException {
    return getCsvExport(securityContext.getUserPrincipal().getName(), taskId);
  }
}
