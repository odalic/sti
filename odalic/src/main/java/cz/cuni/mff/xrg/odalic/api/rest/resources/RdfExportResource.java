package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.outputs.rdfexport.RdfExportService;

/**
 * Definition of the resource providing the result as serialized RDF data.
 * 
 * @author Václav Brodec
 *
 */
@Component
@Path("/tasks/{id}/result/rdf-export")
public final class RdfExportResource {

  public static final String TURTLE_MIME_TYPE = "text/turtle";
  public static final String JSON_LD_MIME_TYPE = "application/ld+json";

  private static final Logger LOGGER = LoggerFactory.getLogger(RdfExportResource.class);

  private final RdfExportService rdfExportService;

  @Autowired
  public RdfExportResource(RdfExportService RdfExportService) {
    Preconditions.checkNotNull(RdfExportService);

    this.rdfExportService = RdfExportService;
  }

  @GET
  @Produces(TURTLE_MIME_TYPE)
  public Response getTurtleExport(@PathParam("id") String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    final String rdfContent;
    try {
      rdfContent = rdfExportService.exportToTurtle(taskId);
    } catch (final CancellationException | ExecutionException e) {
      LOGGER.error(
          "RDF export is not available, because the processing did not finish. Check the result first!",
          e);

      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (final IllegalArgumentException e) {
      LOGGER.error("The task has not been scheduled or does not exist!", e);

      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok(rdfContent).build();
  }

  @GET
  @Produces(JSON_LD_MIME_TYPE)
  public Response getJsonLdExport(@PathParam("id") String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    final String rdfContent;
    try {
      rdfContent = rdfExportService.exportToJsonLd(taskId);
    } catch (final CancellationException | ExecutionException e) {
      LOGGER.error(
          "RDF export is not available, because the processing did not finish. Check the result first!",
          e);

      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (final IllegalArgumentException e) {
      LOGGER.error("The task has not been scheduled or does not exist!", e);

      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok(rdfContent).build();
  }

  /**
   * Alternative method for client that are unable to provide Accept headers.
   */
  @GET
  @Path("turtle")
  @Produces(TURTLE_MIME_TYPE)
  public Response getTurtleAlternativeExport(@PathParam("id") String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    return getTurtleExport(taskId);
  }

  /**
   * Alternative method for client that are unable to provide Accept headers.
   */
  @GET
  @Path("json-ld")
  @Produces(JSON_LD_MIME_TYPE)
  public Response getJsonLdAlternativeExport(@PathParam("id") String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    return getJsonLdExport(taskId);
  }
}
