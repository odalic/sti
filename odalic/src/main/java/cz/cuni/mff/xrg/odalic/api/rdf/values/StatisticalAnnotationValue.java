package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.util.Set;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;
import cz.cuni.mff.xrg.odalic.api.rdf.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;

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
    component = ImmutableSet.of();
    predicate = ImmutableSet.of();
  }

  public StatisticalAnnotationValue(StatisticalAnnotation adaptee) {
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
    return component;
  }

  /**
   * @param component the component to set
   */
  public void setComponent(Set<? extends KnowledgeBaseComponentTypeValueEntry> component) {
    this.component = ImmutableSet.copyOf(component);
  }

  /**
   * @return the predicate
   */
  @RdfProperty("http://odalic.eu/internal/StatisticalAnnotation/predicate")
  public Set<KnowledgeBaseEntityCandidateSetEntry> getPredicate() {
    return predicate;
  }

  /**
   * @param predicate the predicate to set
   */
  public void setPredicate(Set<? extends KnowledgeBaseEntityCandidateSetEntry> predicate) {
    this.predicate = Annotations.copyValues(predicate);
  }

  public StatisticalAnnotation toStatisticalAnnotation() {
    final ImmutableMap.Builder<KnowledgeBase, ComponentTypeValue> componentMapBuilder =
        ImmutableMap.builder();
    for (final KnowledgeBaseComponentTypeValueEntry entry : component) {
      componentMapBuilder.put(entry.getBase().toKnowledgeBase(), entry.getValue());
    }

    return new StatisticalAnnotation(componentMapBuilder.build(), Annotations.toDomain(predicate));
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "StatisticalAnnotationValue [component=" + component + ", predicate=" + predicate + "]";
  }
}
