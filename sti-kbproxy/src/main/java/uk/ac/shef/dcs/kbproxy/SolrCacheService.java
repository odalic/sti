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

public final class SolrCacheService {

  private static final String CACHE_VERSION_ID = "9274dff6-c606-4f5d-8bb5-d528c764e655";
  private static final String CACHE_VERSION = "1.0.18";
  private static final String ENTITY_CACHE = "entity";
  private static final boolean AUTO_COMMIT = true;
  
  private static final Logger log = LoggerFactory.getLogger(SolrCacheService.class);
  
  private final Cache cache;

  public SolrCacheService(final Map<String, CoreContainer> cores) {
    this.cache = (new SolrCache(getSolrServer(template, base, path, identifier))
  }
  
  /**
   * @param name name
   * @param basePath Base path for the initialized solr caches.
   * @param prefixToUriMap Map of user defined prefixes.
   */
  public SolrCacheService(final Path template, final Path base, final Path path, final String identifier) {
    this(new SolrCache(getSolrServer(template, base, path, identifier)));
  }
  
  public SolrCacheService(final Cache solrCache) {
    Preconditions.checkNotNull(solrCache);
    
    this.cache = solrCache;
    this.cores
  }

  private void cacheValue(final String queryCache, final Object value) {
    try {
      log.debug("QUERY (" + cache.getServer().getCoreContainer().getSolrHome()
          + ", cache save)=" + queryCache);
      cache.cache(queryCache, value, AUTO_COMMIT);
    } catch (final Exception ex) {
      log.error("Error saving resource to the cache.", ex);
    }
  }

  public void closeConnection() throws KBProxyException {
    try {
      if (this.cache != null) {
        this.cache.shutdown();
      }
    } catch (final Exception e) {
      throw new KBProxyException(e);
    }
  }

  public void commitChanges() throws KBProxyException {
    try {
      if (this.cache != null) {
        this.cache.commit();
      }
    } catch (final Exception e) {
      throw new KBProxyException(e);
    }
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
        log.debug("QUERY (" + cache.getServer().getCoreContainer().getSolrHome()
            + ", cache load)=" + queryCache);

        result = (T) cache.retrieve(queryCache);
      } catch (final Exception ex) {
        log.error("Error fetching resource from the cache.", ex);
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
          cacheValue(queryCache, result, cache);
        }
      } catch (final Exception ex) {
        throw new KBProxyException("Unexpected error during KB access.", ex);
      }
    }

    return result;
  }
}
