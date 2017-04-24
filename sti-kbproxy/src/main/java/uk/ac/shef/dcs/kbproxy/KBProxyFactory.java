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
 * @author Jan Váňa
 * @author Václav Brodec
 *
 */
public class KBProxyFactory {
  private final Logger log = LoggerFactory.getLogger(getClass());

  public KnowledgeBaseInterface createInstance(final String kbProxyPropertyFile, final String cachesBasePath,
      final String workingDirectory, final Map<String, String> prefixToUriMap)
      throws KBProxyException {
    try {
      final String combinedCachesBasePath = combinePaths(workingDirectory, cachesBasePath);

      final Properties properties = new Properties();
      properties.load(new FileInputStream(combinePaths(workingDirectory, kbProxyPropertyFile)));

      final String className = KnowledgeBaseDefinition.getKBClass(properties);
      
      final KnowledgeBaseInterface proxy = (KnowledgeBaseInterface) Class.forName(className)
          .getDeclaredConstructor(Properties.class, String.class, String.class, Map.class)
          .newInstance(properties, workingDirectory, combinedCachesBasePath, prefixToUriMap);

      return proxy;
    } catch (final Exception e) {
      this.log.error("Error loading knowledge base \"" + kbProxyPropertyFile + "\".", e);
      
      throw new KBProxyException(e);
    }
  }
}
