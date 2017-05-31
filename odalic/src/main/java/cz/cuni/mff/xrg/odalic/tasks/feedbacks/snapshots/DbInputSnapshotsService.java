package cz.cuni.mff.xrg.odalic.tasks.feedbacks.snapshots;

import java.util.Map;

import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.util.storage.DbService;

/**
 * This {@link InputSnapshotsService} implementation persists the snapshots in {@link DB}-backed
 * maps.
 *
 * @author Václav Brodec
 *
 */
public final class DbInputSnapshotsService implements InputSnapshotsService {

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
  public DbInputSnapshotsService(final DbService dbService) {
    Preconditions.checkNotNull(dbService, "The dbService cannot be null!");

    this.db = dbService.getDb();

    this.inputSnapshots = this.db.treeMap("inputSnapshots")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
  }

  @Override
  public Input getInputSnapshotForTaskId(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(taskId, "The taskId cannot be null!");

    final Input inputSnapshot = this.inputSnapshots.get(new Object[] {userId, taskId});
    Preconditions.checkArgument(inputSnapshot != null, "No such task input snapshot present!");

    return inputSnapshot;
  }

  @Override
  public void setInputSnapshotForTaskid(final String userId, final String taskId,
      final Input inputSnapshot) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(taskId, "The taskId cannot be null!");
    Preconditions.checkNotNull(inputSnapshot, "The inputSnapshot cannot be null!");

    this.inputSnapshots.put(new Object[] {userId, taskId}, inputSnapshot);

    this.db.commit();
  }
}
