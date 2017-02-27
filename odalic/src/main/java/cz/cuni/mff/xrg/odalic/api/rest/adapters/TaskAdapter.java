package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.TaskValue;
import cz.cuni.mff.xrg.odalic.tasks.Task;


public final class TaskAdapter extends XmlAdapter<TaskValue, Task> {

  @Override
  public TaskValue marshal(final Task bound) throws Exception {
    return new TaskValue(bound);
  }

  @Override
  public Task unmarshal(final TaskValue value) throws Exception {
    throw new UnsupportedOperationException();
  }
}
