package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Manages the {@link Task} execution.
 *
 * @author Václav Brodec
 *
 */
/**
 * @author brodecva
 *
 */
public interface ExecutionService {
  /**
   * Attempts to cancel execution of the task.
   *
   * @param userId user ID
   * @param taskId task ID
   * @throws IllegalStateException if the task has already finished
   */
  void cancelForTaskId(String userId, String taskId) throws IllegalStateException;

  /**
   * Gets result of the task. Blocks until the result is available or the execution canceled.
   *
   * @param userId user ID
   * @param taskId task ID
   * @return annotations for the task input
   * @throws IllegalArgumentException when the task has not been scheduled
   * @throws InterruptedException if the execution was interrupted while waiting
   * @throws ExecutionException if the computation threw an exception
   * @throws CancellationException if the computation was cancelled
   */
  Result getResultForTaskId(String userId, String taskId) throws IllegalArgumentException,
      InterruptedException, ExecutionException, CancellationException;

  /**
   * Indicates the state of scheduling.
   *
   * @param userId user ID
   * @param taskId task ID
   *
   * @return true, if the execution has been scheduled for the task, false otherwise
   */
  boolean hasBeenScheduledForTaskId(String userId, String taskId);

  /**
   * Indicates whether the task has finished with a non-fatal problem.
   * 
   * @param userId user ID
   * @param taskId task ID
   *
   * @return true if warned, false otherwise
   */
  boolean hasBeenWarnedForTaskId(String userId, String taskId);

  /**
   * Indicates whether the task has failed.
   * 
   * @param userId user ID
   * @param taskId task ID
   *
   * @return true if failed, false otherwise
   */
  boolean hasFailedForTaskId(String userId, String taskId);

  /**
   * Indicates whether the task was voluntarily canceled.
   *
   * @param userId user ID
   * @param taskId task ID
   *
   * @return true if canceled, false otherwise
   */
  boolean isCanceledForTaskId(String userId, String taskId);

  /**
   * Indicates whether the task is done.
   *
   * @param userId user ID
   * @param taskId task ID
   *
   * @return true if done, false otherwise
   *
   */
  boolean isDoneForTaskId(String userId, String taskId);

  /**
   * Indicates whether the task has finished without a problem.
   *
   * @param userId user ID
   * @param taskId task ID
   *
   * @return true if successful, false otherwise
   */
  boolean isSuccessForTasksId(String userId, String taskId);

  /**
   * Submits execution of the task.
   *
   * @param userId user ID
   * @param taskId task ID
   *
   * @throws IllegalStateException if the task has already been submitted for execution
   * @throws IOException when there is a I/O during input parsing
   */
  void submitForTaskId(String userId, String taskId) throws IllegalStateException, IOException;

  /**
   * Removes all the tasks belonging to the user from the scheduled tasks.
   *
   * @param userId user ID
   */
  void unscheduleAll(String userId);

  /**
   * Removes the task from the scheduled tasks.
   *
   * @param userId user ID
   * @param taskId task ID
   */
  void unscheduleForTaskId(String userId, String taskId);

  /**
   * Replaces the computed result.
   * 
   * @param userId user ID
   * @param taskId task ID
   * @param feedback new result
   */
  void mergeWithResultForTaskId(String userId, String taskId, Feedback feedback);

  /**
   * Computes the result from the input directly, without using established tasks infrastructure.
   * 
   * @param userId user ID
   * @param usedBaseNames used bases
   * @param primaryBase preferred base
   * @param input parsed input
   * @param isStatistical true for statistical processing
   * @param feedback feedback
   * @return annotation result
   * @throws IllegalArgumentException when the task has not been scheduled
   * @throws InterruptedException if the execution was interrupted while waiting
   * @throws ExecutionException if the computation threw an exception
   * @throws CancellationException if the computation was cancelled 
   */
  Result compute(String userId, Set<? extends String> usedBaseNames,
      String primaryBase, Input input, boolean isStatistical, Feedback feedback) throws InterruptedException, ExecutionException;
}
