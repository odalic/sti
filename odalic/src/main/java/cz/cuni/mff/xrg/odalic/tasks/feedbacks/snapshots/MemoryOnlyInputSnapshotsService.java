package cz.cuni.mff.xrg.odalic.tasks.feedbacks.snapshots;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.input.Input;

/**
 * This {@link InputSnapshotsService} implementation provides no persistence.
 *
 * @author Václav Brodec
 *
 */
public final class MemoryOnlyInputSnapshotsService implements InputSnapshotsService {

  private final Table<String, String, Input> inputSnapshots;

  private MemoryOnlyInputSnapshotsService(final Table<String, String, Input> inputSnapshots) {
    Preconditions.checkNotNull(inputSnapshots, "The inputSnapshots cannot be null!");

    this.inputSnapshots = inputSnapshots;
  }

  public MemoryOnlyInputSnapshotsService() {
    this(HashBasedTable.create());
  }

  @Override
  public Input getInputSnapshotForTaskId(final String userId, final String taskId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(taskId, "The taskId cannot be null!");

    final Input inputSnapshot = this.inputSnapshots.get(userId, taskId);
    Preconditions.checkArgument(inputSnapshot != null, "No such task input snapshot present!");

    return inputSnapshot;
  }

  @Override
  public void setInputSnapshotForTaskid(final String userId, final String taskId,
      final Input inputSnapshot) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(taskId, "The taskId cannot be null!");
    Preconditions.checkNotNull(inputSnapshot, "The input snapshot cannot be null!");

    this.inputSnapshots.put(userId, taskId, inputSnapshot);
  }
}
