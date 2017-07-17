package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnCompulsoryValue;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnCompulsory;

public final class ColumnCompulsoryAdapter extends XmlAdapter<ColumnCompulsoryValue, ColumnCompulsory> {

  @Override
  public ColumnCompulsoryValue marshal(final ColumnCompulsory bound) throws Exception {
    return new ColumnCompulsoryValue(bound);
  }

  @Override
  public ColumnCompulsory unmarshal(final ColumnCompulsoryValue value) throws Exception {
    return new ColumnCompulsory(value.getPosition());
  }

}
