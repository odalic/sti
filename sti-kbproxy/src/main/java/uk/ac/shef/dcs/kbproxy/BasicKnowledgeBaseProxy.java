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

public abstract class BasicKnowledgeBaseProxy implements KnowledgeBaseInterface {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  
  private final String name;
  
  protected interface Func<Type> {
    Type Do() throws Exception;
  }
  
  /**
   * @param name name of the proxy
   */
  public BasicKnowledgeBaseProxy(final String name) {
    Preconditions.checkNotNull(name);
    
    this.name = name;
  }

  @Override
  public void closeConnection() throws KBProxyException {
  }

  @Override
  public void commitChanges() throws KBProxyException {
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
    return Do(() -> findAttributesOfClazzInternal(clazzId), new ArrayList<Attribute>());
  }

  protected abstract List<Attribute> findAttributesOfClazzInternal(String clazzId)
      throws KBProxyException;

  /**
   * Get attributes of the entity candidate (all predicates and object values of the triples where
   * the candidate entity is the subject).
   *
   * Note: Certain predicates may be blacklisted.
   */
  @Override
  public KBProxyResult<List<Attribute>> findAttributesOfEntities(final Entity ec) {
    return Do(() -> findAttributesOfEntitiesInternal(ec), new ArrayList<Attribute>());
  }

  protected abstract List<Attribute> findAttributesOfEntitiesInternal(Entity ec)
      throws KBProxyException;

  /**
   * get attributes of the property
   */
  @Override
  public KBProxyResult<List<Attribute>> findAttributesOfProperty(final String propertyId) {
    return Do(() -> findAttributesOfPropertyInternal(propertyId), new ArrayList<Attribute>());
  }

  protected abstract List<Attribute> findAttributesOfPropertyInternal(String propertyId)
      throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (classes) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  @Override
  public abstract List<Entity> findClassByFulltext(String pattern, int limit)
      throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB Candidate entities are those
   * resources for which label or part of the label matches the given content
   *
   * @param content
   * @return
   */
  @Override
  public KBProxyResult<List<Entity>> findEntityCandidates(final String content) {
    return Do(() -> findEntityCandidatesInternal(content), new ArrayList<Entity>());
  }

  protected abstract List<Entity> findEntityCandidatesInternal(String content)
      throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB that only match certain types
   */
  @Override
  public KBProxyResult<List<Entity>> findEntityCandidatesOfTypes(final String content,
      final String... types) {
    return Do(() -> findEntityCandidatesOfTypesInternal(content, types), new ArrayList<Entity>());
  }

  protected abstract List<Entity> findEntityCandidatesOfTypesInternal(String content,
      String... types) throws KBProxyException;

  /**
   * compute the seamntic similarity between an entity and a class
   */
  @Override
  public KBProxyResult<Double> findEntityClazzSimilarity(final String entity_id,
      final String clazz_url) {
    return Do(() -> findEntityClazzSimilarityInternal(entity_id, clazz_url), 0.0);
  }

  protected double findEntityClazzSimilarityInternal(final String entity_id, final String clazz_url)
      throws KBProxyException {
    return 0;
  }

  /**
   * @return the granularity of the class in the KB.
   */
  @Override
  public KBProxyResult<Double> findGranularityOfClazz(final String clazz) {
    return Do(() -> findGranularityOfClazzInternal(clazz), 0.0);
  }

  protected double findGranularityOfClazzInternal(final String clazz) throws KBProxyException {
    return 0;
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
  public abstract List<Entity> findPredicateByFulltext(String pattern, int limit, URI domain,
      URI range) throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  @Override
  public abstract List<Entity> findResourceByFulltext(String pattern, int limit)
      throws KBProxyException;

  @Override
  public String getName() {
    return this.name;
  }


  /**
   * Fetches domain of the gives resource.
   *
   * @param uri
   * @return
   * @throws KBProxyException
   */
  @Override
  public abstract List<String> getPropertyDomains(String uri) throws KBProxyException;


  /**
   * Fetches range of the gives resource.
   *
   * @param uri
   * @return
   * @throws KBProxyException
   */
  @Override
  public abstract List<String> getPropertyRanges(String uri) throws KBProxyException;

  /**
   * Inserts a new class into the knowledge base
   */
  @Override
  public abstract Entity insertClass(URI uri, String label, Collection<String> alternativeLabels,
      String superClass) throws KBProxyException;

  /**
   * Inserts a new concept into the knowledge base
   */
  @Override
  public abstract Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels,
      Collection<String> classes) throws KBProxyException;

  /**
   * Inserts a new property into the knowledge base
   */
  @Override
  public abstract Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels,
      String superProperty, String domain, String range) throws KBProxyException;

  /**
   * Information about whether the knowledge base supports inserting new concepts
   */
  @Override
  public abstract boolean isInsertSupported();

  /**
   * Loads the entity from the knowledge base.
   *
   * @param uri The entity uri.
   * @return The entity or null if no such uri was found in the knowledge base.
   */
  @Override
  public KBProxyResult<Entity> loadEntity(final String uri) {
    return Do(() -> loadEntityInternal(uri), null);
  }

  protected abstract Entity loadEntityInternal(String uri) throws KBProxyException;
}
