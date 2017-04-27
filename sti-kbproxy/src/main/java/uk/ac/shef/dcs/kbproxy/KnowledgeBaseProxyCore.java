package uk.ac.shef.dcs.kbproxy;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;


public interface KnowledgeBaseProxyCore {

  void closeConnection() throws KBProxyException;

  void commitChanges() throws KBProxyException;

  /**
   * get attributes of the class
   * @throws KBProxyException 
   */
  List<Attribute> findAttributesOfClazz(String clazzId) throws KBProxyException;

  /**
   * Get attributes of the entity candidate (all predicates and object values of the triples where
   * the candidate entity is the subject).
   *
   * Note: Certain predicates may be blacklisted.
   * @throws KBProxyException 
   */
  List<Attribute> findAttributesOfEntities(Entity ec) throws KBProxyException;

  /**
   * get attributes of the property
   * @throws KBProxyException 
   */
  List<Attribute> findAttributesOfProperty(String propertyId) throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (classes) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  List<Entity> findClassByFulltext(String pattern, int limit) throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB Candidate entities are those
   * resources for which label or part of the label matches the given content
   *
   * @param content
   * @return
   * @throws KBProxyException 
   */
  List<Entity> findEntityCandidates(String content) throws KBProxyException;
  
  List<Entity> findEntityCandidates(String content, final KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB that only match certain types
   * @throws KBProxyException 
   */
  List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws KBProxyException;
  
  List<Entity> findEntityCandidatesOfTypes(String content, final KnowledgeBaseProxyCore dependenciesProxy, String... types) throws KBProxyException;

  /**
   * compute the seamntic similarity between an entity and a class
   */
  Double findEntityClazzSimilarity(String entity_id, String clazz_url);

  /**
   * @return the granularity of the class in the KB.
   */
  Double findGranularityOfClazz(String clazz);

  /**
   * Given a string, fetch candidate entities (predicates) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  List<Entity> findPredicateByFulltext(String pattern, int limit, URI domain, URI range)
      throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  List<Entity> findResourceByFulltext(String pattern, int limit) throws KBProxyException;

  String getName();

  /**
   * Fetches domain of the gives resource.
   *
   * @param uri
   * @return
   * @throws KBProxyException
   */
  List<String> getPropertyDomains(String uri) throws KBProxyException;

  /**
   * Fetches range of the gives resource.
   *
   * @param uri
   * @return
   * @throws KBProxyException
   */
  List<String> getPropertyRanges(String uri) throws KBProxyException;

  /**
   * Inserts a new class into the knowledge base
   */
  Entity insertClass(URI uri, String label, Collection<String> alternativeLabels, String superClass)
      throws KBProxyException;

  /**
   * Inserts a new concept into the knowledge base
   */
  Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels,
      Collection<String> classes) throws KBProxyException;

  /**
   * Inserts a new property into the knowledge base
   */
  Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels,
      String superProperty, String domain, String range) throws KBProxyException;

  /**
   * Information about whether the knowledge base supports inserting new concepts
   */
  boolean isInsertSupported();

  /**
   * Loads the entity from the knowledge base.
   *
   * @param uri The entity uri.
   * @return The entity or null if no such uri was found in the knowledge base.
   * @throws KBProxyException 
   */
  Entity loadEntity(String uri) throws KBProxyException;
  
  Entity loadEntity(String uri, KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException;

  String getResourceLabel(String uri) throws KBProxyException;

  List<Attribute> findAttributes(String resourceId) throws KBProxyException;

  List<Attribute> findAttributesOfClazz(String clazzId,
      KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException;

  List<Attribute> findAttributesOfEntities(Entity ec,
      KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException;
  
  List<Attribute> findAttributesOfProperty(String propertyId,
      KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException;

}
