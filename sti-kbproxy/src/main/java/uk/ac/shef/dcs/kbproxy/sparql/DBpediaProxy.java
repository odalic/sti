package uk.ac.shef.dcs.kbproxy.sparql;

import uk.ac.shef.dcs.kbproxy.KBProxyException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by - on 10/06/2016.
 */
public class DBpediaProxy extends SPARQLProxy {
  /**
   * @param properties the knowledge base definition
   * @param workingDirectory Base working directory of the application.
   * @param cachesBasePath Base path for the initialized solr caches.
   * @param prefixToUriMap Map of user defined prefixes.
   */
  public DBpediaProxy(
          final Properties properties,
          String workingDirectory,
          String cachesBasePath,
          final Map<String, String> prefixToUriMap)
          throws IOException, URISyntaxException, KBProxyException {
    super(properties, workingDirectory, cachesBasePath, prefixToUriMap);
    resultFilter = new DBpediaSearchResultFilter(kbDefinition.getStopListFile());
  }

  @Override
  protected String applyCustomUriHeuristics(String resourceURI, String label) {
    // This is an yago resource, which may have numbered ids as suffix
    // e.g., City015467.
    if (resourceURI.contains("yago")) {
      int end = 0;
      for (int i = 0; i < label.length(); i++) {
        if (Character.isDigit(label.charAt(i))) {
          end = i;
          break;
        }
      }

      if (end > 0) {
        label = label.substring(0, end);
      }
    }

    return label;
  }
}
