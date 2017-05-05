package cz.cuni.mff.xrg.odalic.bases.proxies;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
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
import cz.cuni.mff.xrg.odalic.util.storage.DbService;
import uk.ac.shef.dcs.kbproxy.Proxy;
import uk.ac.shef.dcs.kbproxy.ProxyDefinition;
import uk.ac.shef.dcs.kbproxy.ProxiesFactory;

/**
 * @author Václav Brodec
 */
public final class DbKnowledgeBaseProxiesService implements KnowledgeBaseProxiesService {

  @SuppressWarnings("unused")
  private static final Logger logger =
      LoggerFactory.getLogger(DbKnowledgeBaseProxiesService.class);

  private final ProxiesFactory proxiesFactory;
  private final AdvancedBaseTypesService advancedBaseTypesService;
  private final PrefixMappingService prefixService;
  
  private final DB db;
  
  private final Map<Object[], Proxy> userIdsAndBaseNamesToProxies;

  @SuppressWarnings("unchecked")
  @Autowired
  public DbKnowledgeBaseProxiesService(final ProxiesFactory proxiesFactory, final AdvancedBaseTypesService advancedBaseTypesService, final PrefixMappingService prefixService, final DbService dbService) {
    Preconditions.checkNotNull(proxiesFactory);
    Preconditions.checkNotNull(advancedBaseTypesService);
    Preconditions.checkNotNull(prefixService);
    Preconditions.checkNotNull(dbService);

    this.advancedBaseTypesService = advancedBaseTypesService;
    this.proxiesFactory = proxiesFactory;
    this.prefixService = prefixService;
    
    this.db = dbService.getDb();
    
    this.userIdsAndBaseNamesToProxies = this.db.treeMap("userIdsAndBaseNamesToProxies")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen(); 
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
    
    for (final Map.Entry<Object[], Proxy> entry : this.userIdsAndBaseNamesToProxies.entrySet()) {
      final Object[] key = entry.getKey();
      
      final String entryUserId = (String) key[0];
      final String entryBaseName = (String) key[1];
      
      if (!entryUserId.equals(ownerId)) {
        continue;
      }
      
      if (!allowedBaseNames.contains(entryBaseName)) {
        continue;
      }
      
      result.put(entryUserId, entryBaseName, entry.getValue());
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
    
    this.userIdsAndBaseNamesToProxies.put(new Object[]{base.getOwner().getEmail(), base.getName()}, proxy);
    
    this.db.commit();
  }

  private static String createProxyId(final KnowledgeBase base) {
    return base.getOwner() + "_" + base.getName();
  }

  @Override
  public void delete(final KnowledgeBase base) throws IOException {
    final Proxy previous = this.userIdsAndBaseNamesToProxies.remove(new Object[]{base.getOwner().getEmail(), base.getName()});
    Preconditions.checkArgument(previous != null);
    
    try {
      this.proxiesFactory.dispose(previous.getName());
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }
}
