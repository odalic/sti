package uk.ac.shef.dcs.kbproxy;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.solr.CacheProviderService;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlBaseProxyDefinition;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlKnowlegeBaseProxyCore;

/**
 * @author Jan Váňa
 * @author Václav Brodec
 *
 */
public class KnowledgeBaseProxiesFactory {
  
  private final CacheProviderService cacheProviderService = null;
  
  private final Logger log = LoggerFactory.getLogger(getClass());

  public KnowledgeBaseProxy createInstance(final Object definition, final String kbProxyPropertyFile, final String cachesBasePath,
      final String workingDirectory, final Map<String, String> prefixToUriMap)
      throws KBProxyException {
    try {
      final String combinedCachesBasePath = combinePaths(workingDirectory, cachesBasePath);

      final SparqlBaseProxyDefinition castDefinition = SparqlBaseProxyDefinition.class.cast(definition);
      
      final SparqlKnowlegeBaseProxyCore core = new SparqlKnowlegeBaseProxyCore(castDefinition, prefixToUriMap);
      
      final Path templatePath = null;
      final Path basePath = null;
      final Path relativePath = null;
      final String identifier = null;
      
      final KnowledgeBaseProxyCore cachingCore = new CachingKnowledgeBaseProxyCore(core, cacheProviderService.getCache(templatePath, basePath, relativePath, identifier), castDefinition.getStructureDomain(), castDefinition.getStructureRange());
      
      final KnowledgeBaseProxyCore filteringCore = new FilteringKnowledgeBaseProxyCore(cachingCore, new KBProxyResultFilter(castDefinition.getStoppedClasses(), castDefinition.getStoppedAttributes()));
      
      return new ResilientKnowledgeBaseProxy(filteringCore);
    } catch (final Exception e) {
      this.log.error("Error loading knowledge base \"" + kbProxyPropertyFile + "\".", e);
      
      throw new KBProxyException(e);
    }
  }
}
