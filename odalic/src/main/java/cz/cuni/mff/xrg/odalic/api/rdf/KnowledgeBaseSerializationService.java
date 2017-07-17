/**
 *
 */
package cz.cuni.mff.xrg.odalic.api.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import cz.cuni.mff.xrg.odalic.api.rdf.values.KnowledgeBaseValue;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;

/**
 * Serializes and de-serializes {@link KnowledgeBase} instances.
 *
 * @author Václav Brodec
 *
 */
public interface KnowledgeBaseSerializationService {

  /**
   * De-serializes the knowledge base definition.
   *
   * @param knowledgeBaseStream input stream in supported format
   * @param userId owner's ID
   * @param KnowledgeBaseId KnowledgeBase ID assigned to the de-serialized knowledge base definition
   * @param baseUri base URI of the application
   * @return de-serialized knowledge base definition
   * @throws IOException when an I/O error occurs during de-serialization
   */
  KnowledgeBase deserialize(final InputStream knowledgeBaseStream, String userId, String KnowledgeBaseId, final URI baseUri)
      throws IOException;

  KnowledgeBase deserialize(String userId, KnowledgeBaseValue knowledgeBaseValue);
  
  /**
   * Serializes the knowledge base definition.
   *
   * @param knowledgeBase serialized knowledge base definition
   * @param baseUri base URI of the application (used as prefix to instance specific URIs)
   * @return serialized knowledge base definition
   */
  String serialize(final KnowledgeBase knowledgeBase, final URI baseUri);


}
