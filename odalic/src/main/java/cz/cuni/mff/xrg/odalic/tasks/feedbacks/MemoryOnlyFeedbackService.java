package cz.cuni.mff.xrg.odalic.tasks.feedbacks;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;
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

  private final ConfigurationService configurationService;

  private final Table<String, String, Input> inputSnapshots;

  private MemoryOnlyFeedbackService(final ConfigurationService configurationService,
      final Table<String, String, Input> inputSnapshots) {
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(inputSnapshots);

    this.configurationService = configurationService;
    this.inputSnapshots = inputSnapshots;
  }

  @Autowired
  public MemoryOnlyFeedbackService(final ConfigurationService configurationService,
      final TaskService taskService) {
    this(configurationService, HashBasedTable.create());
  }

  @Override
  public Feedback getForTaskId(final String userId, final String taskId) {
    final Configuration configuration = this.configurationService.getForTaskId(userId, taskId);

    return configuration.getFeedback();
  }

  @Override
  public Input getInputSnapshotForTaskId(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);

    final Input inputSnapshot = this.inputSnapshots.get(userId, taskId);
    Preconditions.checkArgument(inputSnapshot != null, "No such task input snapshot present!");

    return inputSnapshot;
  }

  @Override
  public void setForTaskId(final String userId, final String taskId, final Feedback feedback) {
    final Configuration oldConfiguration = this.configurationService.getForTaskId(userId, taskId);
    this.configurationService.setForTaskId(userId, taskId,
        new Configuration(oldConfiguration.getInput(), oldConfiguration.getUsedBases(),
            oldConfiguration.getPrimaryBase(), feedback, oldConfiguration.getRowsLimit(),
            oldConfiguration.isStatistical()));
  }

  @Override
  public void setInputSnapshotForTaskid(final String userId, final String taskId,
      final Input inputSnapshot) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);
    Preconditions.checkNotNull(inputSnapshot);

    this.inputSnapshots.put(userId, taskId, inputSnapshot);
  }
}
