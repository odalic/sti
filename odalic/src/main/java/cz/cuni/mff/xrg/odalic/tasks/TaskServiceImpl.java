package cz.cuni.mff.xrg.odalic.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * @author Václav Brodec
 *
 */
public final class TaskServiceImpl implements TaskService {

  private final Map<String, TaskDigest> tasksDigests = new HashMap<>();
  
  private final Map<String, Task> tasks = new HashMap<>();
  
  public TaskServiceImpl() {}
  
  public List<TaskDigest> getTasks() {
    return ImmutableList.copyOf(this.tasksDigests.values());
  }

  public Task getById(String id) {
    Preconditions.checkNotNull(id);
    
    Task task = this.tasks.get(id);
    if (task == null) {
      throw new IllegalArgumentException();
    }
    
    return task;
  }

  public boolean hasId(Task task, String id) {
    Preconditions.checkNotNull(id);
    
    if (task.getId() == null) {
      return false;
    }
    
    return task.getId().equals(id);
  }

  public void deleteById(String id) {
    Preconditions.checkNotNull(id);
    
    Task task = this.tasks.remove(id);
    if (task == null) {
      throw new IllegalArgumentException();
    }
  }

  public Task verifyTaskExistenceById(String id) {
    Preconditions.checkNotNull(id);
    
    if(this.tasks.containsKey(id)) {
      return this.tasks.get(id);
    }
    else {
      return null;
    }
  }

  public void create(Task task) {
    if (verifyTaskExistenceById(task.getId()) != null) {
      throw new IllegalArgumentException();
    }
    
    replace(task);
  }

  public void replace(Task task) {
    this.tasks.put(task.getId(), task);
  }
}
