package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * Service providing the serialization of task result in RDF formats.
 *
 * @author Václav Brodec
 *
 */
public interface RdfExportService {
  /**
   * Serializes the task result.
   * 
   * @param userId user ID
   * @param taskId task ID
   *
   * @return serialized RDF
   * @throws InterruptedException if the execution was interrupted while waiting
   * @throws ExecutionException if the computation threw an exception
   * @throws CancellationException if the computation was cancelled
   * @throws IOException if an I/O exception occurs when creating the output content
   */
  String exportToJsonLd(String userId, String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException;

  /**
   * Serializes the task result.
   * 
   * @param userId user ID
   * @param taskId task ID
   *
   * @return serialized RDF
   * @throws InterruptedException if the execution was interrupted while waiting
   * @throws ExecutionException if the computation threw an exception
   * @throws CancellationException if the computation was cancelled
   * @throws IOException if an I/O exception occurs when creating the output content
   */
  String exportToTurtle(String userId, String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException;
}
