package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ConfigurationValue;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;


public final class ConfigurationAdapter extends XmlAdapter<ConfigurationValue, Configuration> {

  @Override
  public ConfigurationValue marshal(final Configuration bound) throws Exception {
    return new ConfigurationValue(bound);
  }

  @Override
  public Configuration unmarshal(final ConfigurationValue value) throws Exception {
    throw new UnsupportedOperationException();
  }
}
