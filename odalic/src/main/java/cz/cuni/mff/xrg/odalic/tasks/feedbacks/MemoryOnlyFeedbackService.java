package cz.cuni.mff.xrg.odalic.tasks.feedbacks;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;

/**
 * This {@link FeedbackService} implementation provides no persistence.
 * 
 * @author Václav Brodec
 *
 */
public final class MemoryOnlyFeedbackService implements FeedbackService {

  private TaskService taskService;
  private final ConfigurationService configurationService;

  @Autowired
  public MemoryOnlyFeedbackService(final ConfigurationService configurationService,
      final TaskService taskService) {
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(taskService);

    this.configurationService = configurationService;
    this.taskService = taskService;
  }

  @Override
  public Feedback getForTaskId(String userId, String taskId) {
    final Configuration configuration = configurationService.getForTaskId(userId, taskId);

    return configuration.getFeedback();
  }

  @Override
  public void setForTaskId(String userId, String taskId, Feedback feedback) {
    final Configuration oldConfiguration = configurationService.getForTaskId(userId, taskId);
    configurationService.setForTaskId(userId, taskId,
        new Configuration(oldConfiguration.getInput(), oldConfiguration.getUsedBases(),
            oldConfiguration.getPrimaryBase(), feedback, oldConfiguration.getRowsLimit(),
            oldConfiguration.isStatistical()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.tasks.feedbacks.FeedbackService#getInputForTaskId(java.lang.String)
   */
  @Override
  public Input getInputForTaskId(String userId, String taskId)
      throws IllegalArgumentException, IOException {
    final Task task = taskService.getById(userId, taskId);

    return task.getInputSnapshot();
  }
}
