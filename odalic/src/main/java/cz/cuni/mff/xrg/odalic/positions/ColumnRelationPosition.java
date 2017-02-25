package cz.cuni.mff.xrg.odalic.positions;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ColumnRelationPositionAdapter;

/**
 * Position of columns in a relation.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(ColumnRelationPositionAdapter.class)
public final class ColumnRelationPosition implements Serializable {

  private static final long serialVersionUID = -730386528524703424L;

  private final ColumnPosition first;

  private final ColumnPosition second;

  /**
   * Creates new representation of a position of columns in relation.
   *
   * @param first first column position
   * @param second second column position
   */
  public ColumnRelationPosition(final ColumnPosition first, final ColumnPosition second) {
    Preconditions.checkNotNull(first);
    Preconditions.checkNotNull(second);
    Preconditions.checkArgument(first.getIndex() != second.getIndex());

    this.first = first;
    this.second = second;
  }

  /**
   * Creates new representation of a position of columns in relation.
   *
   * @param firstIndex first column index
   * @param second second column index
   */
  public ColumnRelationPosition(final int firstIndex, final int secondIndex) {
    this(new ColumnPosition(firstIndex), new ColumnPosition(secondIndex));
  }

  /**
   * Compares for equivalence (only other column relation position with the same first and second
   * column position passes).
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
    final ColumnRelationPosition other = (ColumnRelationPosition) obj;
    if (this.first == null) {
      if (other.first != null) {
        return false;
      }
    } else if (!this.first.equals(other.first)) {
      return false;
    }
    if (this.second == null) {
      if (other.second != null) {
        return false;
      }
    } else if (!this.second.equals(other.second)) {
      return false;
    }
    return true;
  }

  /**
   * @return the first column position
   */
  public ColumnPosition getFirst() {
    return this.first;
  }

  /**
   * @return the first column index
   */
  public int getFirstIndex() {
    return this.first.getIndex();
  }

  /**
   * @return the second column position
   */
  public ColumnPosition getSecond() {
    return this.second;
  }

  /**
   * @return the second column index
   */
  public int getSecondIndex() {
    return this.second.getIndex();
  }

  /**
   * Computes hash code based on the first and second column position.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.first == null) ? 0 : this.first.hashCode());
    result = (prime * result) + ((this.second == null) ? 0 : this.second.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "ColumnRelationPosition [first=" + this.first + ", second=" + this.second + "]";
  }
}
