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

  @Autowired
  public DefaultConfigurationService(final TaskService taskService) {
    Preconditions.checkNotNull(taskService);

    this.taskService = taskService;
  }

  @Override
  public Configuration getForTaskId(final String userId, final String taskId) {
    final Task task = this.taskService.getById(userId, taskId);

    return task.getConfiguration();
  }

  @Override
  public void setForTaskId(final String userId, final String taskId,
      final Configuration configuration) {
    final Task task = this.taskService.getById(userId, taskId);

    try {
      this.taskService.replace(new Task(task.getOwner(), task.getId(), task.getDescription(),
          task.getCreated(), configuration));
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e);
    }
  }
}
