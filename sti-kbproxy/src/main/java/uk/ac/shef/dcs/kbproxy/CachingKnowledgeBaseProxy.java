package uk.ac.shef.dcs.kbproxy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import uk.ac.shef.dcs.kbproxy.BasicKnowledgeBaseProxy.Func;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.util.Cache;
import uk.ac.shef.dcs.util.SolrCache;

public class CachingKnowledgeBaseProxy implements KnowledgeBaseInterface {

  private static final String CACHE_VERSION_ID = "9274dff6-c606-4f5d-8bb5-d528c764e655";

  private static final String CACHE_VERSION = "1.0.18";
  private static final Map<String, CoreContainer> cacheCores = new HashMap<>();
  private static final String ENTITY_CACHE = "entity";
  private static final boolean AUTO_COMMIT = true;
  private static final boolean ALWAYS_CALL_REMOTE_SEARCH_API = false;
  
  protected final Logger log = LoggerFactory.getLogger(getClass());
  
  private final KnowledgeBaseInterface proxy;
  
  private final Cache entityCache;
  private final String cachesBasePath;

  /**
   * @param name name
   * @param cachesBasePath Base path for the initialized solr caches.
   * @param prefixToUriMap Map of user defined prefixes.
   */
  public CachingKnowledgeBaseProxy(final KnowledgeBaseInterface proxy, final String cachesBasePath)
      throws IOException, URISyntaxException, KBProxyException {
    Preconditions.checkNotNull(proxy);
    
    this.proxy = proxy;
    this.cachesBasePath = cachesBasePath;
    this.entityCache = new SolrCache(getSolrServer(ENTITY_CACHE));
  }

  private void cacheValue(final String queryCache, final Object value, final Cache cache) {
    try {
      this.log.debug("QUERY (" + cache.getServer().getCoreContainer().getSolrHome()
          + ", cache save)=" + queryCache);
      cache.cache(queryCache, value, AUTO_COMMIT);
    } catch (final Exception ex) {
      this.log.error("Error saving resource to the cache.", ex);
    }
  }

  @Override
  public void closeConnection() throws KBProxyException {
    try {
      if (this.entityCache != null) {
        this.entityCache.shutdown();
      }
    } catch (final Exception e) {
      throw new KBProxyException(e);
    }
    
    this.proxy.closeConnection();
  }

  @Override
  public void commitChanges() throws KBProxyException {
    try {
      if (this.entityCache != null) {
        this.entityCache.commit();
      }
    } catch (final Exception e) {
      throw new KBProxyException(e);
    }
    
    this.proxy.commitChanges();
  }

  public EmbeddedSolrServer getSolrServer(final String cacheIdentifier) throws KBProxyException {
    final Path cachePath = Paths.get(this.cachesBasePath, getKbDefinition().getName());
    EmbeddedSolrServer cacheServer;

    if (!cacheCores.containsKey(cachePath.toString())) {
      cacheServer = initializeSolrServer(cacheIdentifier, cachePath,
          getKbDefinition().getCacheTemplatePath());
      cacheCores.put(cachePath.toString(), cacheServer.getCoreContainer());
    } else {
      cacheServer = new EmbeddedSolrServer(cacheCores.get(cachePath.toString()), cacheIdentifier);
    }

    return cacheServer;
  }

  private EmbeddedSolrServer initializeSolrServer(final String cacheIdentifier,
      final Path cachePath, final String templatePathString) throws KBProxyException {
    if (!Files.exists(cachePath)) {
      final Path templatePath = Paths.get(templatePathString);
      if (!Files.exists(templatePath)) {
        final String error =
            "Cannot proceed: the cache dir is not set or does not exist: " + templatePathString;
        this.log.error(error);
        throw new KBProxyException(error);
      }

      try {
        FileUtils.copyDirectory(templatePath.toFile(), cachePath.toFile());
      } catch (final IOException exception) {
        final String error = "Cannot proceed: the cache template cannot be copied. source: "
            + templatePath + "target: " + cachePath;

        this.log.error(error);
        throw new KBProxyException(error, exception);
      }
    }

    final EmbeddedSolrServer server = new EmbeddedSolrServer(cachePath, cacheIdentifier);
    verifyServerVersion(server);
    return server;
  }

