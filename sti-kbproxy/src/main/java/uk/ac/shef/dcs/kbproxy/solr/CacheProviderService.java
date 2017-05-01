package uk.ac.shef.dcs.kbproxy.solr;

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
   * @param identifier cache identifier
   * @return cache instance
   */
  Cache getCache(final String identifier);
}
