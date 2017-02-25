package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnPositionValue;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;


public final class ColumnPositionAdapter extends XmlAdapter<ColumnPositionValue, ColumnPosition> {

  @Override
  public ColumnPositionValue marshal(final ColumnPosition bound) throws Exception {
    return new ColumnPositionValue(bound);
  }

  @Override
  public ColumnPosition unmarshal(final ColumnPositionValue value) throws Exception {
    return new ColumnPosition(value.getIndex());
  }
}
