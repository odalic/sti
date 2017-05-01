package cz.cuni.mff.xrg.odalic.tasks;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.jena.ext.com.google.common.collect.ImmutableSortedSet;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.util.storage.DbService;

/**
 * This {@link TaskService} implementation persists the files in {@link DB}-backed maps.
 *
 * @author Václav Brodec
 * @author Josef Janoušek
 *
 */
public final class DbTaskService implements TaskService {

  private final FileService fileService;

  /**
   * The shared database instance.
   */
  private final DB db;

  /**
   * Table of tasks where rows are indexed by user IDs and the columns by task IDs (represented as
   * an array of size 2).
   */
  private final BTreeMap<Object[], Task> tasks;

  @SuppressWarnings("unchecked")
  @Autowired
  public DbTaskService(final FileService fileService, final DbService dbService) {
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(dbService);

    this.fileService = fileService;

    this.db = dbService.getDb();

    this.tasks = this.db.treeMap("tasks")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
  }

  @Override
  public void create(final Task task) {
    Preconditions.checkNotNull(task);
    Preconditions
        .checkArgument(verifyTaskExistenceById(task.getOwner().getEmail(), task.getId()) == null);

    replace(task);
  }

  @Override
  public void deleteAll(final String userId) {
    Preconditions.checkNotNull(userId);

    final Map<Object[], Task> taskIdsToTasks = this.tasks.prefixSubMap(new Object[] {userId});
    taskIdsToTasks.entrySet().stream().forEach(e -> this.fileService
        .unsubscribe(e.getValue()));
    taskIdsToTasks.clear();
  }

  @Override
  public void deleteById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);

    final Task task = this.tasks.remove(new Object[] {userId, taskId});
    Preconditions.checkArgument(task != null);

    this.fileService.unsubscribe(task);

    this.db.commit();
  }

  @Override
  public Task getById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);

    final Task task = this.tasks.get(new Object[] {userId, taskId});
    Preconditions.checkArgument(task != null);

    return task;
  }

  @Override
  public Set<Task> getTasks(final String userId) {
    return ImmutableSet.copyOf(this.tasks.prefixSubMap(new Object[] {userId}).values());
  }

  @Override
  public NavigableSet<Task> getTasksSortedByCreatedInDescendingOrder(final String userId) {
    return ImmutableSortedSet.copyOf(
        (final Task first, final Task second) -> -1
            * first.getCreated().compareTo(second.getCreated()),
        this.tasks.prefixSubMap(new Object[] {userId}).values());
  }

  @Override
  public NavigableSet<Task> getTasksSortedByIdInAscendingOrder(final String userId) {
    return ImmutableSortedSet.copyOf(
        (final Task first, final Task second) -> first.getId().compareTo(second.getId()),
        this.tasks.prefixSubMap(new Object[] {userId}).values());
  }

  @Override
  public void replace(final Task task) {
    Preconditions.checkNotNull(task);

    final Task previous =
        this.tasks.put(new Object[] {task.getOwner().getEmail(), task.getId()}, task);
    if (previous != null) {
      try {
        this.fileService.unsubscribe(previous);
      } catch (final Exception e) {
        this.db.rollback();
        throw e;
      }
    }

    try {
      this.fileService.subscribe(task);
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }

    this.db.commit();
  }

  @Override
  @Nullable
  public Task verifyTaskExistenceById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);

    return this.tasks.get(new Object[] {userId, taskId});
  }
}
