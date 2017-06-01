package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.AdvancedBaseTypeValue;
import cz.cuni.mff.xrg.odalic.bases.AdvancedBaseType;


public final class AdvancedBaseTypeAdapter extends XmlAdapter<AdvancedBaseTypeValue, AdvancedBaseType> {

  @Override
  public AdvancedBaseTypeValue marshal(final AdvancedBaseType bound) throws Exception {
    return new AdvancedBaseTypeValue(bound);
  }

  @Override
  public AdvancedBaseType unmarshal(final AdvancedBaseTypeValue value) throws Exception {
    return new AdvancedBaseType(value.getName(), value.getKeys(), value.getKeysToDefaultValues(), value.getKeysToComments());
  }
}
