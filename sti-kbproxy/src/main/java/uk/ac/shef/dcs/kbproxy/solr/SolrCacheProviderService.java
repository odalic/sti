package uk.ac.shef.dcs.kbproxy.solr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.util.Cache;
import uk.ac.shef.dcs.util.SolrCache;

/**
 * A {@link CacheProviderService} implementation employing the Solr cache.
 * 
 * @author Václav Brodec
 * @author Ziqi Zhang
 *
 */
public final class SolrCacheProviderService implements CacheProviderService {

  private static final String CACHE_VERSION_ID = "9274dff6-c606-4f5d-8bb5-d528c764e655";
  private static final String CACHE_VERSION = "1.0.18";
  
  private static final Logger log = LoggerFactory.getLogger(SolrCacheProviderService.class);
  
  private final Map<String, CoreContainer> containers;

  public SolrCacheProviderService() {
    this(new HashMap<>());
  }
  
  private SolrCacheProviderService(final Map<String, CoreContainer> cores) {
    this.containers = cores;
  }
  
  @Override
  public Cache getCache(final Path templatePath, final Path basePath, final Path relativePath, final String identifier) {
    final Path cachePath = basePath.resolve(relativePath);
    final String containerKey = getContainerKey(cachePath);
    
    final CoreContainer container = this.containers.get(containerKey);
    
    if (container == null) {
      final EmbeddedSolrServer server = initializeServer(templatePath, cachePath,
          identifier);
      this.containers.put(containerKey, server.getCoreContainer());
      
      return new SolrCache(server);
    }
    
    return new SolrCache(new EmbeddedSolrServer(container, identifier));
  }

  private static String getContainerKey(final Path cachePath) {
    return cachePath.toString();
  }
  
  private EmbeddedSolrServer initializeServer(final Path templatePath,
      final Path path, final String identifier) {
    if (!Files.exists(path)) {
      if (!Files.exists(templatePath)) {
        final String error =
            String.format("Cannot proceed: the cache directory \"%s\" is not set or does not exist!", templatePath);
        log.error(error);
        
        throw new IllegalStateException(error);
      }

      try {
        FileUtils.copyDirectory(templatePath.toFile(), path.toFile());
      } catch (final IOException exception) {
        final String error = "Cannot proceed: the cache template cannot be copied (source: "
            + templatePath + ", target: " + path + ")!";

        log.error(error);
        throw new IllegalStateException(error, exception);
      }
    }

    final EmbeddedSolrServer server = new EmbeddedSolrServer(path, identifier);
    verifyServerVersion(server);
    return server;
  }

  private void verifyServerVersion(final EmbeddedSolrServer server) {
    try {
      final Cache cache = new SolrCache(server);
      final String cacheVersion = (String) cache.retrieve(CACHE_VERSION_ID);
      
      if (!CACHE_VERSION.equals(cacheVersion)) {
        server.deleteByQuery("*:*");
        cache.cache(CACHE_VERSION_ID, CACHE_VERSION, true);
      }
    } catch (final SolrServerException | IOException e) {
      final String error = "Error initializing the cache!";
      log.error(error, e);
      
      throw new IllegalStateException(error, e);
    }
  }
}
