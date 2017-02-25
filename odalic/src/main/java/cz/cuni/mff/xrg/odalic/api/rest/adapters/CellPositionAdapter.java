package cz.cuni.mff.xrg.odalic.api.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.values.CellPositionValue;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;


public final class CellPositionAdapter extends XmlAdapter<CellPositionValue, CellPosition> {

  @Override
  public CellPositionValue marshal(final CellPosition bound) throws Exception {
    return new CellPositionValue(bound);
  }

  @Override
  public CellPosition unmarshal(final CellPositionValue value) throws Exception {
    return new CellPosition(value.getRowPosition(), value.getColumnPosition());
  }
}
