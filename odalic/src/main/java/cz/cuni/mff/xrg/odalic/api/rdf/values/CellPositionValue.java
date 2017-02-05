package cz.cuni.mff.xrg.odalic.api.rdf.values;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;

/**
 * Domain class {@link CellPosition} adapted for RDF serialization.
 * 
 * @author Václav Brodec
 *
 */
public class CellPositionValue {

  private RowPositionValue rowPosition;
  
  private ColumnPositionValue columnPosition;
  
  public CellPositionValue() {}
  
  /**
   * @param adaptee
   */
  public CellPositionValue(CellPosition adaptee) {
    this.columnPosition = new ColumnPositionValue(adaptee.getColumnPosition());
    this.rowPosition = new RowPositionValue(adaptee.getRowPosition());
  }

  /**
   * @return the rowPosition
   */
  @Nullable
  public RowPositionValue getRowPosition() {
    return rowPosition;
  }

  /**
   * @param rowPosition the rowPosition to set
   */
  public void setRowPosition(RowPositionValue rowPosition) {
    Preconditions.checkNotNull(rowPosition);
    
    this.rowPosition = rowPosition;
  }

  /**
   * @return the columnPosition
   */
  @Nullable
  public ColumnPositionValue getColumnPosition() {
    return columnPosition;
  }

  /**
   * @param columnPosition the columnPosition to set
   */
  public void setColumnPosition(ColumnPositionValue columnPosition) {
    Preconditions.checkNotNull(columnPosition);
    
    this.columnPosition = columnPosition;
  }
  
  public CellPosition toCellPosition() {
    return new CellPosition(rowPosition.toRowPosition(), columnPosition.toColumnPosition());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CellPositionValue [rowPosition=" + rowPosition + ", columnPosition=" + columnPosition
        + "]";
  }
}
