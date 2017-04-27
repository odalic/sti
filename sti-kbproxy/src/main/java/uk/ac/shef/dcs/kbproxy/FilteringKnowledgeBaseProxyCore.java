package uk.ac.shef.dcs.kbproxy;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;

public final class FilteringKnowledgeBaseProxyCore implements KnowledgeBaseProxyCore {

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(FilteringKnowledgeBaseProxyCore.class);
  
  private final KnowledgeBaseProxyCore core;
  
  private final KBProxyResultFilter resultFilter;
  
  public FilteringKnowledgeBaseProxyCore(final KnowledgeBaseProxyCore core, final KBProxyResultFilter filter) {
    Preconditions.checkNotNull(core);
    Preconditions.checkNotNull(filter);
    
    this.core = core;
    this.resultFilter = filter;
  }

  @Override
  public void closeConnection() throws KBProxyException {
    this.core.closeConnection();
  }

  @Override
  public void commitChanges() throws KBProxyException {
    this.core.commitChanges();
  }

  /**
   * get attributes of the class
   * @throws KBProxyException 
   */
  @Override
  public List<Attribute> findAttributesOfClazz(final String clazzId) throws KBProxyException {
    return this.core.findAttributesOfClazz(clazzId);
  }

  /**
   * Get attributes of the entity candidate (all predicates and object values of the triples where
   * the candidate entity is the subject).
   *
   * Note: Certain predicates may be blacklisted.
   * @throws KBProxyException 
   */
  @Override
  public List<Attribute> findAttributesOfEntities(final Entity ec) throws KBProxyException {
    return this.core.findAttributesOfEntities(ec);
  }

  /**
   * get attributes of the property
   * @throws KBProxyException 
   */
  @Override
  public List<Attribute> findAttributesOfProperty(final String propertyId) throws KBProxyException {
    return this.core.findAttributesOfProperty(propertyId);
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
  public List<Entity> findClassByFulltext(String pattern, int limit) throws KBProxyException {
    return this.core.findClassByFulltext(pattern, limit);
  }

  /**
   * Given a string, fetch candidate entities (resources) from the KB Candidate entities are those
   * resources for which label or part of the label matches the given content
   *
   * @param content
   * @return
   * @throws KBProxyException 
   */
  @Override
  public List<Entity> findEntityCandidates(final String content) throws KBProxyException {
    return this.core.findEntityCandidates(content);
  }
  
  @Override
  public List<Entity> findEntityCandidates(final String content, final KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException {
    final List<Entity> result = this.core.findEntityCandidates(content, dependenciesProxy);

    for (Entity ec : result) {
      filterEntityTypes(ec);
    }
    
    return result;
  }

  /**
   * Given a string, fetch candidate entities (resources) from the KB that only match certain types
   * @throws KBProxyException 
   */
  @Override
  public List<Entity> findEntityCandidatesOfTypes(final String content,
      final String... types) throws KBProxyException {
    return findEntityCandidatesOfTypes(content, this, types);
  }
  
  @Override
  public List<Entity> findEntityCandidatesOfTypes(final String content, final KnowledgeBaseProxyCore dependenciesProxy,
      final String... types) throws KBProxyException {
    final List<Entity> result = this.core.findEntityCandidatesOfTypes(content, dependenciesProxy, types);
    
    for (Entity ec : result) {
      filterEntityTypes(ec);
    }
    
    return result;
  }

  /**
   * compute the seamntic similarity between an entity and a class
   */
  @Override
  public Double findEntityClazzSimilarity(final String entity_id, final String clazz_url) {
    return this.core.findEntityClazzSimilarity(entity_id, clazz_url);
  }

  /**
   * @return the granularity of the class in the KB.
   */
  @Override
  public Double findGranularityOfClazz(final String clazz) {
    return this.core.findGranularityOfClazz(clazz);
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
   * @throws KBProxyException 
   */
  @Override
  public Entity loadEntity(final String uri) throws KBProxyException {
    return this.core.loadEntity(uri);
  }
  
  @Override
  public Entity loadEntity(final String uri, final KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException {
    final Entity result = this.core.loadEntity(uri, dependenciesProxy);
    
    filterEntityTypes(result);
    
    return result;
  }

  private void filterEntityTypes(Entity entity) {
    List<Clazz> filteredTypes = this.resultFilter.filterClazz(entity.getTypes());

    entity.clearTypes();
    for (Clazz ft : filteredTypes) {
      entity.addType(ft);
    }
  }

  @Override
  public String getResourceLabel(String uri) throws KBProxyException {
    return this.core.getResourceLabel(uri);
  }

  @Override
  public List<Attribute> findAttributes(String resourceId) throws KBProxyException {
    List<Attribute> result = this.core.findAttributes(resourceId);
    resultFilter.filterAttribute(result);
    
    return result;
  }

  @Override
  public List<Attribute> findAttributesOfClazz(String clazzId,
      KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException {
    return this.core.findAttributesOfClazz(clazzId, dependenciesProxy);
  }

  @Override
  public List<Attribute> findAttributesOfEntities(Entity ec,
      KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException {
    return this.core.findAttributesOfEntities(ec, dependenciesProxy);
  }

  @Override
  public List<Attribute> findAttributesOfProperty(String propertyId,
      KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException {
    return this.core.findAttributesOfProperty(propertyId, dependenciesProxy);
  }
}
