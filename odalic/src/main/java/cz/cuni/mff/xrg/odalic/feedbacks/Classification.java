package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;

@XmlRootElement(name = "classification")
public final class Classification implements Serializable {

  private static final long serialVersionUID = 6053349406668481968L;

  @XmlElement
  private final ColumnPosition position;

  @XmlElement
  private final HeaderAnnotation annotation;

  @SuppressWarnings("unused")
  private Classification() {
    position = null;
    annotation = null;
  }
  
  public Classification(ColumnPosition position, HeaderAnnotation annotation) {
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
  public HeaderAnnotation getAnnotation() {
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
    Classification other = (Classification) obj;
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
    return "Classification [position=" + position + ", annotation=" + annotation + "]";
  }
}
