package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ColumnIgnoreAdapter;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Hint to ignore column.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(ColumnIgnoreAdapter.class)
public final class ColumnIgnore implements Serializable {

  private static final long serialVersionUID = -4305681863714969261L;

  private final ColumnPosition position;

  /**
   * Creates new hint to ignore column at given position.
   *
   * @param position position of the ignored column
   */
  public ColumnIgnore(final ColumnPosition position) {
    Preconditions.checkNotNull(position, "The position cannot be null!");

    this.position = position;
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
    final ColumnIgnore other = (ColumnIgnore) obj;
    if (this.position == null) {
      if (other.position != null) {
        return false;
      }
    } else if (!this.position.equals(other.position)) {
      return false;
    }
    return true;
  }

  /**
   * @return the position
   */
  public ColumnPosition getPosition() {
    return this.position;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.position == null) ? 0 : this.position.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "ColumnIgnore [position=" + this.position + "]";
  }
}
