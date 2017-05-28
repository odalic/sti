/**
 *
 */
package cz.cuni.mff.xrg.odalic.api.rest.values.util;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.values.StateValue;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;

/**
 * Task states utility class.
 *
 * @author Václav Brodec
 *
 */
public class States {

  /**
   * Queries the execution service and derives the correct {@link StateValue}.
   *
   * @param executionService task execution service
   * @param userId user ID
   * @param taskId task ID
   * @return state value
   */
  public static StateValue queryStateValue(final ExecutionService executionService,
      final String userId, final String taskId) {
    Preconditions.checkNotNull(executionService, "The executionService cannot be null!");
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(taskId, "The taskId cannot be null!");

    final boolean scheduled = executionService.hasBeenScheduledForTaskId(userId, taskId);
    if (!scheduled) {
      return StateValue.READY;
    }

    final boolean done = executionService.isDoneForTaskId(userId, taskId);
    final boolean canceled = executionService.isCanceledForTaskId(userId, taskId);

    if (done) {
      if (canceled) {
        return StateValue.READY;
      } else {
        if (executionService.hasFailedForTaskId(userId, taskId)) {
          return StateValue.ERROR;
        }

        if (executionService.hasBeenWarnedForTaskId(userId, taskId)) {
          return StateValue.WARNING;
        }

        return StateValue.SUCCESS;
      }
    } else {
      return StateValue.RUNNING;
    }
  }

  /**
   * Queries the execution service and derives the correct {@link StateValue}.
   *
   * @param executionService task execution service
   * @param task task
   * @return state value
   */
  public static StateValue queryStateValue(final ExecutionService executionService,
      final Task task) {
    Preconditions.checkNotNull(executionService, "The executionService cannot be null!");
    Preconditions.checkNotNull(task, "The task cannot be null!");

    final String userId = task.getOwner().getEmail();
    final String taskId = task.getId();

    return queryStateValue(executionService, userId, taskId);
  }

  private States() {}

}
