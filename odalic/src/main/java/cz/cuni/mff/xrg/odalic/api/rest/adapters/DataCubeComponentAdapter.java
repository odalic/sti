package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.DataCubeComponentValue;
import cz.cuni.mff.xrg.odalic.feedbacks.DataCubeComponent;


public final class DataCubeComponentAdapter
    extends XmlAdapter<DataCubeComponentValue, DataCubeComponent> {

  @Override
  public DataCubeComponentValue marshal(final DataCubeComponent bound) throws Exception {
    return new DataCubeComponentValue(bound);
  }

  @Override
  public DataCubeComponent unmarshal(final DataCubeComponentValue value) throws Exception {
    return new DataCubeComponent(value.getPosition(), value.getAnnotation());
  }
}
