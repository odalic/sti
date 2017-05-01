/**
 *
 */
package cz.cuni.mff.xrg.odalic.bases;

import java.util.NavigableSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.bases.proxies.KnowledgeBaseProxiesService;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;

/**
 * Default {@link BasesService} implementation.
 *
 */
public final class MemoryOnlyBasesService implements BasesService {

  private final KnowledgeBaseProxiesService knowledgeBaseProxiesService;
  
  private final Table<String, String, KnowledgeBase> userAndBaseIdsToBases;

  private GroupsService groupsService;
  
  public MemoryOnlyBasesService(final KnowledgeBaseProxiesService knowledgeBaseProxiesService, final GroupsService groupsService) {
    this(knowledgeBaseProxiesService, groupsService, HashBasedTable.create());
  }
  
  public MemoryOnlyBasesService(final KnowledgeBaseProxiesService knowledgeBaseProxiesService, final GroupsService groupsService, final Table<String, String, KnowledgeBase> userAndBaseIdsToBases) {
    Preconditions.checkNotNull(knowledgeBaseProxiesService);
    Preconditions.checkNotNull(groupsService);
    Preconditions.checkNotNull(userAndBaseIdsToBases);

    this.knowledgeBaseProxiesService = knowledgeBaseProxiesService;
    this.groupsService = groupsService;
    this.userAndBaseIdsToBases = userAndBaseIdsToBases;
  }

  @Override
  public NavigableSet<KnowledgeBase> getBases(final String userId) {
    Preconditions.checkNotNull(userId);
    
    return ImmutableSortedSet.copyOf(this.userAndBaseIdsToBases.row(userId).values());
  }

  @Override
  public NavigableSet<KnowledgeBase> getInsertSupportingBases(final String userId) {
    Preconditions.checkNotNull(userId);
    
    return this.userAndBaseIdsToBases.row(userId).values().stream().filter(e -> e.isInsertEnabled()).collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
  }

  @Override
  public void create(final KnowledgeBase base) {
    Preconditions.checkArgument(!existsBaseWithId(base.getOwner().getEmail(), base.getName()));

    replace(base);
  }
  
  @Override
  public void replace(final KnowledgeBase base) {
    Preconditions.checkNotNull(base);
    
    final String userId = base.getOwner().getEmail();
    final String baseId = base.getName();

    final KnowledgeBase previous = this.userAndBaseIdsToBases.put(userId, baseId, base);
    if (previous != null) {
      this.groupsService.unsubscribe(previous);
    }
    
    this.knowledgeBaseProxiesService.set(base);
    this.groupsService.subscribe(base);
  }
  
  @Override
  public boolean existsBaseWithId(final String userId, final String baseId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(baseId);

    return this.userAndBaseIdsToBases.contains(userId, baseId);
  }

  @Override
  public KnowledgeBase getByName(String userId, String name) throws IllegalArgumentException {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(name);
    
    final KnowledgeBase base = this.userAndBaseIdsToBases.get(userId, name);
    Preconditions.checkArgument(base != null, "Unknown base!");

    return base;
  }

  @Override
  public KnowledgeBase verifyBaseExistenceByName(String userId, String name) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(name);

    return this.userAndBaseIdsToBases.get(userId, name);
  }
}
