package cz.cuni.mff.xrg.odalic.bases.proxies;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.bases.AdvancedBaseTypesService;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;
import uk.ac.shef.dcs.kbproxy.Proxy;
import uk.ac.shef.dcs.kbproxy.ProxyDefinition;
import uk.ac.shef.dcs.kbproxy.ProxiesFactory;

/**
 * @author Václav Brodec
 */
public final class MemoryOnlyKnowledgeBaseProxiesService implements KnowledgeBaseProxiesService {

  @SuppressWarnings("unused")
  private static final Logger logger =
      LoggerFactory.getLogger(MemoryOnlyKnowledgeBaseProxiesService.class);

  private final ProxiesFactory proxiesFactory;
  private final AdvancedBaseTypesService advancedBaseTypesService;
  private final PrefixMappingService prefixService;

  @Autowired
  public MemoryOnlyKnowledgeBaseProxiesService(final ProxiesFactory proxiesFactory, final AdvancedBaseTypesService advancedBaseTypesService, final PrefixMappingService prefixService) {
    Preconditions.checkNotNull(proxiesFactory);
    Preconditions.checkNotNull(advancedBaseTypesService);
    Preconditions.checkNotNull(prefixService);

    this.advancedBaseTypesService = advancedBaseTypesService;
    this.proxiesFactory = proxiesFactory;
    this.prefixService = prefixService;
  }

  @Override
  public Table<String, String, Proxy> toProxies(final Set<? extends KnowledgeBase> bases) {
    Preconditions.checkNotNull(bases);
    if (bases.isEmpty()) {
      return ImmutableTable.of();
    }
    
    final ImmutableTable.Builder<String, String, Proxy> builder = ImmutableTable.builder();
    
    for (final KnowledgeBase base : bases) {
      final ProxyDefinition definition = this.advancedBaseTypesService.toProxyDefinition(base);
      final Proxy proxy = this.proxiesFactory.create(definition, this.prefixService.getPrefixToUriMap());
      
      builder.put(base.getOwner().getEmail(), base.getName(), proxy);
    }
    
    return builder.build();
  }

  @Override
  public PrefixMappingService getPrefixService() {
    return this.prefixService;
  }

  @Override
  public void set(final KnowledgeBase base) {
    // Do nothing for now. Maybe implement some instance caching later.
  }

  @Override
  public void delete(final KnowledgeBase base) throws IOException {
    Preconditions.checkNotNull(base);
    
    this.proxiesFactory.dispose(base.getQualifiedName());
  }
}
