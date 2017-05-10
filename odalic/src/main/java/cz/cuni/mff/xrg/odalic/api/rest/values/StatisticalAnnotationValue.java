package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.EntityCandidateValueSetDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.EntityCandidateValueSetSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;

/**
 * <p>
 * Domain class {@link StatisticalAnnotation} adapted for REST API.
 * </p>
 *
 * @author Josef Janoušek
 *
 */
@XmlRootElement(name = "statisticalAnnotation")
public final class StatisticalAnnotationValue {

  private Map<String, ComponentTypeValue> component;

  private Map<String, Set<EntityCandidateValue>> predicate;

  public StatisticalAnnotationValue() {
    this.component = ImmutableMap.of();
    this.predicate = ImmutableMap.of();
  }

  public StatisticalAnnotationValue(final StatisticalAnnotation adaptee) {
    this.component = adaptee.getComponent();
    this.predicate = Annotations.toValues(adaptee.getPredicate());
  }

  /**
   * @return the component
   */
  @XmlAnyElement
  public Map<String, ComponentTypeValue> getComponent() {
    return this.component;
  }

  /**
   * @return the predicate
   */
  @XmlAnyElement
  @JsonDeserialize(contentUsing = EntityCandidateValueSetDeserializer.class)
  @JsonSerialize(contentUsing = EntityCandidateValueSetSerializer.class)
  public Map<String, Set<EntityCandidateValue>> getPredicate() {
    return this.predicate;
  }

  /**
   * @param component the component to set
   */
  public void setComponent(
      final Map<? extends String, ? extends ComponentTypeValue> component) {
    final ImmutableMap.Builder<String, ComponentTypeValue> componentBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends String, ? extends ComponentTypeValue> componentEntry : component
        .entrySet()) {
      componentBuilder.put(componentEntry.getKey(), componentEntry.getValue());
    }

    this.component = componentBuilder.build();
  }

  /**
   * @param predicate the predicate to set
   */
  public void setPredicate(
      final Map<? extends String, ? extends Set<? extends EntityCandidateValue>> predicate) {
    this.predicate = Annotations.copyValues(predicate);
  }

  @Override
  public String toString() {
    return "StatisticalAnnotationValue [component=" + this.component + ", predicate="
        + this.predicate + "]";
  }
}
