package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.UserValue;
import cz.cuni.mff.xrg.odalic.users.User;


public final class UserAdapter extends XmlAdapter<UserValue, User> {

  @Override
  public UserValue marshal(final User bound) throws Exception {
    return new UserValue(bound);
  }

  @Override
  public User unmarshal(final UserValue value) throws Exception {
    throw new UnsupportedOperationException();
  }
}
