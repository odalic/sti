package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnCompulsory;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Domain class {@link ColumnCompulsory} adapted for REST API.
 *
 * @author Josef Janoušek
 *
 */
@XmlRootElement(name = "columnCompulsory")
public final class ColumnCompulsoryValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private ColumnPosition position;

  public ColumnCompulsoryValue() {}

  public ColumnCompulsoryValue(final ColumnCompulsory adaptee) {
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
    Preconditions.checkNotNull(position, "The position cannot be null!");

    this.position = position;
  }

  @Override
  public String toString() {
    return "ColumnCompulsoryValue [position=" + this.position + "]";
  }
}
