package cz.cuni.mff.xrg.odalic.tasks;

import java.util.NavigableSet;
import java.util.Set;

/**
 * Task service handles the CRUD operations for {@link Task} instances.
 * 
 * @author Václav Brodec
 *
 */
public interface TaskService {

  Set<Task> getTasks(String userId);
  
  NavigableSet<Task> getTasksSortedByIdInAscendingOrder(String userId);
  
  NavigableSet<Task> getTasksSortedByCreatedInDescendingOrder(String userId);

  Task getById(String userId, String taskId);

  void deleteById(String userId, String taskId);

  Task verifyTaskExistenceById(String userId, String taskId);

  void create(Task task);

  void replace(Task task);

  /**
   * Deletes all tasks belonging to the user.
   * 
   * @param userId user ID
   */
  void deleteAll(String userId);
}
