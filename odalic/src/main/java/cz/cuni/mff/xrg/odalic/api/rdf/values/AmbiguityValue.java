package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.Ambiguity;

/**
 * Domain class {@link Ambiguity} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
public final class AmbiguityValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private CellPositionValue position;

  public AmbiguityValue() {}

  public AmbiguityValue(final Ambiguity adaptee) {
    this.position = new CellPositionValue(adaptee.getPosition());
  }

  /**
   * @return the position
   */
  @Nullable
  public CellPositionValue getPosition() {
    return this.position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(final CellPositionValue position) {
    Preconditions.checkNotNull(position);

    this.position = position;
  }

  public Ambiguity toAmbiguity() {
    return new Ambiguity(this.position.toCellPosition());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AmbiguityValue [position=" + this.position + "]";
  }
}
