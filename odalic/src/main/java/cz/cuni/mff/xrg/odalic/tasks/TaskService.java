package cz.cuni.mff.xrg.odalic.tasks;

import java.util.Set;

/**
 * Task service handles the CRUD operations for {@link Task} instances.
 * 
 * @author Václav Brodec
 *
 */
public interface TaskService {

  Set<Task> getTasks();

  Task getById(String id);

  void deleteById(String id);

  Task verifyTaskExistenceById(String id);

  void create(Task task);

  void replace(Task task);
}
