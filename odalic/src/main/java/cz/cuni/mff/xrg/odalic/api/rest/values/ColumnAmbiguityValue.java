package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnAmbiguity;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Domain class {@link ColumnAmbiguity} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "columnAmbiguity")
public final class ColumnAmbiguityValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private ColumnPosition position;

  public ColumnAmbiguityValue() {}

  public ColumnAmbiguityValue(final ColumnAmbiguity adaptee) {
    this.position = adaptee.getPosition();
  }

  /**
   * @return the position
   */
  @XmlElement
  @Nullable
  public ColumnPosition getPosition() {
    return this.position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(final ColumnPosition position) {
    Preconditions.checkNotNull(position);

    this.position = position;
  }

  @Override
  public String toString() {
    return "ColumnAmbiguityValue [position=" + this.position + "]";
  }
}
