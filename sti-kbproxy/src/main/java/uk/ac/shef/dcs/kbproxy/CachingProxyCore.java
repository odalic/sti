package uk.ac.shef.dcs.kbproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.kbproxy.model.PropertyType;
import uk.ac.shef.dcs.kbproxy.solr.CacheQueries;
import uk.ac.shef.dcs.util.Cache;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CachingProxyCore implements ProxyCore {

  private static interface ProxyOperation<T> {
    T get() throws ProxyException;
  }

  private static final Logger log = LoggerFactory.getLogger(CachingProxyCore.class);

  //to point to the ProxyCore (e.g. certain SPARQL based or PP based proxy)
  private final ProxyCore core;

  private final Cache cache;

  private final String structureDomain;
  private final String structureRange;

  public CachingProxyCore(final ProxyCore proxy, final Cache cache,
      final String structureDomain, final String structureRange) {
    Preconditions.checkNotNull(proxy, "The proxy cannot be null!");
    Preconditions.checkNotNull(cache, "The cache cannot be null!");
    Preconditions.checkNotNull(structureDomain, "The structureDomain cannot be null!");
    Preconditions.checkNotNull(structureRange, "The structureRange cannot be null!");

    this.core = proxy;
    this.cache = cache;
    this.structureDomain = structureDomain;
    this.structureRange = structureRange;
  }

  @SuppressWarnings("unchecked")
  private <T> T retrieveCachedValue(final String queryCache) {
    try {
      log.debug("QUERY (" + this.cache + ", cache load)=" + queryCache);

      return (T) cache.retrieve(queryCache);
    } catch (final Exception ex) {
      log.error("Error fetching resource from the cache.", ex);

      return null;
    }
  }

  private <T> T retrieveOrTryExecute(final String queryCache, final ProxyOperation<T> operation)
      throws ProxyException {
    T result = retrieveCachedValue(queryCache);

    if (isNullOrEmpty(result)) {
      try {
        result = operation.get();

        if (!isNullOrEmpty(result)) {
          cacheValue(queryCache, result);
        }
      } catch (final Exception ex) {
        throw new ProxyException("Unexpected error during KB access.", ex);
      }
    }

    return result;
  }

  private void cacheValue(final String queryCache, final Object value) {
    try {
      log.debug("QUERY (" + cache + ", cache save)=" + queryCache);
      cache.cache(queryCache, value, true);
    } catch (final Exception ex) {
      log.error("Error saving resource to the cache.", ex);
    }
  }

  private static boolean isNullOrEmpty(final Object obj) {
    if (obj == null) {
      return true;
    }

    if (obj instanceof String) {
      final String objString = (String) obj;
      return objString.isEmpty();
    }

    if (obj instanceof List<?>) {
      final List<?> objList = (List<?>) obj;
      return objList.isEmpty();
    }

    return false;
  }

  @Override
  public List<String> getPropertyDomains(String uri) throws ProxyException {
    final String cacheQuery = CacheQueries.getPropertyValues(uri, this.structureDomain);

    return retrieveOrTryExecute(cacheQuery, () -> {
      return this.core.getPropertyDomains(uri);
    });
  }

  @Override
  public List<String> getPropertyRanges(String uri) throws ProxyException {
    final String cacheQuery = CacheQueries.getPropertyValues(uri, this.structureRange);

    return retrieveOrTryExecute(cacheQuery, () -> {
      return this.core.getPropertyRanges(uri);
    });
  }

  @Override
  public Entity loadEntity(String uri) throws ProxyException {
    final String cacheQuery = CacheQueries.loadResource(uri);

    return retrieveOrTryExecute(cacheQuery, () -> {
      return this.core.loadEntity(uri, this);
    });
  }

  @Override
  public List<Entity> findEntityCandidates(final String content) throws ProxyException {
    final String cacheQuery = CacheQueries.findResources(content);

    return retrieveOrTryExecute(cacheQuery, () -> {
      return this.core.findEntityCandidates(content, this);
    });
  }

  @Override
  public List<Entity> findEntityCandidatesOfTypes(final String content, final String... types)
      throws ProxyException {
    final String cacheQuery = CacheQueries.findResources(content, types);

    return retrieveOrTryExecute(cacheQuery, () -> {
      return this.core.findEntityCandidatesOfTypes(content, this, types);
    });
  }

  public String getResourceLabel(String uri) throws ProxyException {
    if (!uri.startsWith("http")) {
      return uri;
    }

    final String queryCache = CacheQueries.findLabelForResource(uri);

    return retrieveOrTryExecute(queryCache, () -> {
      return this.core.getResourceLabel(uri);
    });
  }

  public List<Attribute> findAttributes(String id) throws ProxyException {
    if (id.length() == 0)
      return new ArrayList<>();

    final String queryCache = CacheQueries.findAttributesOfResource(id);

    return retrieveOrTryExecute(queryCache, () -> {
      return this.core.findAttributes(id);
    });
  }

  @Override
  public void closeConnection() throws ProxyException {
    try {
      this.cache.shutdown();
    } catch (final Exception e) {
      throw new ProxyException(e);
    }

    this.core.closeConnection();
  }

  @Override
  public void commitChanges() throws ProxyException {
    try {
      this.cache.commit();
    } catch (final Exception e) {
      throw new ProxyException(e);
    }

    this.core.commitChanges();
  }

  @Override
  public List<Attribute> findAttributesOfClazz(String clazzId) throws ProxyException {
    return this.core.findAttributesOfClazz(clazzId, this);
  }

  @Override
  public List<Attribute> findAttributesOfEntities(Entity ec) throws ProxyException {
    return this.core.findAttributesOfEntities(ec, this);
  }

  @Override
  public List<Attribute> findAttributesOfProperty(String propertyId) throws ProxyException {
    return this.core.findAttributesOfProperty(propertyId, this);
  }

  @Override
  public List<Entity> findClassByFulltext(String pattern, int limit) throws ProxyException {
    return this.core.findClassByFulltext(pattern, limit);
  }

  @Override
  public List<Entity> findEntityCandidates(final String content,
      final ProxyCore dependenciesProxy) throws ProxyException {
    return this.core.findEntityCandidates(content, dependenciesProxy);
  }

  @Override
  public List<Entity> findEntityCandidatesOfTypes(String content,
      ProxyCore dependenciesProxy, String... types) throws ProxyException {
    return this.core.findEntityCandidatesOfTypes(content, dependenciesProxy, types);
  }

  @Override
  public Double findEntityClazzSimilarity(String entity_id, String clazz_url) {
    return this.core.findEntityClazzSimilarity(entity_id, clazz_url);
  }

  @Override
  public Double findGranularityOfClazz(String clazz) {
    return this.core.findGranularityOfClazz(clazz);
  }

  @Override
  public List<Entity> findPredicateByFulltext(String pattern, int limit, URI domain, URI range)
      throws ProxyException {
    return this.core.findPredicateByFulltext(pattern, limit, domain, range);
  }

  @Override
  public List<Entity> findResourceByFulltext(String pattern, int limit) throws ProxyException {
    return this.core.findResourceByFulltext(pattern, limit);
  }

  @Override
  public String getName() {
    return this.core.getName();
  }

  @Override
  public Entity insertClass(URI uri, String label, Collection<String> alternativeLabels,
      String superClass) throws ProxyException {
    return this.core.insertClass(uri, label, alternativeLabels, superClass);
  }

  @Override
  public Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels,
      Collection<String> classes) throws ProxyException {
    return this.core.insertConcept(uri, label, alternativeLabels, classes);
  }

  @Override
  public Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels,
                               String superProperty, String domain, String range, PropertyType type) throws ProxyException {
    return this.core.insertProperty(uri, label, alternativeLabels, superProperty, domain, range, type);
  }

  @Override
  public boolean isInsertSupported() {
    return this.core.isInsertSupported();
  }

  @Override
  public Entity loadEntity(String uri, ProxyCore dependenciesProxy)
      throws ProxyException {
    return this.core.loadEntity(uri, dependenciesProxy);
  }

  @Override
  public List<Attribute> findAttributesOfClazz(String clazzId,
      ProxyCore dependenciesProxy) throws ProxyException {
    return this.core.findAttributesOfClazz(clazzId, dependenciesProxy);
  }

  @Override
  public List<Attribute> findAttributesOfEntities(Entity ec,
      ProxyCore dependenciesProxy) throws ProxyException {
    return this.core.findAttributesOfEntities(ec, dependenciesProxy);
  }

  @Override
  public List<Attribute> findAttributesOfProperty(String propertyId,
      ProxyCore dependenciesProxy) throws ProxyException {
    return this.core.findAttributesOfProperty(propertyId, dependenciesProxy);
  }

  @Override
  public ProxyDefinition getDefinition() {
    return this.core.getDefinition();
  }
}
