package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.DisambiguationAdapter;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;

@Immutable
@XmlJavaTypeAdapter(DisambiguationAdapter.class)
public final class Disambiguation implements Serializable {

  private static final long serialVersionUID = -5229197850609921790L;

  private final CellPosition position;

  private final CellAnnotation annotation;

  /**
   * Creates new cell disambiguation hint.
   *
   * @param position cell position
   * @param annotation hinted cell annotation
   */
  public Disambiguation(final CellPosition position, final CellAnnotation annotation) {
    Preconditions.checkNotNull(position);
    Preconditions.checkNotNull(annotation);

    this.position = position;
    this.annotation = annotation;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Disambiguation other = (Disambiguation) obj;
    if (this.annotation == null) {
      if (other.annotation != null) {
        return false;
      }
    } else if (!this.annotation.equals(other.annotation)) {
      return false;
    }
    if (this.position == null) {
      if (other.position != null) {
        return false;
      }
    } else if (!this.position.equals(other.position)) {
      return false;
    }
    return true;
  }

  /**
   * @return the annotation
   */
  public CellAnnotation getAnnotation() {
    return this.annotation;
  }

  /**
   * @return the position
   */
  public CellPosition getPosition() {
    return this.position;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.annotation == null) ? 0 : this.annotation.hashCode());
    result = (prime * result) + ((this.position == null) ? 0 : this.position.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Disambiguation [position=" + this.position + ", annotation=" + this.annotation + "]";
  }
}
