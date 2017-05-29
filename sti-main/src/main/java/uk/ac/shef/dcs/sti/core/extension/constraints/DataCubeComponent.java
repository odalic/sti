package uk.ac.shef.dcs.sti.core.extension.constraints;

import java.io.Serializable;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.sti.core.extension.annotations.StatisticalAnnotation;
import uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition;

/**
 * DataCubeComponent hint.
 *
 * @author Josef Janoušek
 *
 */
public final class DataCubeComponent implements Serializable {

  private static final long serialVersionUID = 6053349406668481968L;

  private final ColumnPosition position;

  private final StatisticalAnnotation annotation;

  /**
   * Creates custom DataCubeComponent hint of a column.
   *
   * @param position column position
   * @param annotation custom annotation
   */
  public DataCubeComponent(final ColumnPosition position, final StatisticalAnnotation annotation) {
    Preconditions.checkNotNull(position, "The position cannot be null!");
    Preconditions.checkNotNull(annotation, "The annotation cannot be null!");

    this.position = position;
    this.annotation = annotation;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final DataCubeComponent other = (DataCubeComponent) obj;
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
  public StatisticalAnnotation getAnnotation() {
    return this.annotation;
  }

  /**
   * @return the position
   */
  public ColumnPosition getPosition() {
    return this.position;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.annotation == null) ? 0 : this.annotation.hashCode());
    result = (prime * result) + ((this.position == null) ? 0 : this.position.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "DataCubeComponent [position=" + this.position + ", annotation=" + this.annotation + "]";
  }
}
