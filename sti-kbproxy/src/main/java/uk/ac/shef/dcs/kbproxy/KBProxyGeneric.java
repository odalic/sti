package uk.ac.shef.dcs.kbproxy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

public abstract class KBProxyGeneric<KBDefinitionType extends KBDefinition> extends KBProxy {
  protected KBDefinitionType kbDefinition;

  /**
   * @param properties the knowledge base definition
   * @param workingDirectory Base working directory of the application.
   * @param cachesBasePath Base path for the initialized solr caches.
   * @param prefixToUriMap Map of user defined prefixes.
   */
  public KBProxyGeneric(final Properties properties, String workingDirectory, String cachesBasePath, final Map<String, String> prefixToUriMap) throws IOException, URISyntaxException, KBProxyException {
    super(cachesBasePath, prefixToUriMap);

    kbDefinition = createKBDefinition();
    kbDefinition.load(properties, workingDirectory, cachesBasePath);
  }

  @SuppressWarnings("unchecked")
  protected KBDefinitionType createKBDefinition() {
    return (KBDefinitionType) new KBDefinition();
  }

  public KBDefinition getKbDefinition() {
    return this.kbDefinition;
  }
}
