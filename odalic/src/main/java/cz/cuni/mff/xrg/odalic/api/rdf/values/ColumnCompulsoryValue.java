package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnCompulsory;

/**
 * Domain class {@link ColumnCompulsory} adapted for RDF serialization.
 *
 * @author Josef Janoušek
 *
 */
public final class ColumnCompulsoryValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private ColumnPositionValue position;

  public ColumnCompulsoryValue() {}

  public ColumnCompulsoryValue(final ColumnCompulsory adaptee) {
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
    Preconditions.checkNotNull(position);

    this.position = position;
  }

  public ColumnCompulsory toColumnCompulsory() {
    return new ColumnCompulsory(getPosition().toColumnPosition());
  }

  @Override
  public String toString() {
    return "ColumnCompulsoryValue [position=" + this.position + "]";
  }
}
