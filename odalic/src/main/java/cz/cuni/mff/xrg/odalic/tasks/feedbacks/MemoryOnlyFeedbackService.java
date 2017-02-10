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

  @Autowired
  public MemoryOnlyFeedbackService(final ConfigurationService configurationService,
      final TaskService taskService) {
    this(configurationService, HashBasedTable.create());
  }

  private MemoryOnlyFeedbackService(final ConfigurationService configurationService,
      final Table<String, String, Input> inputSnapshots) {
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(inputSnapshots);

    this.configurationService = configurationService;
    this.inputSnapshots = inputSnapshots;
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

  @Override
  public Input getInputSnapshotForTaskId(String userId, String taskId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);

    final Input inputSnapshot = inputSnapshots.get(userId, taskId);
    Preconditions.checkArgument(inputSnapshot != null, "No such task input snapshot present!");

    return inputSnapshot;
  }

  @Override
  public void setInputSnapshotForTaskid(String userId, String taskId, Input inputSnapshot) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);
    Preconditions.checkNotNull(inputSnapshot);
    
    inputSnapshots.put(userId, taskId, inputSnapshot);
  }
}
