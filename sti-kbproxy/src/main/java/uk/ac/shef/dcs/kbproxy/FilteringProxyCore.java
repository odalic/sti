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
import uk.ac.shef.dcs.kbproxy.model.PropertyType;

public final class FilteringProxyCore implements ProxyCore {

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(FilteringProxyCore.class);
  
  private final ProxyCore core;
  
  private final ProxyResultFilter resultFilter;

  /**
   * Wraps CachingProxyCore to filter certain types/predicates
   *
   * TODO: Inefficient and probably also not correct, see: https://grips.semantic-web.at/display/ADEQ/Performance+bottlenecks
   * @param core
   * @param filter
   */
  public FilteringProxyCore(final ProxyCore core, final ProxyResultFilter filter) {
    Preconditions.checkNotNull(core, "The core cannot be null!");
    Preconditions.checkNotNull(filter, "The filter cannot be null!");
    
    this.core = core;
    this.resultFilter = filter;
  }

  @Override
  public void closeConnection() throws ProxyException {
    this.core.closeConnection();
  }

  @Override
  public void commitChanges() throws ProxyException {
    this.core.commitChanges();
  }

//  /**
//   * get attributes of the class
//   * @throws ProxyException
//   */
//  @Override
//  public List<Attribute> findAttributesOfClazz(final String clazzId) throws ProxyException {
//    return this.core.findAttributesOfClazz(clazzId);
//  }

  /**
   * Get attributes of the entity candidate (all predicates and object values of the triples where
   * the candidate entity is the subject).
   *
   * Note: Certain predicates may be blacklisted.
   * @throws ProxyException 
   */
  @Override
  public List<Attribute> findAttributesOfEntities(final Entity ec) throws ProxyException {
    return this.core.findAttributesOfEntities(ec);
  }
//
//  /**
//   * get attributes of the property
//   * @throws ProxyException
//   */
//  @Override
//  public List<Attribute> findAttributesOfProperty(final String propertyId) throws ProxyException {
//    return this.core.findAttributesOfProperty(propertyId);
//  }

  /**
   * Given a string, fetch candidate entities (classes) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  @Override
  public List<Entity> findClassByFulltext(String pattern, int limit) throws ProxyException {
    return this.core.findClassByFulltext(pattern, limit);
  }

  /**
   * Given a string, fetch candidate entities (resources) from the KB Candidate entities are those
   * resources for which label or part of the label matches the given content
   *
   * @param content
   * @return
   * @throws ProxyException 
   */
  @Override
  public List<Entity> findEntityCandidates(final String content) throws ProxyException {
    return this.core.findEntityCandidates(content);
  }
  
  @Override
  public List<Entity> findEntityCandidates(final String content, final ProxyCore dependenciesProxy) throws ProxyException {
    final List<Entity> result = this.core.findEntityCandidates(content, dependenciesProxy);

    for (Entity ec : result) {
      filterEntityTypes(ec);
    }
    
    return result;
  }

  /**
   * Given a string, fetch candidate entities (resources) from the KB that only match certain types
   * @throws ProxyException 
   */
  @Override
  public List<Entity> findEntityCandidatesOfTypes(final String content,
      final String... types) throws ProxyException {
    return findEntityCandidatesOfTypes(content, this, types);
  }
  
  @Override
  public List<Entity> findEntityCandidatesOfTypes(final String content, final ProxyCore dependenciesProxy,
      final String... types) throws ProxyException {
    final List<Entity> result = this.core.findEntityCandidatesOfTypes(content, dependenciesProxy, types);
    
    for (Entity ec : result) {
      filterEntityTypes(ec);
    }
    
    return result;
  }

  /**
   * @param clazz
   * @return parent clazz of given clazz
   */
  @Override
  public String findParentClazz(String clazz) {
    return this.core.findParentClazz(clazz);
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
      URI range) throws ProxyException {
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
      throws ProxyException {
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
   * @throws ProxyException
   */
  @Override
  public List<String> getPropertyDomains(String uri) throws ProxyException {
    return this.core.getPropertyDomains(uri);
  }


  /**
   * Fetches range of the gives resource.
   *
   * @param uri
   * @return
   * @throws ProxyException
   */
  @Override
  public List<String> getPropertyRanges(String uri) throws ProxyException {
    return this.core.getPropertyRanges(uri);
  }

  /**
   * Inserts a new class into the knowledge base
   */
  @Override
  public Entity insertClass(URI uri, String label, Collection<String> alternativeLabels,
      String superClass) throws ProxyException {
    return this.core.insertClass(uri, label, alternativeLabels, superClass);
  }

  /**
   * Inserts a new concept into the knowledge base
   */
  @Override
  public Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels,
      Collection<String> classes) throws ProxyException {
    return this.core.insertConcept(uri, label, alternativeLabels, classes);
  }

  /**
   * Inserts a new property into the knowledge base
   */
  @Override
  public Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels,
      String superProperty, String domain, String range, PropertyType type) throws ProxyException {
    return this.core.insertProperty(uri, label, alternativeLabels, superProperty, domain, range, type);
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
   * @throws ProxyException 
   */
  @Override
  public Entity loadEntity(final String uri) throws ProxyException {
    return this.core.loadEntity(uri);
  }
  
  @Override
  public Entity loadEntity(final String uri, final ProxyCore dependenciesProxy) throws ProxyException {
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
  public String getResourceLabel(String uri, StructureOrDataQueries typeOfQuery) throws ProxyException {
    return this.core.getResourceLabel(uri, typeOfQuery);
  }

  @Override
  public List<Attribute> findAttributes(String resourceId) throws ProxyException {
    List<Attribute> result = this.core.findAttributes(resourceId);
    resultFilter.filterAttribute(result);
    
    return result;
  }

//  @Override
//  public List<Attribute> findAttributesOfClazz(String clazzId,
//      ProxyCore dependenciesProxy) throws ProxyException {
//    return this.core.findAttributesOfClazz(clazzId, dependenciesProxy);
//  }

  @Override
  public List<Attribute> findAttributesOfEntities(Entity ec,
      ProxyCore dependenciesProxy) throws ProxyException {
    return this.core.findAttributesOfEntities(ec, dependenciesProxy);
  }

//  @Override
//  public List<Attribute> findAttributesOfProperty(String propertyId,
//      ProxyCore dependenciesProxy) throws ProxyException {
//    return this.core.findAttributesOfProperty(propertyId, dependenciesProxy);
//  }

  @Override
  public ProxyDefinition getDefinition() {
    return this.core.getDefinition();
  }
}
