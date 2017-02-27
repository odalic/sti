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

  void create(Task task);

  /**
   * Deletes all tasks belonging to the user.
   *
   * @param userId user ID
   */
  void deleteAll(String userId);

  void deleteById(String userId, String taskId);

  Task getById(String userId, String taskId);

  Set<Task> getTasks(String userId);

  NavigableSet<Task> getTasksSortedByCreatedInDescendingOrder(String userId);

  NavigableSet<Task> getTasksSortedByIdInAscendingOrder(String userId);

  void replace(Task task);

  Task verifyTaskExistenceById(String userId, String taskId);
}
