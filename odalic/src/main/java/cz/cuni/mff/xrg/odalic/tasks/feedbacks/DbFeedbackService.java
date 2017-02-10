package cz.cuni.mff.xrg.odalic.tasks.feedbacks;

import java.util.Map;

import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;
import cz.cuni.mff.xrg.odalic.util.storage.DbService;

/**
 * This {@link FeedbackService} implementation persists the snapshots in {@link DB}-backed maps.
 * 
 * @author Václav Brodec
 *
 */
public final class DbFeedbackService implements FeedbackService {

  private final ConfigurationService configurationService;

  /**
   * Table of snapshots where rows are indexed by user IDs and the columns by task IDs (represented
   * as an array of size 2).
   */
  private final Map<Object[], Input> inputSnapshots;

  /**
   * The shared database instance.
   */
  private final DB db;

  @SuppressWarnings("unchecked")
  @Autowired
  public DbFeedbackService(final ConfigurationService configurationService,
      final DbService dbService) {
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(dbService);

    this.configurationService = configurationService;

    this.db = dbService.getDb();

    this.inputSnapshots = db.treeMap("inputSnapshots")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
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

    final Input inputSnapshot = inputSnapshots.get(new Object[] {userId, taskId});
    Preconditions.checkArgument(inputSnapshot != null, "No such task input snapshot present!");

    return inputSnapshot;
  }

  @Override
  public void setInputSnapshotForTaskid(String userId, String taskId, Input inputSnapshot) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);
    Preconditions.checkNotNull(inputSnapshot);

    inputSnapshots.put(new Object[] {userId, taskId}, inputSnapshot);

    db.commit();
  }
}
