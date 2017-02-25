package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.CredentialsValue;
import cz.cuni.mff.xrg.odalic.users.Credentials;


public final class CredentialsAdapter extends XmlAdapter<CredentialsValue, Credentials> {

  @Override
  public CredentialsValue marshal(final Credentials bound) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public Credentials unmarshal(final CredentialsValue value) throws Exception {
    return new Credentials(value.getEmail(), value.getPassword());
  }
}
