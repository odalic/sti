package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.Classification;

/**
 * Domain class {@link Classification} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/Classification")
public final class ClassificationValue implements Serializable {

  private static final long serialVersionUID = 6470286409364911894L;

  private ColumnPositionValue position;

  private HeaderAnnotationValue annotation;

  public ClassificationValue() {}

  public ClassificationValue(final Classification adaptee) {
    this.position = new ColumnPositionValue(adaptee.getPosition());
    this.annotation = new HeaderAnnotationValue(adaptee.getAnnotation());
  }

  /**
   * @return the annotation
   */
  @RdfProperty("http://odalic.eu/internal/Classification/annotation")
  @Nullable
  public HeaderAnnotationValue getAnnotation() {
    return this.annotation;
  }

  /**
   * @return the position
   */
  @RdfProperty("http://odalic.eu/internal/Classification/position")
  @Nullable
  public ColumnPositionValue getPosition() {
    return this.position;
  }

  /**
   * @param annotation the annotation to set
   */
  public void setAnnotation(final HeaderAnnotationValue annotation) {
    Preconditions.checkNotNull(annotation);

    this.annotation = annotation;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(final ColumnPositionValue position) {
    Preconditions.checkNotNull(position);

    this.position = position;
  }

  public Classification toClassification() {
    return new Classification(this.position.toColumnPosition(),
        this.annotation.toHeaderAnnotation());
  }

  @Override
  public String toString() {
    return "ClassificationValue [position=" + this.position + ", annotation=" + this.annotation
        + "]";
  }
}
