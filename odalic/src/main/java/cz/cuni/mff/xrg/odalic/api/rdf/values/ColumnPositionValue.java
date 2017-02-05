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
  
  /**
   * @param adaptee
   */
  public ColumnPositionValue(ColumnPosition adaptee) {
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
  
  public ColumnPosition toColumnPosition() {
    return new ColumnPosition(index);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnPositionValue [index=" + index + "]";
  }
}
