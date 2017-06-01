/**
 *
 */
package cz.cuni.mff.xrg.odalic.api.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import cz.cuni.mff.xrg.odalic.tasks.Task;

/**
 * Serializes and de-serializes {@link Task} instances.
 *
 * @author Václav Brodec
 *
 */
public interface TaskSerializationService {

  /**
   * De-serializes the task.
   *
   * @param taskStream input stream in supported format
   * @param userId owner's ID
   * @param taskId task ID assigned to the de-serialized task
   * @param baseUri base URI of the application
   * @return de-serialized task
   * @throws IOException when an I/O error occurs during de-serialization
   */
  Task deserialize(final InputStream taskStream, String userId, String taskId, final URI baseUri)
      throws IOException;

  /**
   * Serializes the task.
   *
   * @param task serialized task
   * @param baseUri base URI of the application (used as prefix to instance specific URIs)
   * @return serialized task
   */
  String serialize(final Task task, final URI baseUri);
}
