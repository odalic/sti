package uk.ac.shef.dcs.util;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;


/**
 * A general cache interface.
 * 
 * @author Václav Brodec
 *
 */
public interface Cache {

  /**
   * Caches a value in the cache under the assigned key.
   * 
   * @param key cached key
   * @param value cached value
   * @param commit true to commit immediately after the insertion
   * @throws IOException in case of I/O exception
   */
  void cache(String key, Object value, boolean commit) throws IOException;
  
  /**
   * Retrieves associated value from the cache.
   * 
   * @param key
   * @return null if no cache entry has been created for this key; an empty List object if there are
   *         no results for the key (e.g. the retrieval is attempted before the entry was created)
 *   @throws IOException in case of I/O exception
   */
  Object retrieve(String key) throws IOException;

  /**
   * Commits the changes in the cache.
   * 
   * @throws IOException in case of I/O exception
   */
  void commit() throws IOException, SolrServerException;

  /**
   * Shuts the case down. No further operations allowed.
   * 
   * @throws IOException in case of I/O exception
   */
  void shutdown() throws IOException;
}
