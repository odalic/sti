package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.DataCubeComponentAdapter;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;

/**
 * DataCubeComponent hint.
 *
 * @author Josef Janoušek
 *
 */
@XmlJavaTypeAdapter(DataCubeComponentAdapter.class)
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
    return "DataCubeCompoment [position=" + this.position + ", annotation=" + this.annotation + "]";
  }
}
