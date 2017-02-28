package uk.ac.shef.dcs.kbproxy;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;
import uk.ac.shef.dcs.kbproxy.sparql.pp.PPProxy;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import uk.ac.shef.dcs.kbproxy.sparql.DBpediaProxy;

/**
 * Created by - on 17/03/2016.
 */
public class KBProxyFactory {
  private static final String PROPERTIES_SEPARATOR = "\\|";

  public Collection<KBProxy> createInstances(final String kbPropertyFiles, String cachesBasePath,
      final String workingDirectory, final Map<String, String> prefixToUriMap)
      throws KBProxyException {

    try {
      final List<KBProxy> result = new ArrayList<>();
      final String[] kbProxyPropertyFilesArray = kbPropertyFiles.split(PROPERTIES_SEPARATOR);
      cachesBasePath = combinePaths(workingDirectory, cachesBasePath);

      for (final String kbProxyPropertyFile : kbProxyPropertyFilesArray) {
        final Properties properties = new Properties();
        properties.load(new FileInputStream(combinePaths(workingDirectory, kbProxyPropertyFile)));

        final String className = properties.getProperty(KBProxy.KB_SEARCH_CLASS);
        final boolean fuzzyKeywords =
            Boolean.valueOf(properties.getProperty(KBProxy.KB_SEARCH_TRY_FUZZY_KEYWORD, "false"));

        if (className.equals(DBpediaProxy.class.getName())) {
          final KBDefinition definition = new KBDefinition();
          definition.load(properties, workingDirectory);

          result
              .add((KBProxy) Class.forName(className)
                  .getDeclaredConstructor(KBDefinition.class, Boolean.class, String.class,
                      Map.class)
                  .newInstance(definition, fuzzyKeywords, cachesBasePath, prefixToUriMap));
        } else if (className.equals(PPProxy.class.getName())) {
          KBDefinition definition = new KBDefinition();
          definition.load(properties, workingDirectory);

          result.add((KBProxy) Class.forName(className).
                  getDeclaredConstructor(KBDefinition.class,
                          Boolean.class,
                          String.class,
                          Map.class).
                  newInstance(definition,
                          fuzzyKeywords, cachesBasePath, prefixToUriMap));

        } else {
          throw new KBProxyException("Class:" + className + " not supported");
        }
      }

      return result;
    } catch (final Exception e) {
      throw new KBProxyException(e);
    }
  }
}
