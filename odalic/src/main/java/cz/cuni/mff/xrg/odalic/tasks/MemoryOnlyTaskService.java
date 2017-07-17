package cz.cuni.mff.xrg.odalic.tasks;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.jena.ext.com.google.common.collect.ImmutableSortedSet;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.files.FileService;

/**
 * This {@link TaskService} implementation provides no persistence.
 *
 * @author Václav Brodec
 * @author Josef Janoušek
 *
 */
public final class MemoryOnlyTaskService implements TaskService {

  private final FileService fileService;
  private final BasesService basesService;

  /**
   * Table of tasks where rows are indexed by user IDs and the columns by task IDs.
   */
  private final Table<String, String, Task> tasks;


  /**
   * Creates the task service with no registered tasks.
   * 
   * @param fileService file service
   * @param basesService bases service
   */
  @Autowired
  public MemoryOnlyTaskService(final FileService fileService, final BasesService basesService) {
    this(fileService, basesService, HashBasedTable.create());
  }

  private MemoryOnlyTaskService(final FileService fileService, final BasesService basesService,
      final Table<String, String, Task> tasks) {
    Preconditions.checkNotNull(fileService, "The fileService cannot be null!");
    Preconditions.checkNotNull(basesService, "The basesService cannot be null!");
    Preconditions.checkNotNull(tasks, "The tasks cannot be null!");

    this.fileService = fileService;
    this.basesService = basesService;
    this.tasks = tasks;
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

    final Map<String, Task> taskIdsToTasks = this.tasks.row(userId);
    taskIdsToTasks.entrySet().stream().forEach(e -> {
      this.fileService.unsubscribe(e.getValue());
      this.basesService.unsubscribe(e.getValue());
    });
    taskIdsToTasks.clear();
  }

  @Override
  public void deleteById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(taskId, "The taskId cannot be null!");

    final Task task = this.tasks.remove(userId, taskId);
    Preconditions.checkArgument(task != null, String.format("There is not task %s registered to user %s!", taskId, userId));

    this.fileService.unsubscribe(task);
    this.basesService.unsubscribe(task);
  }

  @Override
  public Task getById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(taskId, "The taskId cannot be null!");

    final Task task = this.tasks.get(userId, taskId);
    Preconditions.checkArgument(task != null, String.format("There is not task %s registered to user %s!", taskId, userId));

    return task;
  }

  @Override
  public Set<Task> getTasks(final String userId) {
    return ImmutableSet.copyOf(this.tasks.row(userId).values());
  }

  @Override
  public NavigableSet<Task> getTasksSortedByCreatedInDescendingOrder(final String userId) {
    return ImmutableSortedSet
        .copyOf(
            (final Task first, final Task second) -> -1
                * first.getCreated().compareTo(second.getCreated()),
            this.tasks.row(userId).values());
  }

  @Override
  public NavigableSet<Task> getTasksSortedByIdInAscendingOrder(final String userId) {
    return ImmutableSortedSet.copyOf(
        (final Task first, final Task second) -> first.getId().compareTo(second.getId()),
        this.tasks.row(userId).values());
  }

  @Override
  public void replace(final Task task) {
    Preconditions.checkNotNull(task, "The task cannot be null!");

    final Task previous = this.tasks.put(task.getOwner().getEmail(), task.getId(), task);
    if (previous != null) {
      this.fileService.unsubscribe(previous);
      this.basesService.unsubscribe(previous);
    }

    this.fileService.subscribe(task);
    this.basesService.subscribe(task);
  }

  @Override
  @Nullable
  public Task verifyTaskExistenceById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(taskId, "The taskId cannot be null!");

    return this.tasks.get(userId, taskId);
  }
}
