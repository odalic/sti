package cz.cuni.mff.xrg.odalic.tasks.feedbacks;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;

/**
 * Feedback service handles the setting of {@link Feedback} for tasks.
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
}
