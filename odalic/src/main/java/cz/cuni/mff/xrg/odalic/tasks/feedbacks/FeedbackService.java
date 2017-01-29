package cz.cuni.mff.xrg.odalic.tasks.feedbacks;

import java.io.IOException;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;

/**
 * Feedback service handles the CRUD operations for {@link Feedback} instances.
 * 
 * @author Václav Brodec
 *
 */
public interface FeedbackService {

  Feedback getForTaskId(String userId, String taskId);

  void setForTaskId(String userId, String taskId, Feedback feedback);

  /**
   * Get the task input to base the feedback on.
   * 
   * @param userId user ID
   * @param taskId task ID
   * 
   * @return input used as the basis of feedback
   * @throws IllegalArgumentException when no input for the task exists
   * @throws IOException when I/O exception occurs when reading underlying data
   */
  Input getInputForTaskId(String userId, String taskId) throws IllegalArgumentException, IOException;
}
