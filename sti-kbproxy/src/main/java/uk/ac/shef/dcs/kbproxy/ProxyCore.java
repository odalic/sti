package uk.ac.shef.dcs.kbproxy;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.kbproxy.model.PropertyType;


public interface ProxyCore {

  void closeConnection() throws ProxyException;

  void commitChanges() throws ProxyException;

  /**
   * get attributes of the class
   * @throws ProxyException 
   */
  List<Attribute> findAttributesOfClazz(String clazzId) throws ProxyException;

  /**
   * Get attributes of the entity candidate (all predicates and object values of the triples where
   * the candidate entity is the subject).
   *
   * Note: Certain predicates may be blacklisted.
   * @throws ProxyException 
   */
  List<Attribute> findAttributesOfEntities(Entity ec) throws ProxyException;

  /**
   * get attributes of the property
   * @throws ProxyException 
   */
  List<Attribute> findAttributesOfProperty(String propertyId) throws ProxyException;

  /**
   * Given a string, fetch candidate entities (classes) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  List<Entity> findClassByFulltext(String pattern, int limit) throws ProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB Candidate entities are those
   * resources for which label or part of the label matches the given content
   *
   * @param content
   * @return
   * @throws ProxyException 
   */
  List<Entity> findEntityCandidates(String content) throws ProxyException;
  
  List<Entity> findEntityCandidates(String content, final ProxyCore dependenciesProxy) throws ProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB that only match certain types
   * @throws ProxyException 
   */
  List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws ProxyException;
  
  List<Entity> findEntityCandidatesOfTypes(String content, final ProxyCore dependenciesProxy, String... types) throws ProxyException;

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
      throws ProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  List<Entity> findResourceByFulltext(String pattern, int limit) throws ProxyException;

  String getName();

  /**
   * Fetches domain of the gives resource.
   *
   * @param uri
   * @return
   * @throws ProxyException
   */
  List<String> getPropertyDomains(String uri) throws ProxyException;

  /**
   * Fetches range of the gives resource.
   *
   * @param uri
   * @return
   * @throws ProxyException
   */
  List<String> getPropertyRanges(String uri) throws ProxyException;

  /**
   * Inserts a new class into the knowledge base
   */
  Entity insertClass(URI uri, String label, Collection<String> alternativeLabels, String superClass)
      throws ProxyException;

  /**
   * Inserts a new concept into the knowledge base
   */
  Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels,
      Collection<String> classes) throws ProxyException;

  /**
   * Inserts a new property into the knowledge base
   */
  Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels,
                        String superProperty, String domain, String range, PropertyType type) throws ProxyException;

  /**
   * Information about whether the knowledge base supports inserting new concepts
   */
  boolean isInsertSupported();

  /**
   * Loads the entity from the knowledge base.
   *
   * @param uri The entity uri.
   * @return The entity or null if no such uri was found in the knowledge base.
   * @throws ProxyException 
   */
  Entity loadEntity(String uri) throws ProxyException;
  
  Entity loadEntity(String uri, ProxyCore dependenciesProxy) throws ProxyException;

  String getResourceLabel(String uri) throws ProxyException;

  List<Attribute> findAttributes(String resourceId) throws ProxyException;

  List<Attribute> findAttributesOfClazz(String clazzId,
      ProxyCore dependenciesProxy) throws ProxyException;

  List<Attribute> findAttributesOfEntities(Entity ec,
      ProxyCore dependenciesProxy) throws ProxyException;
  
  List<Attribute> findAttributesOfProperty(String propertyId,
      ProxyCore dependenciesProxy) throws ProxyException;

  ProxyDefinition getDefinition();
}
