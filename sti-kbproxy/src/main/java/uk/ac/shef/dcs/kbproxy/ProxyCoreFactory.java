package uk.ac.shef.dcs.kbproxy;

import java.util.Map;

public interface ProxyCoreFactory {
  /**
   * Produces {@link ProxyCore}s.
   * 
   * @param definition core definition
   * @param prefixesToUris resource prefixes to URIs map
   * @return the core
   * @throws ClassCastException when the definition object is incompatible with the factory-produces core type
   */
  ProxyCore create(final ProxyDefinition definition, final Map<String, String> prefixesToUris) throws ClassCastException;
}
