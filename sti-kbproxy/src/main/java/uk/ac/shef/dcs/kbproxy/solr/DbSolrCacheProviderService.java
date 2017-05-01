package uk.ac.shef.dcs.kbproxy.solr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;
import cz.cuni.mff.xrg.odalic.util.storage.DbService;
import uk.ac.shef.dcs.util.Cache;
import uk.ac.shef.dcs.util.SolrCache;

/**
 * A persisent {@link CacheProviderService} implementation employing the Solr cache.
 * 
 * @author Václav Brodec
 * @author Jan Váňa
 * @author Ziqi Zhang
 *
 */
public final class DbSolrCacheProviderService implements CacheProviderService {

  private static final String CACHES_DIRECTORY_PROPERTY_KEY = "sti.cache.main.dir";
  private static final String CACHE_VERSION_ID = "9274dff6-c606-4f5d-8bb5-d528c764e655";
  private static final String CACHE_VERSION = "1.0.18";
  
  private static final String TEMPLATE_SUBDIRECTORY_NAME = "empty";
  
  private static final Logger log = LoggerFactory.getLogger(DbSolrCacheProviderService.class);
  
  private final Path basePath;
  
  private final Path templatePath;
  
  private final DB db;
  
  private final Map<String, Path> idsToPaths;
  
  private final Map<String, CoreContainer> idsToContainers;

  @SuppressWarnings("unchecked")
  public DbSolrCacheProviderService(final PropertiesService propertiesService, final DbService dbService) {
    Preconditions.checkNotNull(propertiesService);
    Preconditions.checkNotNull(dbService); 
    
    this.basePath = readBasePath(propertiesService);
    this.templatePath = readTemplatePath(propertiesService);
    
    this.db = dbService.getDb();
    
    this.idsToPaths = this.db.hashMap("idsToPaths", Serializer.STRING, Serializer.JAVA).createOrOpen();
    this.idsToContainers = new HashMap<>();
  }
  
  private static Path readTemplatePath(final PropertiesService propertiesService) {
    final Path templatePath = Paths.get(propertiesService.get().getProperty(CACHES_DIRECTORY_PROPERTY_KEY)).resolve(TEMPLATE_SUBDIRECTORY_NAME);
    Preconditions.checkArgument(templatePath != null, "The template path key not found!");
    
    Preconditions.checkArgument(Files.exists(templatePath), String.format("The cache template on path %s does not exist!", templatePath));
    
    return templatePath;
  }

  private static Path readBasePath(final PropertiesService propertiesService) {
    final Path basePath = Paths.get(propertiesService.get().getProperty(CACHES_DIRECTORY_PROPERTY_KEY));
    Preconditions.checkArgument(basePath != null, "The base path key not found!");
    
    Preconditions.checkArgument(Files.exists(basePath), String.format("The base path %s does not exist!", basePath));
    
    return basePath;
  }
  
  @Override
  public Cache getCache(final String id) {
    final CoreContainer container = this.idsToContainers.get(id);
    
    if (container == null) {
      return registerCache(id);
    } else {
      return wrap(id, container);
    }
  }

  private SolrCache wrap(final String id, final CoreContainer container) {
    return new SolrCache(new EmbeddedSolrServer(container, id));
  }

  private Cache registerCache(final String id) {
    final Path instancePath = getInstancePath(id);
    
    final EmbeddedSolrServer server = initializeServer(instancePath, id);
    this.idsToContainers.put(id, server.getCoreContainer());
    
    return new SolrCache(server);
  }

  private Path getInstancePath(final String id) {
    final Path cachePath = this.idsToPaths.get(id);
    
    if (cachePath == null) {
      return generateNewPath(id);
    } else {
      return cachePath;      
    }
  }

  private Path generateNewPath(final String id) {
    final Path newCachePath = basePath.resolve(generateRelativePath());
    this.idsToPaths.put(id, newCachePath);
  
    this.db.commit();
  
    return newCachePath;
  }

  private static String generateRelativePath() {
    return UUID.randomUUID().toString();
  }

  private EmbeddedSolrServer initializeServer(final Path path, final String identifier) {
    if (!Files.exists(path)) {
      if (!Files.exists(this.templatePath)) {
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
