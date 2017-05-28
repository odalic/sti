package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;

/**
 * Domain class {@link ColumnIgnore} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
public final class ColumnIgnoreValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private ColumnPositionValue position;

  public ColumnIgnoreValue() {}

  public ColumnIgnoreValue(final ColumnIgnore adaptee) {
    this.position = new ColumnPositionValue(adaptee.getPosition());
  }

  /**
   * @return the position
   */
  @Nullable
  public ColumnPositionValue getPosition() {
    return this.position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(final ColumnPositionValue position) {
    Preconditions.checkNotNull(position, "The position cannot be null!");

    this.position = position;
  }

  public ColumnIgnore toColumnIgnore() {
    return new ColumnIgnore(getPosition().toColumnPosition());
  }

  @Override
  public String toString() {
    return "ColumnIgnoreValue [position=" + this.position + "]";
  }
}
