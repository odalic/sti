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
  
  public ColumnIgnoreValue(ColumnIgnore adaptee) {
    this.position = new ColumnPositionValue(adaptee.getPosition());
  }

  /**
   * @return the position
   */
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
  
  public ColumnIgnore toColumnIgnore() {
    return new ColumnIgnore(getPosition().toColumnPosition());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnIgnoreValue [position=" + position + "]";
  }  
}
