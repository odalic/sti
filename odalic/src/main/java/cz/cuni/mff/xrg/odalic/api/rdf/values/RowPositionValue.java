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
  
  /**
   * @param adaptee
   */
  public RowPositionValue(RowPosition adaptee) {
    this.index = adaptee.getIndex();
  }

  /**
   * @return the index
   */
  public int getIndex() {
    return index;
  }

  /**
   * @param index the index to set
   */
  public void setIndex(int index) {
    Preconditions.checkArgument(index >= 0);
    
    this.index = index;
  }
  
  public RowPosition toRowPosition() {
    return new RowPosition(index);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "RowPositionValue [index=" + index + "]";
  }
}
