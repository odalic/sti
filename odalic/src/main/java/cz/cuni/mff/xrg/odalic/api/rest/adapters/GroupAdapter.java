package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.GroupValue;
import cz.cuni.mff.xrg.odalic.groups.Group;


public final class GroupAdapter extends XmlAdapter<GroupValue, Group> {

  @Override
  public GroupValue marshal(final Group bound) throws Exception {
    return new GroupValue(bound);
  }

  @Override
  public Group unmarshal(final GroupValue value) throws Exception {
    throw new UnsupportedOperationException();
  }
}
