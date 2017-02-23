package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.Disambiguation;

/**
 * Domain class {@link Disambiguation} adapted for RDF serialization.
 * 
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/Disambiguation")
public final class DisambiguationValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private CellPositionValue position;
  
  private CellAnnotationValue annotation;
  
  public DisambiguationValue() {}
  
  public DisambiguationValue(Disambiguation adaptee) {
    this.position = new CellPositionValue(adaptee.getPosition());
    this.annotation = new CellAnnotationValue(adaptee.getAnnotation());
  }

  /**
   * @return the position
   */
  @RdfProperty("http://odalic.eu/internal/Disambiguation/position")
  @Nullable
  public CellPositionValue getPosition() {
    return position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(CellPositionValue position) {
    Preconditions.checkNotNull(position);
    
    this.position = position;
  }

  /**
   * @return the annotation
   */
  @RdfProperty("http://odalic.eu/internal/Disambiguation/annotation")
  @Nullable
  public CellAnnotationValue getAnnotation() {
    return annotation;
  }

  /**
   * @param annotation the annotation to set
   */
  public void setAnnotation(CellAnnotationValue annotation) {
    Preconditions.checkNotNull(annotation);
    
    this.annotation = annotation;
  }
  
  public Disambiguation toDisambiguation() {
    return new Disambiguation(position.toCellPosition(), annotation.toCellAnnotation());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "DisambiguationValue [position=" + position + ", annotation=" + annotation + "]";
  }
}
