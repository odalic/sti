package cz.cuni.mff.xrg.odalic.tasks;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.bases.BasesService;
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

  private final BasesService basesService;
  
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
  public DbTaskService(final FileService fileService, final BasesService basesService, final DbService dbService) {
    Preconditions.checkNotNull(fileService, "The fileService cannot be null!");
    Preconditions.checkNotNull(basesService, "The basesService cannot be null!");
    Preconditions.checkNotNull(dbService, "The dbService cannot be null!");

    this.fileService = fileService;
    this.basesService = basesService;
    
    this.db = dbService.getDb();

    this.tasks = this.db.treeMap("tasks")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
  }

  @Override
  public void create(final Task task) {
    Preconditions.checkNotNull(task, "The task cannot be null!");
    Preconditions
        .checkArgument(verifyTaskExistenceById(task.getOwner().getEmail(), task.getId()) == null);

    replace(task);
  }

  @Override
  public void deleteAll(final String userId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");

    try {
      final Map<Object[], Task> taskIdsToTasks = this.tasks.prefixSubMap(new Object[] {userId});
      taskIdsToTasks.entrySet().stream().forEach(e -> {
        this.fileService.unsubscribe(e.getValue());
        this.basesService.unsubscribe(e.getValue());
      });
      taskIdsToTasks.clear();
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }

  @Override
  public void deleteById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(taskId, "The taskId cannot be null!");

    try {
      final Task task = this.tasks.remove(new Object[] {userId, taskId});
      Preconditions.checkArgument(task != null, String.format("There is not task %s registered to user %s!", taskId, userId));
  
      this.fileService.unsubscribe(task);
      this.basesService.unsubscribe(task);
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }

    this.db.commit();
  }

  @Override
  public Task getById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(taskId, "The taskId cannot be null!");

    final Task task = this.tasks.get(new Object[] {userId, taskId});
    Preconditions.checkArgument(task != null, String.format("There is not task %s registered to user %s!", taskId, userId));

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
    Preconditions.checkNotNull(task, "The task cannot be null!");

    final Task previous =
        this.tasks.put(new Object[] {task.getOwner().getEmail(), task.getId()}, task);
    if (previous != null) {
      try {
        this.fileService.unsubscribe(previous);
        this.basesService.unsubscribe(previous);
      } catch (final Exception e) {
        this.db.rollback();
        throw e;
      }
    }

    try {
      this.fileService.subscribe(task);
      this.basesService.subscribe(task);
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }

    this.db.commit();
  }

  @Override
  @Nullable
  public Task verifyTaskExistenceById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(taskId, "The taskId cannot be null!");

    return this.tasks.get(new Object[] {userId, taskId});
  }
}
