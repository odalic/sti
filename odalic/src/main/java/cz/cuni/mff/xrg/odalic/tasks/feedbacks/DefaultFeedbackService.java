package cz.cuni.mff.xrg.odalic.tasks.feedbacks;

import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;

/**
 * This {@link FeedbackService} implementation provides no persistence.
 *
 * @author Václav Brodec
 *
 */
public final class DefaultFeedbackService implements FeedbackService {

  private final ConfigurationService configurationService;
  private final ExecutionService executionService;

  private DefaultFeedbackService(final ConfigurationService configurationService,
      final ExecutionService executionService) {
    Preconditions.checkNotNull(configurationService, "The configurationService cannot be null!");
    Preconditions.checkNotNull(executionService, "The executionService cannot be null!");

    this.configurationService = configurationService;
    this.executionService = executionService;
  }

  @Override
  public Feedback getForTaskId(final String userId, final String taskId) {
    final Configuration configuration = this.configurationService.getForTaskId(userId, taskId);

    return configuration.getFeedback();
  }

  @Override
  public void setForTaskId(final String userId, final String taskId, final Feedback feedback) {
    final Configuration oldConfiguration = this.configurationService.getForTaskId(userId, taskId);
    this.configurationService.setForTaskId(userId, taskId,
        new Configuration(oldConfiguration.getInput(), oldConfiguration.getUsedBases(),
            oldConfiguration.getPrimaryBase(), feedback, oldConfiguration.getRowsLimit(),
            oldConfiguration.isStatistical()));
    this.executionService.mergeWithResultForTaskId(userId, taskId, null); //TODO: Merge result;
  }
}
