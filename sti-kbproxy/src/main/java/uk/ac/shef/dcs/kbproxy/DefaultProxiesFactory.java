package uk.ac.shef.dcs.kbproxy;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.shef.dcs.kbproxy.solr.CacheProviderService;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlProxyCoreFactory;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlProxyDefinition;
import uk.ac.shef.dcs.kbproxy.sparql.pp.PPProxyCoreFactory;
import uk.ac.shef.dcs.kbproxy.sparql.pp.PPProxyDefinition;

import java.io.IOException;
import java.util.Map;

import cz.cuni.mff.xrg.odalic.util.logging.PerformanceLogger;

/**
 * @author Jan Váňa
 * @author Václav Brodec
 */
@Component
public final class DefaultProxiesFactory implements ProxiesFactory {
  
  private static final String DEFAULT_CORE_ID = "entity";

  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(DefaultProxiesFactory.class);
  
  private final CacheProviderService cacheProviderService;
  
  private final Map<Class<? extends ProxyDefinition>, ProxyCoreFactory> definitionClassesToCoreFactories;

  private final PerformanceLogger performanceLogger;

  @Autowired
  public DefaultProxiesFactory(final CacheProviderService cacheProviderService, final PerformanceLogger performanceLogger) {
    this(cacheProviderService, performanceLogger, ImmutableMap.of(SparqlProxyDefinition.class, new SparqlProxyCoreFactory(), PPProxyDefinition.class, new PPProxyCoreFactory()));
  }
  
  private DefaultProxiesFactory(final CacheProviderService cacheProviderService, final PerformanceLogger performanceLogger, final Map<Class<? extends ProxyDefinition>, ProxyCoreFactory> definitionClassesToCores) {
    Preconditions.checkNotNull(cacheProviderService, "The cacheProviderService cannot be null!");
    Preconditions.checkNotNull(definitionClassesToCores, "The definitionClassesToCores cannot be null!");
    
    this.cacheProviderService = cacheProviderService;
    this.definitionClassesToCoreFactories = definitionClassesToCores;
    this.performanceLogger = performanceLogger;
  }
  

  @Override
  public Proxy create(final ProxyDefinition definition, final Map<String, String> prefixesToUris) {
      final ProxyCoreFactory factory = this.definitionClassesToCoreFactories.get(definition.getClass());
      Preconditions.checkArgument(factory != null, "Unknwon definition type!");
      
      final ProxyCore core = factory.create(definition, prefixesToUris, performanceLogger);
      
      final ProxyCore cachingCore = new CachingProxyCore(core, cacheProviderService.getCache(core.getName(), DEFAULT_CORE_ID), definition.getStructureDomain(), definition.getStructureRange(), performanceLogger);
      
      final ProxyResultFilter filter = new ProxyResultFilter(definition.getStoppedClasses(), definition.getStoppedAttributes());
      final ProxyCore filteringCore = new FilteringProxyCore(cachingCore, filter);

      final ProxyCore performanceLoggingCore = new PerformanceLoggingProxyCore(filteringCore, performanceLogger);

      return new CoreExceptionsWrappingProxy(performanceLoggingCore);
  }
  
  @Override
  public void dispose(final String id) throws IOException {
    this.cacheProviderService.removeCache(id);
  }
}
