/**
 *
 */
package cz.cuni.mff.xrg.odalic.groups;

import java.net.URL;
import java.util.Set;
import java.util.SortedSet;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * Memory-only {@link GroupsService} implementation.
 *
 */
@Component
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
  public Group merge(final Group group) {
    Preconditions.checkNotNull(group);

    final String userId = group.getOwner().getEmail();
    final String groupId = group.getId();
    
    final Group previous = this.userAndGroupIdsToGroups.get(userId, groupId);
    if (previous == null) {
      create(group);
      return group;
    }
    
    final Group merged = previous.merge(group);
    replace(previous.merge(group));
    
    return merged;
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
  public void subscribe(final KnowledgeBase base) {
    final User baseOwner = base.getOwner();
    final String baseName = base.getName();
    
    final Set<Group> groups = base.getSelectedGroups();
    
    for (final Group group : groups) {      
      subscribe(baseOwner, baseName, group);
    }
  }

  private void subscribe(final User baseOwner, final String baseName, final Group group) {
    final User owner = group.getOwner();
    Preconditions.checkArgument(owner.equals(baseOwner),
        "The owner of the group is not the same as the owner of the base!");
    
    final String userId = owner.getEmail();
    final String groupId = group.getId();      
    
    Preconditions.checkArgument(this.userAndGroupIdsToGroups.get(userId, groupId).equals(group),
        "The group is not registered!");
 
    final Set<String> bases = this.utilizingBases.get(userId, groupId);
 
    final boolean inserted;
    if (bases == null) {
      this.utilizingBases.put(userId, groupId, Sets.newHashSet(baseName));
      inserted = true;
    } else {
      inserted = bases.add(baseName);
    }
 
      Preconditions.checkArgument(inserted, "The base has already been subcscribed to the group!");
  }

  @Override
  public void unsubscribe(final KnowledgeBase base) {
    final User baseOwner = base.getOwner();
    final String baseName = base.getName();
    
    final Set<Group> groups = base.getSelectedGroups();
    
    for (final Group group : groups) {      
      unsubscribe(baseOwner, baseName, group);
    }
  }

  private void unsubscribe(final User baseOwner, final String baseName, final Group group) {
    final User owner = group.getOwner();
    Preconditions.checkArgument(owner.equals(baseOwner),
        "The owner of the group is not the same as the owner of the base!");
    
    final String userId = owner.getEmail();
    final String groupId = group.getId();      
    
    Preconditions.checkArgument(this.userAndGroupIdsToGroups.get(userId, groupId).equals(group),
        "The group is not registered!");
 
    final Set<String> bases = this.utilizingBases.get(userId, groupId);
 
    final boolean removed;
    if (bases == null) {
      removed = false;
    } else {
      removed = bases.remove(baseName);
 
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

  @Override
  public void deleteById(String userId, String groupId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(groupId);

    checkUtilization(userId, groupId);

    final Group group = this.userAndGroupIdsToGroups.remove(userId, groupId);
    Preconditions.checkArgument(group != null);
  }
  
  private void checkUtilization(final String userId, final String groupId)
      throws IllegalStateException {
    final Set<String> utilizingBaseIds = this.utilizingBases.get(userId, groupId);
    if (utilizingBaseIds == null) {
      return;
    }

    final String jointUtilizingBaseIds = String.join(", ", utilizingBaseIds);
    Preconditions.checkState(utilizingBaseIds.isEmpty(),
        String.format("Some bases definition (%s) still refer to this group!", jointUtilizingBaseIds));
  }
}
