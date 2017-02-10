package cz.cuni.mff.xrg.odalic.tasks;

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
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
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

  /**
   * Creates the task service with no registered tasks.
   */
  @SuppressWarnings("unchecked")
  @Autowired
  public DbTaskService(final FileService fileService, final DbService dbService) {
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(dbService);

    this.fileService = fileService;

    this.db = dbService.getDb();

    this.tasks = db.treeMap("tasks")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#getTasks()
   */
  @Override
  public Set<Task> getTasks(String userId) {
    return ImmutableSet.copyOf(tasks.prefixSubMap(new Object[] {userId}).values());
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#getById(java.lang.String)
   */
  @Override
  public Task getById(String userId, String taskId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);

    final Task task = tasks.get(new Object[] {userId, taskId});
    Preconditions.checkArgument(task != null);

    return task;
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#deleteById(java.lang.String)
   */
  @Override
  public void deleteById(String userId, String taskId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);

    final Task task = tasks.remove(new Object[] {userId, taskId});
    Preconditions.checkArgument(task != null);

    final Configuration configuration = task.getConfiguration();
    fileService.unsubscribe(configuration.getInput(), task);

    db.commit();
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#verifyTaskExistenceById(java.lang.String)
   */
  @Override
  @Nullable
  public Task verifyTaskExistenceById(String userId, String taskId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);

    return tasks.get(new Object[] {userId, taskId});
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#create(cz.cuni.mff.xrg.odalic.tasks.Task)
   */
  @Override
  public void create(final Task task) {
    Preconditions.checkNotNull(task);
    Preconditions
        .checkArgument(verifyTaskExistenceById(task.getOwner().getEmail(), task.getId()) == null);

    replace(task);
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#replace(cz.cuni.mff.xrg.odalic.tasks.Task)
   */
  @Override
  public void replace(final Task task) {
    Preconditions.checkNotNull(task);

    final Task previous = tasks.put(new Object[] {task.getOwner().getEmail(), task.getId()}, task);
    if (previous != null) {
      final Configuration previousConfiguration = previous.getConfiguration();
      final File previousInput = previousConfiguration.getInput();

      try {
        fileService.unsubscribe(previousInput, previous);
      } catch (final Exception e) {
        db.rollback();
        throw e;
      }
    }

    final Configuration configuration = task.getConfiguration();
    final File input = configuration.getInput();

    try {
      fileService.subscribe(input, task);
    } catch (final Exception e) {
      db.rollback();
      throw e;
    }

    db.commit();
  }

  @Override
  public NavigableSet<Task> getTasksSortedByIdInAscendingOrder(String userId) {
    return ImmutableSortedSet.copyOf(
        (Task first, Task second) -> first.getId().compareTo(second.getId()),
        tasks.prefixSubMap(new Object[] {userId}).values());
  }

  @Override
  public NavigableSet<Task> getTasksSortedByCreatedInDescendingOrder(String userId) {
    return ImmutableSortedSet.copyOf(
        (Task first, Task second) -> -1 * first.getCreated().compareTo(second.getCreated()),
        tasks.prefixSubMap(new Object[] {userId}).values());
  }
}
