package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.RowPositionValue;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;


public final class RowPositionAdapter extends XmlAdapter<RowPositionValue, RowPosition> {

  @Override
  public RowPositionValue marshal(final RowPosition bound) throws Exception {
    return new RowPositionValue(bound);
  }

  @Override
  public RowPosition unmarshal(final RowPositionValue value) throws Exception {
    return new RowPosition(value.getIndex());
  }
}
