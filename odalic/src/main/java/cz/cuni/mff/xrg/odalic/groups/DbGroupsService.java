/**
 *
 */
package cz.cuni.mff.xrg.odalic.groups;

import java.net.URL;
import java.util.Set;
import java.util.SortedSet;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.util.storage.DbService;

/**
 * This {@link GroupsService} implementation persists the files in {@link DB}-backed maps.
 *
 * @author Václav Brodec
 */
@Component
public final class DbGroupsService implements GroupsService {

  private final DB db;

  private final BTreeMap<Object[], Group> userAndGroupIdsToGroups;

  private final BTreeMap<Object[], Boolean> utilizingBases;

  @Autowired
  @SuppressWarnings("unchecked")
  public DbGroupsService(final DbService dbService) {
    Preconditions.checkNotNull(dbService);

    this.db = dbService.getDb();

    this.userAndGroupIdsToGroups = this.db.treeMap("userAndGroupIdsToGroups")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
    this.utilizingBases = this.db.treeMap("utilizingBases")
        .keySerializer(
            new SerializerArrayTuple(Serializer.STRING, Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.BOOLEAN).createOrOpen();
  }

  @Override
  public SortedSet<Group> getGroups(final String userId) {
    return ImmutableSortedSet
        .copyOf(this.userAndGroupIdsToGroups.prefixSubMap(new Object[] {userId}).values());
  }

  @Override
  public Group getGroup(final String userId, String groupId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(groupId);

    final Group group = this.userAndGroupIdsToGroups.get(new Object[] {userId, groupId});
    Preconditions.checkArgument(group != null, "Unknown group!");

    return group;
  }

  @Override
  public void replace(final Group group) {
    Preconditions.checkNotNull(group);

    this.userAndGroupIdsToGroups.put(new Object[] {group.getOwner().getEmail(), group.getId()},
        group);

    this.db.commit();
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

    return this.userAndGroupIdsToGroups.containsKey(new Object[] {userId, groupId});
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

    final Object[] userGroupId = new Object[] {userId, groupId};

    Preconditions.checkArgument(this.userAndGroupIdsToGroups.get(userGroupId).equals(group),
        "The group is not registered!");

    final Boolean previous =
        this.utilizingBases.put(new Object[] {userId, groupId, baseName}, true);
    Preconditions.checkArgument(previous == null,
        "The base has already been subcscribed to the group!");
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

    Preconditions.checkArgument(
        this.userAndGroupIdsToGroups.get(new Object[] {userId, groupId}).equals(group),
        "The group is not registered!");

    final Boolean removed = this.utilizingBases.remove(new Object[] {userId, groupId, baseName});
    Preconditions.checkArgument(removed != null, "The base is not subcscribed to the group!");

    Preconditions.checkArgument(removed, "The base is not subcscribed to the group!");
  }

  @Override
  public Group verifyGroupExistenceById(final String userId, final String groupId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(groupId);

    return this.userAndGroupIdsToGroups.get(new Object[] {userId, groupId});
  }

  @Override
  public Set<Group> detectUsed(final String userId, final URL endpoint) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(endpoint);

    // TODO: Implement used groups detection.
    return ImmutableSet
        .copyOf(this.userAndGroupIdsToGroups.prefixSubMap(new Object[] {userId}).values());
  }

  @Override
  public void deleteById(String userId, String groupId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(groupId);

    checkUtilization(userId, groupId);

    final Group group = this.userAndGroupIdsToGroups.remove(new Object[] {userId, groupId});
    Preconditions.checkArgument(group != null);
  }

  private void checkUtilization(final String userId, final String groupId)
      throws IllegalStateException {
    final Set<String> utilizingBaseIds =
        this.utilizingBases.prefixSubMap(new Object[] {userId, groupId}).keySet().stream()
            .map(e -> (String) e[2]).collect(ImmutableSet.toImmutableSet());

    if (!utilizingBaseIds.isEmpty()) {
      final String jointUtilizingBaseIds = String.join(", ", utilizingBaseIds);
      throw new IllegalStateException(
          String.format("Some bases (%s) still refer to this group!", jointUtilizingBaseIds));
    }
  }

  @Override
  public Group merge(Group group) {
    Preconditions.checkNotNull(group);

    final String userId = group.getOwner().getEmail();
    final String groupId = group.getId();
    
    final Group previous = this.userAndGroupIdsToGroups.get(new Object[] {userId, groupId});
    if (previous == null) {
      create(group);
      return group;
    }
    
    final Group merged = previous.merge(group);
    replace(merged);
    
    return merged;
  }
}
