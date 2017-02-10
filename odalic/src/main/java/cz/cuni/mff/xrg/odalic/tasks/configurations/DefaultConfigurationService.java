package cz.cuni.mff.xrg.odalic.tasks.configurations;

import javax.ws.rs.BadRequestException;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;

/**
 * This {@link ConfigurationService} implementation just refers to {@link TaskService}.
 * 
 * @author Václav Brodec
 *
 */
public final class DefaultConfigurationService implements ConfigurationService {

  private final TaskService taskService;

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService#getForTaskId(java.lang.String)
   */
  @Override
  public Configuration getForTaskId(String userId, String taskId) {
    final Task task = taskService.getById(userId, taskId);

    return task.getConfiguration();
  }

  /**
   * @param taskId
   * @param configuration
   */
  @Override
  public void setForTaskId(String userId, final String taskId, final Configuration configuration) {
    final Task task = taskService.getById(userId, taskId);

    try {
      taskService.replace(new Task(task.getOwner(), task.getId(), task.getDescription(), task.getCreated(), configuration));
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e);
    }
  }

  @Autowired
  public DefaultConfigurationService(final TaskService taskService) {
    Preconditions.checkNotNull(taskService);

    this.taskService = taskService;
  }
}
