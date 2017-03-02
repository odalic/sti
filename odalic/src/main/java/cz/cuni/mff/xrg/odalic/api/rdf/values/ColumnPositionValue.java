package cz.cuni.mff.xrg.odalic.api.rdf.values;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Domain class {@link ColumnPosition} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
public final class ColumnPositionValue {

  private int index;

  public ColumnPositionValue() {}

  public ColumnPositionValue(final ColumnPosition adaptee) {
    this.index = adaptee.getIndex();
  }

  /**
   * @return the index
   */
  public int getIndex() {
    return this.index;
  }

  /**
   * @param index the index to set
   */
  public void setIndex(final int index) {
    Preconditions.checkArgument(index >= 0);

    this.index = index;
  }

  public ColumnPosition toColumnPosition() {
    return new ColumnPosition(this.index);
  }

  @Override
  public String toString() {
    return "ColumnPositionValue [index=" + this.index + "]";
  }
}
