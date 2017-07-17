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

  public ColumnRelationValue(final ColumnRelation adaptee) {
    this.position = new ColumnRelationPositionValue(adaptee.getPosition());
    this.annotation = new ColumnRelationAnnotationValue(adaptee.getAnnotation());
  }

  /**
   * @return the annotation
   */
  @RdfProperty("http://odalic.eu/internal/ColumnRelation/annotation")
  @Nullable
  public ColumnRelationAnnotationValue getAnnotation() {
    return this.annotation;
  }

  /**
   * @return the position
   */
  @RdfProperty("http://odalic.eu/internal/ColumnRelation/position")
  @Nullable
  public ColumnRelationPositionValue getPosition() {
    return this.position;
  }

  /**
   * @param annotation the annotation to set
   */
  public void setAnnotation(final ColumnRelationAnnotationValue annotation) {
    Preconditions.checkNotNull(annotation, "The annotation cannot be null!");

    this.annotation = annotation;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(final ColumnRelationPositionValue position) {
    Preconditions.checkNotNull(position, "The position cannot be null!");

    this.position = position;
  }

  public ColumnRelation toColumnRelation() {
    return new ColumnRelation(this.position.toColumnRelationPosition(),
        this.annotation.toColumnRelationAnnotation());
  }

  @Override
  public String toString() {
    return "ColumnRelationValue [position=" + this.position + ", annotation=" + this.annotation
        + "]";
  }
}
