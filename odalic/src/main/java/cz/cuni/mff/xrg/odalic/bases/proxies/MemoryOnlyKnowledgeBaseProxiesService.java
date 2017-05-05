package cz.cuni.mff.xrg.odalic.bases.proxies;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
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
  
  private final Table<String, String, Proxy> userIdsAndBaseNamesToProxies;

  @Autowired
  public MemoryOnlyKnowledgeBaseProxiesService(final ProxiesFactory proxiesFactory, final AdvancedBaseTypesService advancedBaseTypesService, final PrefixMappingService prefixService) {
    this(proxiesFactory, advancedBaseTypesService, prefixService, HashBasedTable.create());
  }
  
  private MemoryOnlyKnowledgeBaseProxiesService(final ProxiesFactory proxiesFactory, final AdvancedBaseTypesService advancedBaseTypesService, final PrefixMappingService prefixService, final Table<String, String, Proxy> userIdsAndBaseNamesToProxies) {
    Preconditions.checkNotNull(proxiesFactory);
    Preconditions.checkNotNull(advancedBaseTypesService);
    Preconditions.checkNotNull(prefixService);

    this.advancedBaseTypesService = advancedBaseTypesService;
    this.proxiesFactory = proxiesFactory;
    this.prefixService = prefixService;
    this.userIdsAndBaseNamesToProxies = userIdsAndBaseNamesToProxies;
  }

  @Override
  public Table<String, String, Proxy> toProxies(final Set<? extends KnowledgeBase> bases) {
    Preconditions.checkNotNull(bases);
    if (bases.isEmpty()) {
      return ImmutableTable.of();
    }
    
    final String ownerId = bases.iterator().next().getOwner().getEmail();
    Preconditions.checkArgument(bases.stream().allMatch(e -> e.getOwner().getEmail().equals(ownerId)));
    
    final Set<String> allowedBaseNames = bases.stream().map(e -> e.getName()).collect(ImmutableSet.toImmutableSet());
    
    final Table<String, String, Proxy> result = HashBasedTable.create();
    
    for (final Table.Cell<String, String, Proxy> cell : this.userIdsAndBaseNamesToProxies.cellSet()) {
      if (!cell.getRowKey().equals(ownerId)) {
        continue;
      }
      
      if (!allowedBaseNames.contains(cell.getColumnKey())) {
        continue;
      }
      
      result.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
    }
    
    return ImmutableTable.copyOf(result);
  }

  @Override
  public PrefixMappingService getPrefixService() {
    return this.prefixService;
  }

  @Override
  public void set(final KnowledgeBase base) {
    final ProxyDefinition definition = this.advancedBaseTypesService.toProxyDefinition(base);
    final String proxyId = createProxyId(base);
    
    final Proxy proxy = this.proxiesFactory.create(proxyId, definition, this.prefixService.getPrefixToUriMap());
    
    this.userIdsAndBaseNamesToProxies.put(base.getOwner().getEmail(), base.getName(), proxy);
  }

  private static String createProxyId(final KnowledgeBase base) {
    return base.getOwner() + "_" + base.getName();
  }

  @Override
  public void delete(final KnowledgeBase base) throws IOException {
    final Proxy previous = this.userIdsAndBaseNamesToProxies.remove(base.getOwner().getEmail(), base.getName());
    Preconditions.checkArgument(previous != null);
    
    this.proxiesFactory.dispose(previous.getName());
  }
}
