package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.DataCubeComponent;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;

/**
 * Domain class {@link DataCubeComponent} adapted for REST API.
 * 
 * @author Josef Janoušek
 *
 */
@XmlRootElement(name = "dataCubeComponent")
public class DataCubeComponentValue implements Serializable {

  private static final long serialVersionUID = 6470286409364911894L;

  private ColumnPosition position;

  private StatisticalAnnotation annotation;

  public DataCubeComponentValue() {}
  
  public DataCubeComponentValue(DataCubeComponent adaptee) {
    this.position = adaptee.getPosition();
    this.annotation = adaptee.getAnnotation();
  }

  /**
   * @return the position
   */
  @XmlElement
  @Nullable
  public ColumnPosition getPosition() {
    return position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(ColumnPosition position) {
    Preconditions.checkNotNull(position);
    
    this.position = position;
  }

  /**
   * @return the annotation
   */
  @XmlElement
  @Nullable
  public StatisticalAnnotation getAnnotation() {
    return annotation;
  }

  /**
   * @param annotation the annotation to set
   */
  public void setAnnotation(StatisticalAnnotation annotation) {
    Preconditions.checkNotNull(annotation);
    
    this.annotation = annotation;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "DataCubeComponentValue [position=" + position + ", annotation=" + annotation + "]";
  }
}