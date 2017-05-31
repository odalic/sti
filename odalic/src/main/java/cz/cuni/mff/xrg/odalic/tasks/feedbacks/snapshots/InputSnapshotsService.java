package cz.cuni.mff.xrg.odalic.tasks.feedbacks.snapshots;

import cz.cuni.mff.xrg.odalic.input.Input;

/**
 * This service allows to set the {@link Input} snapshots that feedbacks relate to.
 *
 * @author Václav Brodec
 *
 */
public interface InputSnapshotsService {

  /**
   * Get the task input to base a feedback on.
   *
   * @param userId user ID
   * @param taskId task ID
   *
   * @return input used as the basis of feedback
   * @throws IllegalArgumentException when no input for the task exists
   */
  Input getInputSnapshotForTaskId(String userId, String taskId);

  /**
   * Sets the input snapshot a feedback is based on.
   *
   * @param userId user ID
   * @param taskId task ID
   * @param inputSnapshot input as the basis of feedback
   */
  void setInputSnapshotForTaskid(String userId, String taskId, Input inputSnapshot);
}
