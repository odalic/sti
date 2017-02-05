package cz.cuni.mff.xrg.odalic.api.rdf.values;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;

/**
 * Domain class {@link ColumnRelationPosition} adapted for RDF serialization.
 * 
 * @author Václav Brodec
 *
 */
public final class ColumnRelationPositionValue {

  private ColumnPositionValue first;
  
  private ColumnPositionValue second;
  
  public ColumnRelationPositionValue() {}
  
  public ColumnRelationPositionValue(ColumnRelationPosition adaptee) {
    this.first = new ColumnPositionValue(adaptee.getFirst());
    this.second = new ColumnPositionValue(adaptee.getSecond());
  }

  /**
   * @return the first
   */
  @Nullable
  public ColumnPositionValue getFirst() {
    return first;
  }

  /**
   * @param first the first to set
   */
  public void setFirst(ColumnPositionValue first) {
    Preconditions.checkNotNull(first);
    
    this.first = first;
  }

  /**
   * @return the second
   */
  @Nullable
  public ColumnPositionValue getSecond() {
    return second;
  }

  /**
   * @param second the second to set
   */
  public void setSecond(ColumnPositionValue second) {
    Preconditions.checkNotNull(second);
    
    this.second = second;
  }
  
  public ColumnRelationPosition toColumnRelationPosition() {
    return new ColumnRelationPosition(first.toColumnPosition(), second.toColumnPosition());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnRelationPositionValue [first=" + first + ", second=" + second + "]";
  }
}
