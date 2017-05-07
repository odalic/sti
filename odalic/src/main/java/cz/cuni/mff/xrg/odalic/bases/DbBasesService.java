package cz.cuni.mff.xrg.odalic.bases;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import cz.cuni.mff.xrg.odalic.bases.proxies.KnowledgeBaseProxiesService;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.util.storage.DbService;

/**
 * This {@link BasesService} implementation persists the files in {@link DB}-backed maps.
 *
 * @author Václav Brodec
 */
@Component
public final class DbBasesService implements BasesService {

  private final KnowledgeBaseProxiesService knowledgeBaseProxiesService;

  private final GroupsService groupsService;

  private final DB db;
  
  private final BTreeMap<Object[], KnowledgeBase> userAndBaseIdsToBases;

  private final BTreeMap<Object[], Boolean> utilizingTasks;

  @Autowired
  @SuppressWarnings("unchecked")
  public DbBasesService(final KnowledgeBaseProxiesService knowledgeBaseProxiesService,
      final GroupsService groupsService, final DbService dbService) {
    Preconditions.checkNotNull(knowledgeBaseProxiesService);
    Preconditions.checkNotNull(groupsService);
    Preconditions.checkNotNull(dbService);

    this.db = dbService.getDb();

    this.knowledgeBaseProxiesService = knowledgeBaseProxiesService;
    this.groupsService = groupsService;
    
    this.userAndBaseIdsToBases = this.db.treeMap("userAndBaseIdsToBases")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
    this.utilizingTasks = this.db.treeMap("utilizingTasks")
        .keySerializer(
            new SerializerArrayTuple(Serializer.STRING, Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.BOOLEAN).createOrOpen();
  }

  @Override
  public NavigableSet<KnowledgeBase> getBases(final String userId) {
    Preconditions.checkNotNull(userId);

    return ImmutableSortedSet.copyOf(this.userAndBaseIdsToBases.prefixSubMap(new Object[] {userId}).values());
  }

  @Override
  public NavigableSet<KnowledgeBase> getInsertSupportingBases(final String userId) {
    Preconditions.checkNotNull(userId);

    return this.userAndBaseIdsToBases.prefixSubMap(new Object[] {userId}).values().stream().filter(e -> e.isInsertEnabled())
        .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
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

    final KnowledgeBase previous = this.userAndBaseIdsToBases.put(new Object[] {userId, baseId}, base);
    
    try {
      if (previous != null) {
        this.groupsService.unsubscribe(previous);
      }
  
      this.knowledgeBaseProxiesService.set(base);
      this.groupsService.subscribe(base);
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }

  @Override
  public KnowledgeBase merge(final KnowledgeBase base) {
    Preconditions.checkNotNull(base);

    final String userId = base.getOwner().getEmail();
    final String baseId = base.getName();

    final KnowledgeBase previous = this.userAndBaseIdsToBases.get(new Object[] {userId, baseId});
    if (previous == null) {
      create(base);
      return base;
    }

    return previous;
  }

  @Override
  public boolean existsBaseWithId(final String userId, final String baseId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(baseId);

    return this.userAndBaseIdsToBases.containsKey(new Object[]{userId, baseId});
  }

  @Override
  public KnowledgeBase getByName(String userId, String name) throws IllegalArgumentException {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(name);

    final KnowledgeBase base = this.userAndBaseIdsToBases.get(new Object[] {userId, name});
    Preconditions.checkArgument(base != null, "Unknown base!");

    return base;
  }

  @Override
  public KnowledgeBase verifyBaseExistenceByName(String userId, String name) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(name);

    return this.userAndBaseIdsToBases.get(new Object[] {userId, name});
  }

  @Override
  public void deleteAll(final String userId) {
    Preconditions.checkNotNull(userId);

    try {
      final Map<Object[], KnowledgeBase> baseIdsToBases = this.userAndBaseIdsToBases.prefixSubMap(new Object[] {userId});
      baseIdsToBases.entrySet().stream().forEach(e -> this.groupsService
          .unsubscribe(e.getValue()));
      baseIdsToBases.clear();
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }
  
  @Override
  public void deleteById(String userId, String name) throws IOException {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(name);

    checkUtilization(userId, name);

    final KnowledgeBase base = this.userAndBaseIdsToBases.remove(new Object[] {userId, name});
    Preconditions.checkArgument(base != null);

    try {
      this.knowledgeBaseProxiesService.delete(base);
      this.groupsService.unsubscribe(base);
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }

  private void checkUtilization(final String userId, final String name)
      throws IllegalStateException {
    final Set<String> utilizingTaskIds =
        this.utilizingTasks.prefixSubMap(new Object[] {userId, name}).keySet().stream()
            .map(e -> (String) e[2]).collect(ImmutableSet.toImmutableSet());

    if (!utilizingTaskIds.isEmpty()) {
      final String jointUtilizingTasksIds = String.join(", ", utilizingTaskIds);
      throw new IllegalStateException(
          String.format("Some tasks (%s) still refer to this base!", jointUtilizingTasksIds));
    }
  }

  @Override
  public void subscribe(final Task task) {
    final User taskOwner = task.getOwner();
    final String taskId = task.getId();

    final Set<KnowledgeBase> bases = task.getConfiguration().getUsedBases();

    try {
      for (final KnowledgeBase base : bases) {
        subscribe(taskOwner, taskId, base);
      }
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }

  private void subscribe(final User taskOwner, final String taskId, final KnowledgeBase base) {
    final User owner = base.getOwner();
    Preconditions.checkArgument(owner.equals(taskOwner),
        "The owner of the base is not the same as the owner of the task!");

    final String userId = owner.getEmail();
    final String baseName = base.getName();

    final Object[] userIdBaseName = new Object[] {userId, baseName};

    Preconditions.checkArgument(this.userAndBaseIdsToBases.get(userIdBaseName).equals(base),
        "The base is not registered!");

    final Boolean previous =
        this.utilizingTasks.put(new Object[] {userId, baseName, taskId}, true);
    Preconditions.checkArgument(previous == null,
        "The task has already been subcscribed to the base!");
  }

  @Override
  public void unsubscribe(final Task task) {
    final User taskOwner = task.getOwner();
    final String taskId = task.getId();

    final Set<KnowledgeBase> bases = task.getConfiguration().getUsedBases();

    try {
      for (final KnowledgeBase base : bases) {
        unsubscribe(taskOwner, taskId, base);
      }
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }

  private void unsubscribe(final User taskOwner, final String taskId, final KnowledgeBase base) {
    final User owner = base.getOwner();
    Preconditions.checkArgument(owner.equals(taskOwner),
        "The owner of the base is not the same as the owner of the task!");

    final String userId = owner.getEmail();
    final String baseName = base.getName();

    Preconditions.checkArgument(
        this.userAndBaseIdsToBases.get(new Object[] {userId, baseName}).equals(base),
        "The base is not registered!");

    final Boolean removed = this.utilizingTasks.remove(new Object[] {userId, baseName, taskId});
    Preconditions.checkArgument(removed != null, "The task is not subcscribed to the base!");

    Preconditions.checkArgument(removed, "The task is not subcscribed to the base!");
  }
}
