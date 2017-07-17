package cz.cuni.mff.xrg.odalic.positions;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.CellPositionAdapter;

/**
 * Position of a common cell in a table. Headers do not count.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(CellPositionAdapter.class)
public final class CellPosition implements Serializable {

  private static final long serialVersionUID = 7955615617737637528L;

  private final RowPosition rowPosition;

  private final ColumnPosition columnPosition;


  /**
   * Creates new representation of cell position in a table.
   *
   * @param rowIndex row index
   * @param columnIndex column index
   */
  public CellPosition(final int rowIndex, final int columnIndex) {
    this(new RowPosition(rowIndex), new ColumnPosition(columnIndex));
  }

  /**
   * Creates new representation of cell position in a table.
   *
   * @param rowPosition row position
   * @param columnPosition column position
   */
  public CellPosition(final RowPosition rowPosition, final ColumnPosition columnPosition) {
    Preconditions.checkNotNull(rowPosition, "The rowPosition cannot be null!");
    Preconditions.checkNotNull(columnPosition, "The columnPosition cannot be null!");

    this.rowPosition = rowPosition;
    this.columnPosition = columnPosition;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CellPosition other = (CellPosition) obj;
    if (this.columnPosition == null) {
      if (other.columnPosition != null) {
        return false;
      }
    } else if (!this.columnPosition.equals(other.columnPosition)) {
      return false;
    }
    if (this.rowPosition == null) {
      if (other.rowPosition != null) {
        return false;
      }
    } else if (!this.rowPosition.equals(other.rowPosition)) {
      return false;
    }
    return true;
  }

  /**
   * @return the column index
   */
  public int getColumnIndex() {
    return this.columnPosition.getIndex();
  }

  /**
   * @return the column position
   */
  public ColumnPosition getColumnPosition() {
    return this.columnPosition;
  }

  /**
   * @return the row index
   */
  public int getRowIndex() {
    return this.rowPosition.getIndex();
  }

  /**
   * @return the row position
   */
  public RowPosition getRowPosition() {
    return this.rowPosition;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        (prime * result) + ((this.columnPosition == null) ? 0 : this.columnPosition.hashCode());
    result = (prime * result) + ((this.rowPosition == null) ? 0 : this.rowPosition.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "" + this.rowPosition + this.columnPosition;
  }
}
