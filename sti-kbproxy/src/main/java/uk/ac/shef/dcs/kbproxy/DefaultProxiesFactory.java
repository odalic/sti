package uk.ac.shef.dcs.kbproxy;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import uk.ac.shef.dcs.kbproxy.solr.CacheProviderService;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlProxyDefinition;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlProxyCoreFactory;

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
  
  @Autowired
  public DefaultProxiesFactory(final CacheProviderService cacheProviderService) {
    this(cacheProviderService, ImmutableMap.of(SparqlProxyDefinition.class, new SparqlProxyCoreFactory()));
  }
  
  private DefaultProxiesFactory(final CacheProviderService cacheProviderService, final Map<Class<? extends ProxyDefinition>, ProxyCoreFactory> definitionClassesToCores) {
    Preconditions.checkNotNull(cacheProviderService);
    Preconditions.checkNotNull(definitionClassesToCores);
    
    this.cacheProviderService = cacheProviderService;
    this.definitionClassesToCoreFactories = definitionClassesToCores;
  }
  

  @Override
  public Proxy create(final ProxyDefinition definition, final Map<String, String> prefixesToUris) {
      final ProxyCoreFactory factory = this.definitionClassesToCoreFactories.get(definition.getClass());
      Preconditions.checkArgument(factory != null, "Unknwon definition type!");
      
      final ProxyCore core = factory.create(definition, prefixesToUris);
      
      final ProxyCore cachingCore = new CachingProxyCore(core, cacheProviderService.getCache(core.getName(), DEFAULT_CORE_ID), definition.getStructureDomain(), definition.getStructureRange());
      
      final ProxyResultFilter filter = new ProxyResultFilter(definition.getStoppedClasses(), definition.getStoppedAttributes());
      final ProxyCore filteringCore = new FilteringProxyCore(cachingCore, filter);
      
      return new CoreExceptionsWrappingProxy(filteringCore);
  }
  
  @Override
  public void dispose(final String id) throws IOException {
    this.cacheProviderService.removeCache(id);
  }
}
