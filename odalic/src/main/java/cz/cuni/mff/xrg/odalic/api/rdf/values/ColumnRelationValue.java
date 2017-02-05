package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnRelation;

/**
 * Domain class {@link ColumnRelation} adapted for RDF serialization.
 * 
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/ColumnRelation")
public final class ColumnRelationValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private ColumnRelationPositionValue position;
  
  private ColumnRelationAnnotationValue annotation;
  
  public ColumnRelationValue() {}
  
  public ColumnRelationValue(ColumnRelation adaptee) {
    this.position = new ColumnRelationPositionValue(adaptee.getPosition());
    this.annotation = new ColumnRelationAnnotationValue(adaptee.getAnnotation());
  }

  /**
   * @return the position
   */
  @RdfProperty("http://odalic.eu/internal/ColumnRelation/Position")
  @Nullable
  public ColumnRelationPositionValue getPosition() {
    return position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(ColumnRelationPositionValue position) {
    Preconditions.checkNotNull(position);
    
    this.position = position;
  }

  /**
   * @return the annotation
   */
  @RdfProperty("http://odalic.eu/internal/ColumnRelation/Annotation")
  @Nullable
  public ColumnRelationAnnotationValue getAnnotation() {
    return annotation;
  }

  /**
   * @param annotation the annotation to set
   */
  public void setAnnotation(ColumnRelationAnnotationValue annotation) {
    Preconditions.checkNotNull(annotation);
    
    this.annotation = annotation;
  }

  public ColumnRelation toColumnRelation() {
    return new ColumnRelation(position.toColumnRelationPosition(), annotation.toColumnRelationAnnotation());
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnRelationValue [position=" + position + ", annotation=" + annotation + "]";
  }
}
