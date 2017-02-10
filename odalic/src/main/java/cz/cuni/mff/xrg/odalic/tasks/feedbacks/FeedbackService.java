package cz.cuni.mff.xrg.odalic.tasks.feedbacks;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;

/**
 * Feedback service handles the setting of {@link Feedback} for tasks and provides and allows to set
 * the {@link Input} snapshots that the feedbacks relate to.
 * 
 * @author Václav Brodec
 *
 */
public interface FeedbackService {

  /**
   * Get the task feedback.
   * 
   * @param userId user ID
   * @param taskId task ID
   * @return the feedback
   */
  Feedback getForTaskId(String userId, String taskId);

  /**
   * Sets the task feedback.
   * 
   * @param userId user ID
   * @param taskId task ID
   * @param feedback feedback
   */
  void setForTaskId(String userId, String taskId, Feedback feedback);

  /**
   * Get the task input to base the feedback on.
   * 
   * @param userId user ID
   * @param taskId task ID
   * 
   * @return input used as the basis of feedback
   * @throws IllegalArgumentException when no input for the task exists
   */
  Input getInputSnapshotForTaskId(String userId, String taskId);

  /**
   * Sets the input snapshot the feedback is based on.
   * 
   * @param userId user ID
   * @param taskId task ID
   * @param inputSnapshot input as the basis of feedback
   */
  void setInputSnapshotForTaskid(String userId, String taskId, Input inputSnapshot);
}
