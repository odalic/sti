package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.util.Map;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.collect.ImmutableMap;

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

  private Map<KnowledgeBaseValue, ComponentTypeValue> component;

  private Map<KnowledgeBaseValue, EntityCandidateSetWrapper> predicate;

  public StatisticalAnnotationValue() {
    component = ImmutableMap.of();
    predicate = ImmutableMap.of();
  }

  public StatisticalAnnotationValue(StatisticalAnnotation adaptee) {
    this.component = adaptee.getComponent().entrySet().stream().collect(
        ImmutableMap.toImmutableMap(e -> new KnowledgeBaseValue(e.getKey()), e -> e.getValue()));
    this.predicate = Annotations.toValues(adaptee.getPredicate());
  }

  /**
   * @return the component
   */
  @RdfProperty("http://odalic.eu/internal/StatisticalAnnotation/Component")
  public Map<KnowledgeBaseValue, ComponentTypeValue> getComponent() {
    return component;
  }

  /**
   * @param component the component to set
   */
  public void setComponent(
      Map<? extends KnowledgeBaseValue, ? extends ComponentTypeValue> component) {
    this.component = ImmutableMap.copyOf(component);
  }

  /**
   * @return the predicate
   */
  @RdfProperty("http://odalic.eu/internal/StatisticalAnnotation/Predicate")
  public Map<KnowledgeBaseValue, EntityCandidateSetWrapper> getPredicate() {
    return predicate;
  }

  /**
   * @param predicate the predicate to set
   */
  public void setPredicate(
      Map<? extends KnowledgeBaseValue, ? extends EntityCandidateSetWrapper> predicate) {
    this.predicate = Annotations.copyValues(predicate);
  }

  public StatisticalAnnotation toStatisticalAnnotation() {
    return new StatisticalAnnotation(
        component.entrySet().stream().collect(
            ImmutableMap.toImmutableMap(e -> e.getKey().toKnowledgeBase(), e -> e.getValue())),
        Annotations.toDomain(predicate));
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
