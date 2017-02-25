package cz.cuni.mff.xrg.odalic.positions;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ColumnPositionAdapter;

/**
 * Position of column in a table.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(ColumnPositionAdapter.class)
public final class ColumnPosition implements Serializable {

  private static final long serialVersionUID = -1179554576389130985L;

  private final int index;

  /**
   * Creates new column position representation.
   *
   * @param index zero-based index
   */
  public ColumnPosition(final int index) {
    Preconditions.checkArgument(index >= 0);

    this.index = index;
  }


  /**
   * Compares for equality (only other column position with the same index passes).
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
    final ColumnPosition other = (ColumnPosition) obj;
    if (this.index != other.index) {
      return false;
    }
    return true;
  }

  /**
   * @return the index
   */
  public int getIndex() {
    return this.index;
  }

  /**
   * Computes hash code based on the index.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.index;
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "[" + this.index + "]";
  }
}
