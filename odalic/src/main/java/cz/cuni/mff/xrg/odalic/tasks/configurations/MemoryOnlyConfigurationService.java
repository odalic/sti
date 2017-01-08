package cz.cuni.mff.xrg.odalic.tasks.configurations;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;

/**
 * This {@link ConfigurationService} implementation provides no persistence.
 * 
 * @author Václav Brodec
 *
 */
public final class MemoryOnlyConfigurationService implements ConfigurationService {

  private final TaskService taskService;

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService#getForTaskId(java.lang.String)
   */
  @Override
  public Configuration getForTaskId(String taskId) {
    final Task task = taskService.getById(taskId);

    return task.getConfiguration();
  }

  /**
   * @param taskId
   * @param configuration
   */
  @Override
  public void setForTaskId(final String taskId, final Configuration configuration) {
    final Task task = taskService.getById(taskId);

    taskService
        .replace(new Task(task.getId(), task.getDescription(), task.getCreated(), configuration));
  }

  @Autowired
  public MemoryOnlyConfigurationService(final TaskService taskService) {
    Preconditions.checkNotNull(taskService);

    this.taskService = taskService;
  }
}
