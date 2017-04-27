package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;
import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;
import uk.ac.shef.dcs.kbproxy.KnowledgeBaseProxy;
import uk.ac.shef.dcs.kbproxy.KnowledgeBaseProxiesFactory;
import uk.ac.shef.dcs.sti.STIException;

/**
 * Created by Jan
 */
public class DefaultKnowledgeBaseProxiesProvider implements KnowledgeBaseProxiesProvider {

  private static final String PROPERTY_CACHE_FOLDER = "sti.cache.main.dir";

  private static final Logger logger =
      LoggerFactory.getLogger(DefaultKnowledgeBaseProxiesProvider.class);

  private final PrefixMappingService prefixService;
  
  private final Path baseCachePath;
  private final Map<String, KnowledgeBaseProxy> namesToProxies;

  @Autowired
  public DefaultKnowledgeBaseProxiesProvider(final PrefixMappingService prefixService, final PropertiesService propertiesService)
      throws STIException, IOException {
    this(prefixService, initializeCacheBasePath(propertiesService), new HashMap<>());
  }

  private static Path initializeCacheBasePath(final PropertiesService propertiesService) {
    return Paths.get(propertiesService.get().getProperty(PROPERTY_CACHE_FOLDER));
  }

  private DefaultKnowledgeBaseProxiesProvider(final PrefixMappingService prefixService,
      final Path baseCachePath, final Map<String, KnowledgeBaseProxy> namesToProxies) {
    Preconditions.checkNotNull(prefixService);
    Preconditions.checkNotNull(baseCachePath);

    this.prefixService = prefixService;
    this.baseCachePath = baseCachePath;
    this.namesToProxies = namesToProxies;
  }

  @Override
  public Map<String, KnowledgeBaseProxy> getKBProxies() {
    return ImmutableMap.copyOf(this.namesToProxies);
  }

  @Override
  public PrefixMappingService getPrefixService() {
    return this.prefixService;
  }

  private Collection<KnowledgeBaseProxy> initnamesToProxies(final Map<String, String> prefixToUriMap)
      throws STIException {
    logger.info("Initializing KBProxy ...");
    try {
      final KnowledgeBaseProxiesFactory fbf = new KnowledgeBaseProxiesFactory();
      return null;
      //return fbf.createInstances(null, prefixToUriMap);
    } catch (final Exception e) {
      logger.error("Failed proxies initializ", e.getLocalizedMessage(), e.getStackTrace());
      
      throw new STIException("Failed initializing namesToProxies.", e);
    }
  }

  @Override
  public void set(final KnowledgeBase base) {
    final String name = base.getName();
    
    //TODO: Detect usage.
    
    final KnowledgeBaseProxy proxy = null;
    
    this.namesToProxies.put(name, proxy);
  }
}
