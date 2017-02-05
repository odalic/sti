package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnAmbiguity;

/**
 * Domain class {@link ColumnAmbiguity} adapted for RDF serialization.
 * 
 * @author Václav Brodec
 *
 */
public final class ColumnAmbiguityValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private ColumnPositionValue position;
  
  public ColumnAmbiguityValue() {}
  
  public ColumnAmbiguityValue(ColumnAmbiguity adaptee) {
    this.position = new ColumnPositionValue(adaptee.getPosition());
  }

  /**
   * @return the position
   */
  @XmlElement
  @Nullable
  public ColumnPositionValue getPosition() {
    return position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(ColumnPositionValue position) {
    Preconditions.checkNotNull(position);
    
    this.position = position;
  }

  public ColumnAmbiguity toColumnAmbiguity() {
    return new ColumnAmbiguity(position.toColumnPosition());
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnAmbiguityValue [position=" + position + "]";
  }
}
