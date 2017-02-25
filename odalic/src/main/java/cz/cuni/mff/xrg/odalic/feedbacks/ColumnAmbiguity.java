package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ColumnAmbiguityAdapter;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Hint to leave the cells in column ambiguous.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(ColumnAmbiguityAdapter.class)
public final class ColumnAmbiguity implements Serializable {

  private static final long serialVersionUID = -6608929731300596230L;

  private final ColumnPosition position;

  /**
   * Creates a new hint to leave the cells in column ambiguous.
   *
   * @param position column position
   */
  public ColumnAmbiguity(final ColumnPosition position) {
    Preconditions.checkNotNull(position);

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
    final ColumnAmbiguity other = (ColumnAmbiguity) obj;
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
    return "ColumnAmbiguity [position=" + this.position + "]";
  }
}
