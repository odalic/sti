package uk.ac.shef.dcs.kbproxy;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;

public class ResilientKnowledgeBaseProxy implements KnowledgeBaseProxy {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  
  private final KnowledgeBaseProxyCore core;
  
  protected interface Func<Type> {
    Type Do() throws Exception;
  }
  
  public ResilientKnowledgeBaseProxy(final KnowledgeBaseProxyCore core) {
    Preconditions.checkNotNull(core);
    
    this.core = core;
  }

  @Override
  public void closeConnection() throws KBProxyException {
    this.core.closeConnection();
  }

  @Override
  public void commitChanges() throws KBProxyException {
    this.core.commitChanges();
  }

  private <ResultType> KBProxyResult<ResultType> Do(final Func<ResultType> func,
      final ResultType defaultValue) {
    try {
      final ResultType result = func.Do();
      return new KBProxyResult<>(result);
    } catch (final Exception ex) {
      this.logger.error(ex.getLocalizedMessage(), ex);
      
      return new KBProxyResult<>(defaultValue, ex.getLocalizedMessage());
    }
  }

  /**
   * get attributes of the class
   */
  @Override
  public KBProxyResult<List<Attribute>> findAttributesOfClazz(final String clazzId) {
    return Do(() -> this.core.findAttributesOfClazz(clazzId), new ArrayList<Attribute>());
  }

  /**
   * Get attributes of the entity candidate (all predicates and object values of the triples where
   * the candidate entity is the subject).
   *
   * Note: Certain predicates may be blacklisted.
   */
  @Override
  public KBProxyResult<List<Attribute>> findAttributesOfEntities(final Entity ec) {
    return Do(() -> this.core.findAttributesOfEntities(ec), new ArrayList<Attribute>());
  }

  /**
   * get attributes of the property
   */
  @Override
  public KBProxyResult<List<Attribute>> findAttributesOfProperty(final String propertyId) {
    return Do(() -> this.core.findAttributesOfProperty(propertyId), new ArrayList<Attribute>());
  }

  /**
   * Given a string, fetch candidate entities (classes) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  @Override
  public List<Entity> findClassByFulltext(String pattern, int limit)
      throws KBProxyException {
    return this.core.findClassByFulltext(pattern, limit);
  }

  /**
   * Given a string, fetch candidate entities (resources) from the KB Candidate entities are those
   * resources for which label or part of the label matches the given content
   *
   * @param content
   * @return
   */
  @Override
  public KBProxyResult<List<Entity>> findEntityCandidates(final String content) {
    return Do(() -> this.core.findEntityCandidates(content), new ArrayList<Entity>());
  }
  
  @Override
  public KBProxyResult<List<Entity>> findEntityCandidates(final String content, final KnowledgeBaseProxyCore dependenciesProxy) {
    return Do(() -> this.core.findEntityCandidates(content, dependenciesProxy), new ArrayList<Entity>());
  }

  /**
   * Given a string, fetch candidate entities (resources) from the KB that only match certain types
   */
  @Override
  public KBProxyResult<List<Entity>> findEntityCandidatesOfTypes(final String content,
      final String... types) {
    return Do(() -> this.core.findEntityCandidatesOfTypes(content, types), new ArrayList<Entity>());
  }
  
  @Override
  public KBProxyResult<List<Entity>> findEntityCandidatesOfTypes(final String content, final KnowledgeBaseProxyCore dependenciesProxy,
      final String... types) {
    return Do(() -> this.core.findEntityCandidatesOfTypes(content, dependenciesProxy, types), new ArrayList<Entity>());
  }

  /**
   * compute the seamntic similarity between an entity and a class
   */
  @Override
  public KBProxyResult<Double> findEntityClazzSimilarity(final String entity_id,
      final String clazz_url) {
    return Do(() -> this.core.findEntityClazzSimilarity(entity_id, clazz_url), 0.0);
  }

  /**
   * @return the granularity of the class in the KB.
   */
  @Override
  public KBProxyResult<Double> findGranularityOfClazz(final String clazz) {
    return Do(() -> this.core.findGranularityOfClazz(clazz), 0.0);
  }

  /**
   * Given a string, fetch candidate entities (predicates) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  @Override
  public List<Entity> findPredicateByFulltext(String pattern, int limit, URI domain,
      URI range) throws KBProxyException {
    return this.core.findPredicateByFulltext(pattern, limit, domain, range);
  }

  /**
   * Given a string, fetch candidate entities (resources) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  @Override
  public List<Entity> findResourceByFulltext(String pattern, int limit)
      throws KBProxyException {
    return this.core.findResourceByFulltext(pattern, limit);
  }

  @Override
  public String getName() {
    return this.core.getName();
  }

  /**
   * Fetches domain of the gives resource.
   *
   * @param uri
   * @return
   * @throws KBProxyException
   */
  @Override
  public List<String> getPropertyDomains(String uri) throws KBProxyException {
    return this.core.getPropertyDomains(uri);
  }

  /**
   * Fetches range of the gives resource.
   *
   * @param uri
   * @return
   * @throws KBProxyException
   */
  @Override
  public List<String> getPropertyRanges(String uri) throws KBProxyException {
    return this.core.getPropertyRanges(uri);
  }

  /**
   * Inserts a new class into the knowledge base
   */
  @Override
  public Entity insertClass(URI uri, String label, Collection<String> alternativeLabels,
      String superClass) throws KBProxyException {
    return this.core.insertClass(uri, label, alternativeLabels, superClass);
  }

  /**
   * Inserts a new concept into the knowledge base
   */
  @Override
  public Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels,
      Collection<String> classes) throws KBProxyException {
    return this.core.insertConcept(uri, label, alternativeLabels, classes);
  }

  /**
   * Inserts a new property into the knowledge base
   */
  @Override
  public Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels,
      String superProperty, String domain, String range) throws KBProxyException {
    return this.core.insertProperty(uri, label, alternativeLabels, superProperty, domain, range);
  }

  /**
   * Information about whether the knowledge base supports inserting new concepts
   */
  @Override
  public boolean isInsertSupported() {
    return this.core.isInsertSupported();
  }

  /**
   * Loads the entity from the knowledge base.
   *
   * @param uri The entity uri.
   * @return The entity or null if no such URI was found in the knowledge base.
   */
  @Override
  public KBProxyResult<Entity> loadEntity(final String uri) {
    return Do(() -> this.core.loadEntity(uri), null);
  }
  
  @Override
  public KBProxyResult<Entity> loadEntity(final String uri, final KnowledgeBaseProxyCore dependenciesProxy) {
    return Do(() -> this.core.loadEntity(uri, dependenciesProxy), null);
  }

  @Override
  public String getResourceLabel(String uri) throws KBProxyException {
    return this.core.getResourceLabel(uri);
  }

  @Override
  public List<Attribute> findAttributes(String resourceId) throws KBProxyException {
    return this.core.findAttributes(resourceId);
  }
}
