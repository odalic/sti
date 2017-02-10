/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import cz.cuni.mff.xrg.odalic.tasks.Task;

/**
 * Serializes and de-serializes {@link Task} instances to and from RDF representation.
 * 
 * @author Václav Brodec
 *
 */
public interface TaskRdfSerializationService {

  /**
   * Serialized the task.
   * 
   * @param task serialized task
   * @param baseUri base URI of the application
   * @return serialized task
   */
  String serialize(final Task task, final URI baseUri);

  /**
   * De-serializes the task.
   * 
   * @param taskStream input stream in supported RDF notation
   * @param userId owner's ID
   * @param taskId task ID assigned to the de-serialized task
   * @param baseUri base URI of the application
   * @return de-serialized task
   * @throws IOException when an I/O error occurs during de-serialization
   */
  Task deserialize(final InputStream taskStream, String userId, String taskId, final URI baseUri)
      throws IOException;
}