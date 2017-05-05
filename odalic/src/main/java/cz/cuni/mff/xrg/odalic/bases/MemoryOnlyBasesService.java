package cz.cuni.mff.xrg.odalic.bases;

import java.io.IOException;
import java.util.NavigableSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.bases.proxies.KnowledgeBaseProxiesService;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * Memory-only {@link BasesService} implementation.
 * 
 * @author Václav Brodec
 *
 */
@Component
public final class MemoryOnlyBasesService implements BasesService {

  private final KnowledgeBaseProxiesService knowledgeBaseProxiesService;

  private final GroupsService groupsService;

  private final Table<String, String, KnowledgeBase> userAndBaseIdsToBases;

  private final Table<String, String, Set<String>> utilizingTasks;

  @Autowired
  public MemoryOnlyBasesService(final KnowledgeBaseProxiesService knowledgeBaseProxiesService,
      final GroupsService groupsService) {
    this(knowledgeBaseProxiesService, groupsService, HashBasedTable.create(),
        HashBasedTable.create());
  }

  public MemoryOnlyBasesService(final KnowledgeBaseProxiesService knowledgeBaseProxiesService,
      final GroupsService groupsService,
      final Table<String, String, KnowledgeBase> userAndBaseIdsToBases,
      final Table<String, String, Set<String>> utilizingTasks) {
    Preconditions.checkNotNull(knowledgeBaseProxiesService);
    Preconditions.checkNotNull(groupsService);
    Preconditions.checkNotNull(userAndBaseIdsToBases);
    Preconditions.checkNotNull(utilizingTasks);

    this.knowledgeBaseProxiesService = knowledgeBaseProxiesService;
    this.groupsService = groupsService;
    this.userAndBaseIdsToBases = userAndBaseIdsToBases;
    this.utilizingTasks = utilizingTasks;
  }

  @Override
  public NavigableSet<KnowledgeBase> getBases(final String userId) {
    Preconditions.checkNotNull(userId);

    return ImmutableSortedSet.copyOf(this.userAndBaseIdsToBases.row(userId).values());
  }

  @Override
  public NavigableSet<KnowledgeBase> getInsertSupportingBases(final String userId) {
    Preconditions.checkNotNull(userId);

    return this.userAndBaseIdsToBases.row(userId).values().stream().filter(e -> e.isInsertEnabled())
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

    final KnowledgeBase previous = this.userAndBaseIdsToBases.put(userId, baseId, base);
    if (previous != null) {
      this.groupsService.unsubscribe(previous);
    }

    this.knowledgeBaseProxiesService.set(base);
    this.groupsService.subscribe(base);
  }

  @Override
  public KnowledgeBase merge(final KnowledgeBase base) {
    Preconditions.checkNotNull(base);

    final String userId = base.getOwner().getEmail();
    final String baseId = base.getName();

    final KnowledgeBase previous = this.userAndBaseIdsToBases.get(userId, baseId);
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

  @Override
  public void deleteById(String userId, String name) throws IOException {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(name);

    checkUtilization(userId, name);

    final KnowledgeBase base = this.userAndBaseIdsToBases.remove(userId, name);
    Preconditions.checkArgument(base != null);

    this.knowledgeBaseProxiesService.delete(base);
    this.groupsService.unsubscribe(base);
  }

  private void checkUtilization(final String userId, final String name)
      throws IllegalStateException {
    final Set<String> utilizingTaskIds = this.utilizingTasks.get(userId, name);
    if (utilizingTaskIds == null) {
      return;
    }

    final String jointUtilizingTasksIds = String.join(", ", utilizingTaskIds);
    Preconditions.checkState(utilizingTaskIds.isEmpty(),
        String.format("Some tasks (%s) still refer to this base!", jointUtilizingTasksIds));
  }

  @Override
  public void subscribe(final Task task) {
    final User taskOwner = task.getOwner();
    final String taskId = task.getId();

    final Set<KnowledgeBase> bases = task.getConfiguration().getUsedBases();

    for (final KnowledgeBase base : bases) {
      subscribe(taskOwner, taskId, base);
    }
  }

  private void subscribe(final User taskOwner, final String taskId, final KnowledgeBase base) {
    final User owner = base.getOwner();
    Preconditions.checkArgument(owner.equals(taskOwner),
        "The owner of the base is not the same as the owner of the task!");

    final String userId = owner.getEmail();
    final String baseName = base.getName();

    Preconditions.checkArgument(this.userAndBaseIdsToBases.get(userId, baseName).equals(base),
        "The base is not registered!");

    final Set<String> tasks = this.utilizingTasks.get(userId, baseName);

    final boolean inserted;
    if (tasks == null) {
      this.utilizingTasks.put(userId, baseName, Sets.newHashSet(taskId));
      inserted = true;
    } else {
      inserted = tasks.add(taskId);
    }

    Preconditions.checkArgument(inserted, "The task has already been subcscribed to the base!");
  }

  @Override
  public void unsubscribe(final Task task) {
    final User taskOwner = task.getOwner();
    final String taskId = task.getId();

    final Set<KnowledgeBase> bases = task.getConfiguration().getUsedBases();

    for (final KnowledgeBase base : bases) {
      unsubscribe(taskOwner, taskId, base);
    }
  }

  private void unsubscribe(final User taskOwner, final String taskId, final KnowledgeBase base) {
    final User owner = base.getOwner();
    Preconditions.checkArgument(owner.equals(taskOwner),
        "The owner of the base is not the same as the owner of the task!");

    final String userId = owner.getEmail();
    final String baseName = base.getName();

    Preconditions.checkArgument(this.userAndBaseIdsToBases.get(userId, baseName).equals(base),
        "The base is not registered!");

    final Set<String> tasks = this.utilizingTasks.get(userId, baseName);

    final boolean removed;
    if (tasks == null) {
      removed = false;
    } else {
      removed = tasks.remove(taskId);

      if (tasks.isEmpty()) {
        this.utilizingTasks.remove(userId, baseName);
      }
    }

    Preconditions.checkArgument(removed, "The task is not subcscribed to the base!");
  }
}
