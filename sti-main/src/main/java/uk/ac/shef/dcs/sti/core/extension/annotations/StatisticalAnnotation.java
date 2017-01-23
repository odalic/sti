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
  public StatisticalAnnotation(
      ComponentTypeValue component,
      Set<? extends EntityCandidate> predicate) {
    Preconditions.checkNotNull(component);
    Preconditions.checkNotNull(predicate);
    
    this.component = component;
    this.predicate = ImmutableSet.copyOf(predicate);
  }

  /**
   * @return the component
   */
  public ComponentTypeValue getComponent() {
    return component;
  }

  /**
   * @return the predicate
   */
  public Set<EntityCandidate> getPredicate() {
    return predicate;
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
    result = prime * result + ((component == null) ? 0 : component.hashCode());
    result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
    return result;
  }

  /**
   * Compares for equality (only other annotation of the same component type
   * and the same predicate set passes).
   * 
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
    StatisticalAnnotation other = (StatisticalAnnotation) obj;
    if (component == null) {
      if (other.component != null) {
        return false;
      }
    } else if (!component.equals(other.component)) {
      return false;
    }
    if (predicate == null) {
      if (other.predicate != null) {
        return false;
      }
    } else if (!predicate.equals(other.predicate)) {
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "StatisticalAnnotation [component=" + component + ", predicate=" + predicate + "]";
  }
}
