package uk.ac.shef.dcs.kbproxy.solr;

import java.io.IOException;

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
   * @param containerId cache container ID
   * @param containerId cache core ID within the container
   * @return cache instance
   */
  Cache getCache(final String containerId, final String coreId);

  void removeCache(String containerId) throws IOException;
}
