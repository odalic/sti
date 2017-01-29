package cz.cuni.mff.xrg.odalic.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.jena.ext.com.google.common.collect.ImmutableSortedSet;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

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

  private final Map<String, Task> tasks;

  private MemoryOnlyTaskService(final FileService fileService, final Map<String, Task> tasks) {
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(tasks);

    this.fileService = fileService;
    this.tasks = tasks;
  }

  /**
   * Creates the task service with no registered tasks.
   */
  @Autowired
  public MemoryOnlyTaskService(final FileService fileService) {
    this(fileService, new HashMap<>());
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#getTasks()
   */
  @Override
  public Set<Task> getTasks(String userId) {
    return ImmutableSet.copyOf(tasks.values());
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#getById(java.lang.String)
   */
  @Override
  public Task getById(String userId, String taskId) {
    Preconditions.checkNotNull(taskId);

    Task task = tasks.get(taskId);
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
    Preconditions.checkNotNull(taskId);

    final Task task = tasks.remove(taskId);
    Preconditions.checkArgument(task != null);
    
    final Configuration configuration = task.getConfiguration();
    fileService.unsubscribe(configuration.getInput(), task);
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#verifyTaskExistenceById(java.lang.String)
   */
  @Override
  @Nullable
  public Task verifyTaskExistenceById(String userId, String taskId) {
    Preconditions.checkNotNull(taskId);

    if (tasks.containsKey(taskId)) {
      return tasks.get(taskId);
    } else {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.TaskService#create(cz.cuni.mff.xrg.odalic.tasks.Task)
   */
  @Override
  public void create(final Task task) {
    Preconditions.checkNotNull(task);
    Preconditions.checkArgument(verifyTaskExistenceById(task.getOwner().getEmail(), task.getId()) == null);

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
    
    final Task previous = tasks.put(task.getId(), task);
    if (previous != null) {
      final Configuration previousConfiguration = previous.getConfiguration();
      final File previousInput = previousConfiguration.getInput();
      
      fileService.unsubscribe(previousInput, previous);
    }
    
    final Configuration configuration = task.getConfiguration();
    final File input = configuration.getInput();
    fileService.subscribe(input, task);
  }

  @Override
  public NavigableSet<Task> getTasksSortedByIdInAscendingOrder(String userId) {
    return ImmutableSortedSet.copyOf(
        (Task first, Task second) -> first.getId().compareTo(second.getId()), tasks.values());
  }

  @Override
  public NavigableSet<Task> getTasksSortedByCreatedInDescendingOrder(String userId) {
    return ImmutableSortedSet.copyOf(
        (Task first, Task second) -> -1 * first.getCreated().compareTo(second.getCreated()),
        tasks.values());
  }
}
