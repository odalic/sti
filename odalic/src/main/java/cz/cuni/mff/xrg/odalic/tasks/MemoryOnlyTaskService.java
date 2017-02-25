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

import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;

/**
 * This {@link TaskService} implementation provides no persistence.
 *
 * @author Václav Brodec
 * @author Josef Janoušek
 *
 */
public final class MemoryOnlyTaskService implements TaskService {

  private final FileService fileService;

  /**
   * Table of tasks where rows are indexed by user IDs and the columns by task IDs.
   */
  private final Table<String, String, Task> tasks;

  /**
   * Creates the task service with no registered tasks.
   */
  @Autowired
  public MemoryOnlyTaskService(final FileService fileService) {
    this(fileService, HashBasedTable.create());
  }

  private MemoryOnlyTaskService(final FileService fileService,
      final Table<String, String, Task> tasks) {
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(tasks);

    this.fileService = fileService;
    this.tasks = tasks;
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

  @Override
  public void deleteAll(final String userId) {
    Preconditions.checkNotNull(userId);

    final Map<String, Task> taskIdsToTasks = this.tasks.row(userId);
    taskIdsToTasks.entrySet().stream().forEach(e -> this.fileService
        .unsubscribe(e.getValue().getConfiguration().getInput(), e.getValue()));
    taskIdsToTasks.clear();
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#deleteById(java.lang.String)
   */
  @Override
  public void deleteById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);

    final Task task = this.tasks.remove(userId, taskId);
    Preconditions.checkArgument(task != null);

    final Configuration configuration = task.getConfiguration();
    this.fileService.unsubscribe(configuration.getInput(), task);
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#getById(java.lang.String)
   */
  @Override
  public Task getById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);

    final Task task = this.tasks.get(userId, taskId);
    Preconditions.checkArgument(task != null);

    return task;
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#getTasks()
   */
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

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#replace(cz.cuni.mff.xrg.odalic.tasks.Task)
   */
  @Override
  public void replace(final Task task) {
    Preconditions.checkNotNull(task);

    final Task previous = this.tasks.put(task.getOwner().getEmail(), task.getId(), task);
    if (previous != null) {
      final Configuration previousConfiguration = previous.getConfiguration();
      final File previousInput = previousConfiguration.getInput();

      this.fileService.unsubscribe(previousInput, previous);
    }

    final Configuration configuration = task.getConfiguration();
    final File input = configuration.getInput();
    this.fileService.subscribe(input, task);
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#verifyTaskExistenceById(java.lang.String)
   */
  @Override
  @Nullable
  public Task verifyTaskExistenceById(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);

    return this.tasks.get(userId, taskId);
  }
}
