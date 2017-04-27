package uk.ac.shef.dcs.kbproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.kbproxy.solr.CacheQueries;
import uk.ac.shef.dcs.util.Cache;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CachingKnowledgeBaseProxyCore implements KnowledgeBaseProxyCore {

  private static interface ProxyOperation<T> {
    T get() throws KBProxyException;
  }

  private static final Logger log = LoggerFactory.getLogger(CachingKnowledgeBaseProxyCore.class);

  private final KnowledgeBaseProxyCore proxy;

  private final Cache cache;

  private final String structureDomain;
  private final String structureRange;

  public CachingKnowledgeBaseProxyCore(final KnowledgeBaseProxyCore proxy, final Cache cache,
      final String structureDomain, final String structureRange) {
    Preconditions.checkNotNull(proxy);
    Preconditions.checkNotNull(cache);
    Preconditions.checkNotNull(structureDomain);
    Preconditions.checkNotNull(structureRange);

    this.proxy = proxy;
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
      throws KBProxyException {
    T result = retrieveCachedValue(queryCache);

    if (isNullOrEmpty(result)) {
      try {
        result = operation.get();

        if (!isNullOrEmpty(result)) {
          cacheValue(queryCache, result);
        }
      } catch (final Exception ex) {
        throw new KBProxyException("Unexpected error during KB access.", ex);
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
  public List<String> getPropertyDomains(String uri) throws KBProxyException {
    final String cacheQuery = CacheQueries.getPropertyValues(uri, this.structureDomain);

    return retrieveOrTryExecute(cacheQuery, () -> {
      return this.proxy.getPropertyDomains(uri);
    });
  }

  @Override
  public List<String> getPropertyRanges(String uri) throws KBProxyException {
    final String cacheQuery = CacheQueries.getPropertyValues(uri, this.structureRange);

    return retrieveOrTryExecute(cacheQuery, () -> {
      return this.proxy.getPropertyRanges(uri);
    });
  }

  @Override
  public Entity loadEntity(String uri) throws KBProxyException {
    final String cacheQuery = CacheQueries.loadResource(uri);

    return retrieveOrTryExecute(cacheQuery, () -> {
      return this.proxy.loadEntity(uri, this);
    });
  }

  @Override
  public List<Entity> findEntityCandidates(final String content) throws KBProxyException {
    final String cacheQuery = CacheQueries.findResources(content);

    return retrieveOrTryExecute(cacheQuery, () -> {
      return this.proxy.findEntityCandidates(content, this);
    });
  }

  @Override
  public List<Entity> findEntityCandidatesOfTypes(final String content, final String... types)
      throws KBProxyException {
    final String cacheQuery = CacheQueries.findResources(content, types);

    return retrieveOrTryExecute(cacheQuery, () -> {
      return this.proxy.findEntityCandidatesOfTypes(content, this, types);
    });
  }

  public String getResourceLabel(String uri) throws KBProxyException {
    if (!uri.startsWith("http")) {
      return uri;
    }

    final String queryCache = CacheQueries.findLabelForResource(uri);

    return retrieveOrTryExecute(queryCache, () -> {
      return this.proxy.getResourceLabel(uri);
    });
  }

  public List<Attribute> findAttributes(String id) throws KBProxyException {
    if (id.length() == 0)
      return new ArrayList<>();

    final String queryCache = CacheQueries.findAttributesOfResource(id);

    return retrieveOrTryExecute(queryCache, () -> {
      return this.proxy.findAttributes(id);
    });
  }

  @Override
  public void closeConnection() throws KBProxyException {
    try {
      this.cache.shutdown();
    } catch (final Exception e) {
      throw new KBProxyException(e);
    }

    this.proxy.closeConnection();
  }

  @Override
  public void commitChanges() throws KBProxyException {
    try {
      this.cache.commit();
    } catch (final Exception e) {
      throw new KBProxyException(e);
    }

    this.proxy.commitChanges();
  }

  @Override
  public List<Attribute> findAttributesOfClazz(String clazzId) throws KBProxyException {
    return this.proxy.findAttributesOfClazz(clazzId, this);
  }

  @Override
  public List<Attribute> findAttributesOfEntities(Entity ec) throws KBProxyException {
    return this.proxy.findAttributesOfEntities(ec, this);
  }

  @Override
  public List<Attribute> findAttributesOfProperty(String propertyId) throws KBProxyException {
    return this.proxy.findAttributesOfProperty(propertyId, this);
  }

  @Override
  public List<Entity> findClassByFulltext(String pattern, int limit) throws KBProxyException {
    return this.proxy.findClassByFulltext(pattern, limit);
  }

  @Override
  public List<Entity> findEntityCandidates(final String content,
      final KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException {
    return dependenciesProxy.findEntityCandidates(content);
  }

  @Override
  public List<Entity> findEntityCandidatesOfTypes(String content,
      KnowledgeBaseProxyCore dependenciesProxy, String... types) throws KBProxyException {
    return dependenciesProxy.findEntityCandidatesOfTypes(content, types);
  }

  @Override
  public Double findEntityClazzSimilarity(String entity_id, String clazz_url) {
    return this.proxy.findEntityClazzSimilarity(entity_id, clazz_url);
  }

  @Override
  public Double findGranularityOfClazz(String clazz) {
    return this.proxy.findGranularityOfClazz(clazz);
  }

  @Override
  public List<Entity> findPredicateByFulltext(String pattern, int limit, URI domain, URI range)
      throws KBProxyException {
    return this.proxy.findPredicateByFulltext(pattern, limit, domain, range);
  }

  @Override
  public List<Entity> findResourceByFulltext(String pattern, int limit) throws KBProxyException {
    return this.proxy.findResourceByFulltext(pattern, limit);
  }

  @Override
  public String getName() {
    return this.proxy.getName();
  }

  @Override
  public Entity insertClass(URI uri, String label, Collection<String> alternativeLabels,
      String superClass) throws KBProxyException {
    return this.proxy.insertClass(uri, label, alternativeLabels, superClass);
  }

  @Override
  public Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels,
      Collection<String> classes) throws KBProxyException {
    return this.proxy.insertConcept(uri, label, alternativeLabels, classes);
  }

  @Override
  public Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels,
      String superProperty, String domain, String range) throws KBProxyException {
    return this.proxy.insertProperty(uri, label, alternativeLabels, superProperty, domain, range);
  }

  @Override
  public boolean isInsertSupported() {
    return this.proxy.isInsertSupported();
  }

  @Override
  public Entity loadEntity(String uri, KnowledgeBaseProxyCore dependenciesProxy)
      throws KBProxyException {
    return dependenciesProxy.loadEntity(uri);
  }

  @Override
  public List<Attribute> findAttributesOfClazz(String clazzId,
      KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException {
    return dependenciesProxy.findAttributesOfClazz(clazzId);
  }

  @Override
  public List<Attribute> findAttributesOfEntities(Entity ec,
      KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException {
    return dependenciesProxy.findAttributesOfEntities(ec);
  }

  @Override
  public List<Attribute> findAttributesOfProperty(String propertyId,
      KnowledgeBaseProxyCore dependenciesProxy) throws KBProxyException {
    return dependenciesProxy.findAttributesOfProperty(propertyId);
  }
}
