package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.util.Set;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rdf.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;

/**
 * <p>
 * Domain class {@link StatisticalAnnotation} adapted for RDF serialization.
 * </p>
 *
 * @author Josef Janoušek
 *
 */
@RdfsClass("http://odalic.eu/internal/StatisticalAnnotation")
public final class StatisticalAnnotationValue {

  private Set<KnowledgeBaseComponentTypeValueEntry> component;

  private Set<KnowledgeBaseEntityCandidateSetEntry> predicate;

  public StatisticalAnnotationValue() {
    this.component = ImmutableSet.of();
    this.predicate = ImmutableSet.of();
  }

  public StatisticalAnnotationValue(final StatisticalAnnotation adaptee) {
    this.component = adaptee.getComponent().entrySet().stream()
        .map(e -> new KnowledgeBaseComponentTypeValueEntry(new KnowledgeBaseValue(e.getKey()),
            e.getValue()))
        .collect(ImmutableSet.toImmutableSet());
    this.predicate = Annotations.toValues(adaptee.getPredicate());
  }

  /**
   * @return the component
   */
  @RdfProperty("http://odalic.eu/internal/StatisticalAnnotation/component")
  public Set<KnowledgeBaseComponentTypeValueEntry> getComponent() {
    return this.component;
  }

  /**
   * @return the predicate
   */
  @RdfProperty("http://odalic.eu/internal/StatisticalAnnotation/predicate")
  public Set<KnowledgeBaseEntityCandidateSetEntry> getPredicate() {
    return this.predicate;
  }

  /**
   * @param component the component to set
   */
  public void setComponent(final Set<? extends KnowledgeBaseComponentTypeValueEntry> component) {
    this.component = ImmutableSet.copyOf(component);
  }

  /**
   * @param predicate the predicate to set
   */
  public void setPredicate(final Set<? extends KnowledgeBaseEntityCandidateSetEntry> predicate) {
    this.predicate = Annotations.copyValues(predicate);
  }

  public StatisticalAnnotation toStatisticalAnnotation() {
    final ImmutableMap.Builder<KnowledgeBase, ComponentTypeValue> componentMapBuilder =
        ImmutableMap.builder();
    for (final KnowledgeBaseComponentTypeValueEntry entry : this.component) {
      componentMapBuilder.put(entry.getBase().toKnowledgeBase(), entry.getValue());
    }

    return new StatisticalAnnotation(componentMapBuilder.build(),
        Annotations.toDomain(this.predicate));
  }

  @Override
  public String toString() {
    return "StatisticalAnnotationValue [component=" + this.component + ", predicate="
        + this.predicate + "]";
  }
}
