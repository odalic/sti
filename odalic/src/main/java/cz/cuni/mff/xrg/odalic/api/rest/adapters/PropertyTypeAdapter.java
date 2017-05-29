package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.PropertyTypeValue;
import cz.cuni.mff.xrg.odalic.entities.PropertyType;

public final class PropertyTypeAdapter extends XmlAdapter<PropertyTypeValue, PropertyType> {

  @Override
  public PropertyTypeValue marshal(final PropertyType bound) throws Exception {
    return Enum.valueOf(PropertyTypeValue.class, bound.name());
  }

  @Override
  public PropertyType unmarshal(final PropertyTypeValue value) throws Exception {
    return Enum.valueOf(PropertyType.class, value.name());
  }
}
