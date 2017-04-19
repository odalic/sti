/**
 *
 */
package cz.cuni.mff.xrg.odalic.groups;

import java.net.URL;
import java.util.Set;
import java.util.SortedSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;

/**
 * Default {@link GroupsService} implementation.
 *
 */
public final class MemoryOnlyGroupsService implements GroupsService {

  private final Table<String, String, Group> userAndGroupIdsToGroups;
  
  private final Table<String, String, Set<String>> utilizingBases;

  public MemoryOnlyGroupsService() {
    this(HashBasedTable.create(), HashBasedTable.create());
  }
  
  private MemoryOnlyGroupsService(final Table<String, String, Group> usersAndNamesToGroups,
      final Table<String, String, Set<String>> utilizingBases) {
    Preconditions.checkNotNull(usersAndNamesToGroups);
    Preconditions.checkNotNull(utilizingBases);
    
    this.userAndGroupIdsToGroups = usersAndNamesToGroups;
    this.utilizingBases = utilizingBases;
  }
  
  @Override
  public SortedSet<Group> getGroups(final String userId) {
    return ImmutableSortedSet.copyOf(this.userAndGroupIdsToGroups.row(userId).values());
  }

  @Override
  public Group getGroup(final String userId, String groupId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(groupId);
    
    final Group group = this.userAndGroupIdsToGroups.get(userId, groupId);
    Preconditions.checkArgument(group != null, "Unknown group!");

    return group;
  }

  @Override
  public void replace(final Group group) {
    Preconditions.checkNotNull(group);

    this.userAndGroupIdsToGroups.put(group.getOwner().getEmail(), group.getId(), group);
  }

  @Override
  public void create(final Group group) {
    Preconditions.checkArgument(!existsGroupWithId(group.getOwner().getEmail(), group.getId()));

    replace(group);
  }
  
  @Override
  public boolean existsGroupWithId(final String userId, final String groupId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(groupId);

    return this.userAndGroupIdsToGroups.contains(userId, groupId);
  }

  @Override
  public void subscribe(final Group group, final KnowledgeBase base) {
    final String userId = group.getOwner().getEmail();
    final String groupId = group.getId();

    Preconditions.checkArgument(this.userAndGroupIdsToGroups.get(userId, groupId).equals(group),
        "The group is not registered!");

    final Set<String> bases = this.utilizingBases.get(userId, groupId);

    final boolean inserted;
    if (bases == null) {
      this.utilizingBases.put(userId, groupId, Sets.newHashSet(base.getName()));
      inserted = true;
    } else {
      inserted = bases.add(base.getName());
    }

    Preconditions.checkArgument(inserted, "The base has already been subcscribed to the group!");
  }

  @Override
  public void unsubscribe(final Group group, final KnowledgeBase base) {
    final String userId = group.getOwner().getEmail();
    final String groupId = group.getId();

    Preconditions.checkArgument(this.userAndGroupIdsToGroups.get(userId, groupId).equals(group),
        "The group is not registered!");

    final Set<String> bases = this.utilizingBases.get(userId, groupId);

    final boolean removed;
    if (bases == null) {
      removed = false;
    } else {
      removed = bases.remove(base.getName());

      if (bases.isEmpty()) {
        this.utilizingBases.remove(userId, groupId);
      }
    }

    Preconditions.checkArgument(removed, "The base is not subcscribed to the group!");
  }

  @Override
  public Group verifyGroupExistenceById(final String userId, final String groupId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(groupId);

    return this.userAndGroupIdsToGroups.get(userId, groupId);
  }

  @Override
  public Set<Group> detectUsed(final String userId, final URL endpoint) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(endpoint);
    
    // TODO: Implement used groups detection.
    return ImmutableSet.copyOf(this.userAndGroupIdsToGroups.row(userId).values());
  }
}
