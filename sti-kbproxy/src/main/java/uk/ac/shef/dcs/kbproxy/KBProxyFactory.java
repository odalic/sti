package uk.ac.shef.dcs.kbproxy;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by - on 17/03/2016.
 */
public class KBProxyFactory {
  private static final String PROPERTIES_SEPARATOR = "\\|";
  private final Logger log = LoggerFactory.getLogger(getClass());

  public Collection<KBProxy> createInstances(final String kbPropertyFiles, String cachesBasePath,
      final String workingDirectory, final Map<String, String> prefixToUriMap)
      throws KBProxyException {

    try {
      final List<KBProxy> result = new ArrayList<>();
      final String[] kbProxyPropertyFilesArray = kbPropertyFiles.split(PROPERTIES_SEPARATOR);
      cachesBasePath = combinePaths(workingDirectory, cachesBasePath);

      for (final String kbProxyPropertyFile : kbProxyPropertyFilesArray) {
        try {
          final Properties properties = new Properties();
          properties.load(new FileInputStream(combinePaths(workingDirectory, kbProxyPropertyFile)));

          final String className = KBDefinition.getKBClass(properties);
          final KBProxy proxy = (KBProxy) Class.forName(className)
              .getDeclaredConstructor(Properties.class, String.class, String.class, Map.class)
              .newInstance(properties, workingDirectory, cachesBasePath, prefixToUriMap);

          result.add(proxy);
        } catch (final Exception e) {
          this.log.error("Error loading knowledge base \"" + kbProxyPropertyFile + "\".", e);
        }
      }

      return result;
    } catch (final Exception e) {
      throw new KBProxyException(e);
    }
  }
}
