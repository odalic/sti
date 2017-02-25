package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Domain class {@link ColumnPosition} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "columnPosition")
public final class ColumnPositionValue {

  private int index;

  public ColumnPositionValue() {}

  public ColumnPositionValue(final ColumnPosition adaptee) {
    this.index = adaptee.getIndex();
  }

  /**
   * @return the index
   */
  @XmlElement
  public int getIndex() {
    return this.index;
  }

  /**
   * @param index the index to set
   */
  public void setIndex(final int index) {
    Preconditions.checkArgument(index >= 0);

    this.index = index;
  }

  @Override
  public String toString() {
    return "ColumnPositionValue [index=" + this.index + "]";
  }
}
