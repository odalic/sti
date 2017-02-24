package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.DataCubeComponent;

/**
 * Domain class {@link DataCubeComponent} adapted for RDF serialization.
 * 
 * @author Josef Janoušek
 *
 */
@RdfsClass("http://odalic.eu/internal/DataCubeComponent")
public class DataCubeComponentValue implements Serializable {

  private static final long serialVersionUID = 6470286409364911894L;

  private ColumnPositionValue position;

  private StatisticalAnnotationValue annotation;

  public DataCubeComponentValue() {}
  
  public DataCubeComponentValue(DataCubeComponent adaptee) {
    this.position = new ColumnPositionValue(adaptee.getPosition());
    this.annotation = new StatisticalAnnotationValue(adaptee.getAnnotation());
  }

  /**
   * @return the position
   */
  @RdfProperty("http://odalic.eu/internal/DataCubeComponent/position")
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

  /**
   * @return the annotation
   */
  @RdfProperty("http://odalic.eu/internal/DataCubeComponent/annotation")
  @Nullable
  public StatisticalAnnotationValue getAnnotation() {
    return annotation;
  }

  /**
   * @param annotation the annotation to set
   */
  public void setAnnotation(StatisticalAnnotationValue annotation) {
    Preconditions.checkNotNull(annotation);
    
    this.annotation = annotation;
  }
  
  public DataCubeComponent toDataCubeComponent() {
    return new DataCubeComponent(position.toColumnPosition(), annotation.toStatisticalAnnotation());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "DataCubeComponentValue [position=" + position + ", annotation=" + annotation + "]";
  }
}
