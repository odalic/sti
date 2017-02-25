package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.StatisticalAnnotationAdapter;
import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;

/**
 * Annotates column in a table for statistical data processing.
 *
 * @author Josef Janoušek
 *
 */
@Immutable
@XmlJavaTypeAdapter(StatisticalAnnotationAdapter.class)
public final class StatisticalAnnotation implements Serializable {

  private static final long serialVersionUID = -1695807148169080424L;

  private final Map<KnowledgeBase, ComponentTypeValue> component;

  private final Map<KnowledgeBase, Set<EntityCandidate>> predicate;

  /**
   * Creates new annotation.
   *
   * @param component type of statistical component
   * @param predicate predicate for statistical component
   */
  public StatisticalAnnotation(
      final Map<? extends KnowledgeBase, ? extends ComponentTypeValue> component,
      final Map<? extends KnowledgeBase, ? extends Set<? extends EntityCandidate>> predicate) {
    Preconditions.checkNotNull(component);
    Preconditions.checkNotNull(predicate);

    final ImmutableMap.Builder<KnowledgeBase, ComponentTypeValue> componentBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends ComponentTypeValue> componentEntry : component
        .entrySet()) {
      componentBuilder.put(componentEntry.getKey(), componentEntry.getValue());
    }
    this.component = componentBuilder.build();

    final ImmutableMap.Builder<KnowledgeBase, Set<EntityCandidate>> predicateBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends Set<? extends EntityCandidate>> predicateEntry : predicate
        .entrySet()) {
      predicateBuilder.put(predicateEntry.getKey(), ImmutableSet.copyOf(predicateEntry.getValue()));
    }
    this.predicate = predicateBuilder.build();
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
  public Map<KnowledgeBase, ComponentTypeValue> getComponent() {
    return this.component;
  }

  /**
   * @return the predicate
   */
  public Map<KnowledgeBase, Set<EntityCandidate>> getPredicate() {
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

  /**
   * Merges with the other annotation.
   *
   * @param other annotation based on different set of knowledge bases
   * @return merged annotation
   * @throws IllegalArgumentException If both this and the other annotation have some candidates
   *         from the same knowledge base
   */
  public StatisticalAnnotation merge(final StatisticalAnnotation other)
      throws IllegalArgumentException {
    final ImmutableMap.Builder<KnowledgeBase, ComponentTypeValue> componentBuilder =
        ImmutableMap.builder();
    componentBuilder.putAll(this.component);
    componentBuilder.putAll(other.component);

    final ImmutableMap.Builder<KnowledgeBase, Set<EntityCandidate>> predicateBuilder =
        ImmutableMap.builder();
    predicateBuilder.putAll(this.predicate);
    predicateBuilder.putAll(other.predicate);

    return new StatisticalAnnotation(componentBuilder.build(), predicateBuilder.build());
  }

  @Override
  public String toString() {
    return "StatisticalAnnotation [component=" + this.component + ", predicate=" + this.predicate
        + "]";
  }
}
