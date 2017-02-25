package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Domain class {@link ColumnIgnore} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "columnIgnore")
public final class ColumnIgnoreValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private ColumnPosition position;

  public ColumnIgnoreValue() {}

  public ColumnIgnoreValue(final ColumnIgnore adaptee) {
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnIgnoreValue [position=" + this.position + "]";
  }
}
