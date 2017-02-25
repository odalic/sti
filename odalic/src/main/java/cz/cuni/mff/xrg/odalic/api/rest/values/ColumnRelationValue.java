package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.ColumnRelation;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;

/**
 * Domain class {@link ColumnRelation} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "columnRelation")
public final class ColumnRelationValue implements Serializable {

  private static final long serialVersionUID = -9087389821835847372L;

  private ColumnRelationPosition position;

  private ColumnRelationAnnotation annotation;

  public ColumnRelationValue() {}

  public ColumnRelationValue(final ColumnRelation adaptee) {
    this.position = adaptee.getPosition();
    this.annotation = adaptee.getAnnotation();
  }

  /**
   * @return the annotation
   */
  @XmlElement
  @Nullable
  public ColumnRelationAnnotation getAnnotation() {
    return this.annotation;
  }

  /**
   * @return the position
   */
  @XmlElement
  @Nullable
  public ColumnRelationPosition getPosition() {
    return this.position;
  }

  /**
   * @param annotation the annotation to set
   */
  public void setAnnotation(final ColumnRelationAnnotation annotation) {
    Preconditions.checkNotNull(annotation);

    this.annotation = annotation;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(final ColumnRelationPosition position) {
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
    return "ColumnRelationValue [position=" + this.position + ", annotation=" + this.annotation
        + "]";
  }
}
