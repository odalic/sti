package uk.ac.shef.dcs.kbproxy.solr;

import java.nio.file.Path;

import uk.ac.shef.dcs.util.Cache;

/**
 * Provides the user ready-to-use {@link Cache} instances.
 * 
 * @author Václav Brodec
 *
 */
public interface CacheProviderService {

  /**
   * Provides a cache instance.
   * 
   * @param templatePath template path of the cache files, these are used to construct the initial cache files
   * @param basePath base path to resolve the relative path
   * @param relativePath relative path to the cache files
   * @param identifier cache identifier
   * @return cache instance
   */
  Cache getCache(final Path templatePath, final Path basePath, final Path relativePath, final String identifier);

}
