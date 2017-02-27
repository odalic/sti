package uk.ac.shef.dcs.sti.core.extension.annotations;

import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Annotates column in a table for statistical data processing.
 *
 * @author Josef Janoušek
 *
 */
@Immutable
public final class StatisticalAnnotation {

  private final ComponentTypeValue component;

  private final Set<EntityCandidate> predicate;

  /**
   * Creates new annotation.
   *
   * @param component type of statistical component
   * @param predicate predicate for statistical component
   */
  public StatisticalAnnotation(final ComponentTypeValue component,
      final Set<? extends EntityCandidate> predicate) {
    Preconditions.checkNotNull(component);
    Preconditions.checkNotNull(predicate);

    this.component = component;
    this.predicate = ImmutableSet.copyOf(predicate);
  }

  /**
   * Compares for equality (only other annotation of the same component type and the same predicate
   * set passes).
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
    final StatisticalAnnotation other = (StatisticalAnnotation) obj;
    if (this.component == null) {
      if (other.component != null) {
        return false;
      }
    } else if (!this.component.equals(other.component)) {
      return false;
    }
    if (this.predicate == null) {
      if (other.predicate != null) {
        return false;
      }
    } else if (!this.predicate.equals(other.predicate)) {
      return false;
    }
    return true;
  }

  /**
   * @return the component
   */
  public ComponentTypeValue getComponent() {
    return this.component;
  }

  /**
   * @return the predicate
   */
  public Set<EntityCandidate> getPredicate() {
    return this.predicate;
  }

  /**
   * Computes hash code based on the component and the predicate.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.component == null) ? 0 : this.component.hashCode());
    result = (prime * result) + ((this.predicate == null) ? 0 : this.predicate.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "StatisticalAnnotation [component=" + this.component + ", predicate=" + this.predicate
        + "]";
  }
}