  private boolean isNullOrEmpty(final Object obj) {
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

  @SuppressWarnings("unchecked")
  private <T> T retrieveCachedValue(final String queryCache) {
    T result = null;
    if (!ALWAYS_CALL_REMOTE_SEARCH_API) {
      // if cache is not disabled, try to examine the cache first
      try {
        this.log.debug("QUERY (" + entityCache.getServer().getCoreContainer().getSolrHome()
            + ", cache load)=" + queryCache);

        result = (T) entityCache.retrieve(queryCache);
      } catch (final Exception ex) {
        this.log.error("Error fetching resource from the cache.", ex);
      }
    }

    return result;
  }

  protected <ResultType> ResultType retrieveOrTryExecute(final String queryCache, final Func<ResultType> func) throws KBProxyException {
    ResultType result = retrieveCachedValue(queryCache);

    if (isNullOrEmpty(result)) {
      try {
        result = func.Do();

        if (!isNullOrEmpty(result)) {
          cacheValue(queryCache, result, entityCache);
        }
      } catch (final Exception ex) {
        throw new KBProxyException("Unexpected error during KB access.", ex);
      }
    }

    return result;
  }

  private void verifyServerVersion(final EmbeddedSolrServer server) throws KBProxyException {
    try {
      final Cache cache = new SolrCache(server);
      final String cacheVersion = (String) cache.retrieve(CACHE_VERSION_ID);
      if (!CACHE_VERSION.equals(cacheVersion)) {
        server.deleteByQuery("*:*");
        cache.cache(CACHE_VERSION_ID, CACHE_VERSION, true);
      }
    } catch (SolrServerException | IOException | ClassNotFoundException e) {
      final String error = "Error initializing the cache.";
      this.log.error(error, e);
      throw new KBProxyException(error, e);
    }
  }

  @Override
  public KBProxyResult<List<Attribute>> findAttributesOfClazz(String clazzId) {
    return this.proxy.findAttributesOfClazz(clazzId);
  }

  @Override
  public KBProxyResult<List<Attribute>> findAttributesOfEntities(Entity ec) {
    return this.proxy.findAttributesOfEntities(ec);
  }

  @Override
  public KBProxyResult<List<Attribute>> findAttributesOfProperty(String propertyId) {
    return this.proxy.findAttributesOfProperty(propertyId);
  }

  @Override
  public List<Entity> findClassByFulltext(String pattern, int limit) throws KBProxyException {
    return this.proxy.findClassByFulltext(pattern, limit);
  }

  @Override
  public KBProxyResult<List<Entity>> findEntityCandidates(String content) {
    return this.proxy.findEntityCandidates(content);
  }

  @Override
  public KBProxyResult<List<Entity>> findEntityCandidatesOfTypes(String content, String... types) {
    return this.proxy.findEntityCandidatesOfTypes(content, types);
  }

  @Override
  public KBProxyResult<Double> findEntityClazzSimilarity(String entity_id, String clazz_url) {
    return this.proxy.findEntityClazzSimilarity(entity_id, clazz_url);
  }

  @Override
  public KBProxyResult<Double> findGranularityOfClazz(String clazz) {
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
  public List<String> getPropertyDomains(String uri) throws KBProxyException {
    return this.proxy.getPropertyDomains(uri);
  }

  @Override
  public List<String> getPropertyRanges(String uri) throws KBProxyException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Entity insertClass(URI uri, String label, Collection<String> alternativeLabels,
      String superClass) throws KBProxyException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels,
      Collection<String> classes) throws KBProxyException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels,
      String superProperty, String domain, String range) throws KBProxyException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isInsertSupported() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public KBProxyResult<Entity> loadEntity(String uri) {
    // TODO Auto-generated method stub
    return null;
  }
}
