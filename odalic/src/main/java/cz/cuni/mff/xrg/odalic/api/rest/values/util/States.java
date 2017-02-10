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

  private States() {}

  /**
   * Queries the execution service and derives the correct {@link StateValue}.
   * 
   * @param executionService task execution service
   * @param task task
   * @return state value
   */
  public static StateValue queryStateValue(ExecutionService executionService, Task task) {
    Preconditions.checkNotNull(executionService);
    Preconditions.checkNotNull(task);
    
    final String userId = task.getOwner().getEmail();
    final String taskId = task.getId();
    
    return queryStateValue(executionService, userId, taskId);
  }
  
  /**
   * Queries the execution service and derives the correct {@link StateValue}.
   * 
   * @param executionService task execution service
   * @param userId user ID
   * @param taskId task ID
   * @return state value
   */
  public static StateValue queryStateValue(ExecutionService executionService, String userId, String taskId) {
    Preconditions.checkNotNull(executionService);
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(taskId);
    
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

}
