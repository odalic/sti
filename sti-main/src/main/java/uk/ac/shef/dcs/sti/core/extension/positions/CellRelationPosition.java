package uk.ac.shef.dcs.sti.core.extension.positions;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * Position of cells at the same row in a relation.
 *
 * @author Václav Brodec
 *
 */
@Immutable
public final class CellRelationPosition {

  private final ColumnRelationPosition columnsPosition;

  private final RowPosition rowPosition;

  /**
   * Creates new representation of position of two cells at the same row in a relation.
   *
   * @param first column position of the first cell
   * @param second column position of the second cell
   * @param rowPosition position of row that the cells share
   */
  public CellRelationPosition(final ColumnPosition first, final ColumnPosition second,
      final RowPosition rowPosition) {
    this(new ColumnRelationPosition(first, second), rowPosition);
  }

  /**
   * Creates new representation of position of two cells at the same row in a relation.
   *
   * @param columnsPosition position of columns in which the cells are located
   * @param rowPosition position of row that the cells share
   */
  public CellRelationPosition(final ColumnRelationPosition columnsPosition,
      final RowPosition rowPosition) {
    Preconditions.checkNotNull(columnsPosition);
    Preconditions.checkNotNull(rowPosition);

    this.columnsPosition = columnsPosition;
    this.rowPosition = rowPosition;
  }

  /**
   * Creates new representation of position of two cells at the same row in a relation.
   *
   * @param firstColumnIndex column index of the first cell
   * @param secondColumnIndex column index of the second cell
   * @param rowIndex index of the row that the cells share
   */
  public CellRelationPosition(final int firstColumnIndex, final int secondColumnIndex,
      final int rowIndex) {
    this(new ColumnRelationPosition(firstColumnIndex, secondColumnIndex),
        new RowPosition(rowIndex));
  }

  /**
   * Compares for equality (only other cell relation position with the same column position and row
   * position passes).
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final CellRelationPosition other = (CellRelationPosition) obj;
    if (this.columnsPosition == null) {
      if (other.columnsPosition != null) {
        return false;
      }
    } else if (!this.columnsPosition.equals(other.columnsPosition)) {
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
   * @return the columns position
   */
  public ColumnRelationPosition getColumnsPosition() {
    return this.columnsPosition;
  }

  /**
   * @return the first cell column index
   */
  public int getFirstColumnIndex() {
    return this.columnsPosition.getFirstIndex();
  }

  /**
   * @return the first cell column position
   */
  public ColumnPosition getFirstColumnPosition() {
    return this.columnsPosition.getFirst();
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

  /**
   * @return the second cell column index
   */
  public int getSecondColumnIndex() {
    return this.columnsPosition.getSecondIndex();
  }

  /**
   * @return the second cell column position
   */
  public ColumnPosition getSecondColumnPosition() {
    return this.columnsPosition.getSecond();
  }

  /**
   * Computes hash code based on the column position and the row position.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        (prime * result) + ((this.columnsPosition == null) ? 0 : this.columnsPosition.hashCode());
    result = (prime * result) + ((this.rowPosition == null) ? 0 : this.rowPosition.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CellRelationPosition [columnsPosition=" + this.columnsPosition + ", rowPosition="
        + this.rowPosition + "]";
  }
}
