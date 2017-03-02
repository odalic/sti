package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.AmbiguityAdapter;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;

@Immutable
@XmlJavaTypeAdapter(AmbiguityAdapter.class)
public final class Ambiguity implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private final CellPosition position;

  /**
   * Creates a new hint to keep a cell ambiguous.
   *
   * @param position position of the cell
   */
  public Ambiguity(final CellPosition position) {
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
    final Ambiguity other = (Ambiguity) obj;
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
  public CellPosition getPosition() {
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
    return "Ambiguity [position=" + this.position + "]";
  }
}
