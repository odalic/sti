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

  public CellPositionValue(final CellPosition adaptee) {
    this.columnPosition = new ColumnPositionValue(adaptee.getColumnPosition());
    this.rowPosition = new RowPositionValue(adaptee.getRowPosition());
  }

  /**
   * @return the column position
   */
  @Nullable
  public ColumnPositionValue getColumnPosition() {
    return this.columnPosition;
  }

  /**
   * @return the row position
   */
  @Nullable
  public RowPositionValue getRowPosition() {
    return this.rowPosition;
  }

  /**
   * @param columnPosition the column position to set
   */
  public void setColumnPosition(final ColumnPositionValue columnPosition) {
    Preconditions.checkNotNull(columnPosition, "The columnPosition cannot be null!");

    this.columnPosition = columnPosition;
  }

  /**
   * @param rowPosition the row position to set
   */
  public void setRowPosition(final RowPositionValue rowPosition) {
    Preconditions.checkNotNull(rowPosition, "The rowPosition cannot be null!");

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
