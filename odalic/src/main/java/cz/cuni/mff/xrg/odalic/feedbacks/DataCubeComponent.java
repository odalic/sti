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
  public DataCubeComponent(ColumnPosition position, StatisticalAnnotation annotation) {
    Preconditions.checkNotNull(position);
    Preconditions.checkNotNull(annotation);

    this.position = position;
    this.annotation = annotation;
  }

  /**
   * @return the position
   */
  public ColumnPosition getPosition() {
    return position;
  }

  /**
   * @return the annotation
   */
  public StatisticalAnnotation getAnnotation() {
    return annotation;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
    result = prime * result + ((position == null) ? 0 : position.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DataCubeComponent other = (DataCubeComponent) obj;
    if (annotation == null) {
      if (other.annotation != null) {
        return false;
      }
    } else if (!annotation.equals(other.annotation)) {
      return false;
    }
    if (position == null) {
      if (other.position != null) {
        return false;
      }
    } else if (!position.equals(other.position)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "DataCubeCompoment [position=" + position + ", annotation=" + annotation + "]";
  }
}
