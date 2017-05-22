package cz.cuni.mff.xrg.odalic.api.rdf.values;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Domain class {@link ColumnPosition} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
public final class ColumnPositionValue implements Comparable<ColumnPositionValue> {

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
  public int compareTo(final ColumnPositionValue o) {
    return Integer.compare(this.index, o.index);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + index;
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ColumnPositionValue other = (ColumnPositionValue) obj;
    if (index != other.index) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ColumnPositionValue [index=" + this.index + "]";
  }
}
