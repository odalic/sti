package cz.cuni.mff.xrg.odalic.api.rdf.values;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.RowPosition;

/**
 * Domain class {@link RowPosition} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */

public final class RowPositionValue {

  private int index;

  public RowPositionValue() {}

  public RowPositionValue(final RowPosition adaptee) {
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
    Preconditions.checkArgument(index >= 0, "Row position index must be nonnegative!");

    this.index = index;
  }

  public RowPosition toRowPosition() {
    return new RowPosition(this.index);
  }

  @Override
  public String toString() {
    return "RowPositionValue [index=" + this.index + "]";
  }
}
