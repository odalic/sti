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
  public CellPositionValue(final CellPosition adaptee) {
    this.columnPosition = new ColumnPositionValue(adaptee.getColumnPosition());
    this.rowPosition = new RowPositionValue(adaptee.getRowPosition());
  }

  /**
   * @return the columnPosition
   */
  @Nullable
  public ColumnPositionValue getColumnPosition() {
    return this.columnPosition;
  }

  /**
   * @return the rowPosition
   */
  @Nullable
  public RowPositionValue getRowPosition() {
    return this.rowPosition;
  }

  /**
   * @param columnPosition the columnPosition to set
   */
  public void setColumnPosition(final ColumnPositionValue columnPosition) {
    Preconditions.checkNotNull(columnPosition);

    this.columnPosition = columnPosition;
  }

  /**
   * @param rowPosition the rowPosition to set
   */
  public void setRowPosition(final RowPositionValue rowPosition) {
    Preconditions.checkNotNull(rowPosition);

    this.rowPosition = rowPosition;
  }

  public CellPosition toCellPosition() {
    return new CellPosition(this.rowPosition.toRowPosition(),
        this.columnPosition.toColumnPosition());
  }

  @Override
  public String toString() {
    return "CellPositionValue [rowPosition=" + this.rowPosition + ", columnPosition="
        + this.columnPosition + "]";
  }
}
