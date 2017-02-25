package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.RowPosition;

/**
 * Domain class {@link CellPosition} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "cellPosition")
public class CellPositionValue {

  private RowPosition rowPosition;

  private ColumnPosition columnPosition;

  public CellPositionValue() {}

  public CellPositionValue(final CellPosition adaptee) {
    this.columnPosition = adaptee.getColumnPosition();
    this.rowPosition = adaptee.getRowPosition();
  }

  /**
   * @return the columnPosition
   */
  @XmlElement
  @Nullable
  public ColumnPosition getColumnPosition() {
    return this.columnPosition;
  }

  /**
   * @return the rowPosition
   */
  @XmlElement
  @Nullable
  public RowPosition getRowPosition() {
    return this.rowPosition;
  }

  /**
   * @param columnPosition the columnPosition to set
   */
  public void setColumnPosition(final ColumnPosition columnPosition) {
    Preconditions.checkNotNull(columnPosition);

    this.columnPosition = columnPosition;
  }

  /**
   * @param rowPosition the rowPosition to set
   */
  public void setRowPosition(final RowPosition rowPosition) {
    Preconditions.checkNotNull(rowPosition);

    this.rowPosition = rowPosition;
  }

  @Override
  public String toString() {
    return "CellPositionValue [rowPosition=" + this.rowPosition + ", columnPosition="
        + this.columnPosition + "]";
  }
}
