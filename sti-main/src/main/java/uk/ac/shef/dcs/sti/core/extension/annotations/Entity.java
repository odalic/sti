package uk.ac.shef.dcs.sti.core.extension.annotations;

import java.io.Serializable;

import com.google.common.base.Preconditions;

/**
 * Groups the resource ID and its label in one handy class.
 *
 * @author Václav Brodec
 */
public final class Entity implements Comparable<Entity>, Serializable {

  private static final long serialVersionUID = -3001706805535088480L;

  private final String resource;

  private final String label;

  /**
   * Creates new entity representation.
   *
   * @param resource entity resource ID
   * @param label label
   */
  public Entity(final String resource, final String label) {
    Preconditions.checkNotNull(resource, "The resource cannot be null!");
    Preconditions.checkNotNull(label, "The label cannot be null!");

    this.resource = resource;
    this.label = label;
  }

  /**
   * Compares the entities by their resource ID lexicographically.
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   * @see java.lang.String#compareTo(String) for the definition of resource ID comparison
   */
  @Override
  public int compareTo(final Entity o) {
    return this.resource.compareTo(o.resource);
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
    final Entity other = (Entity) obj;
    if (this.resource == null) {
      if (other.resource != null) {
        return false;
      }
    } else if (!this.resource.equals(other.resource)) {
      return false;
    }
    if (this.label == null) {
      if (other.label != null) {
        return false;
      }
    } else if (!this.label.equals(other.label)) {
      return false;
    }
    return true;
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * @return the resource ID
   */
  public String getResource() {
    return this.resource;
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
    result = (prime * result) + ((this.resource == null) ? 0 : this.resource.hashCode());
    result = (prime * result) + ((this.label == null) ? 0 : this.label.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Annotation [resource=" + this.resource + ", label=" + this.label + "]";
  }
}
