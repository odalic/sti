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
import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

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

  private Map<KnowledgeBase, ComponentTypeValue> component;

  private Map<KnowledgeBase, Set<EntityCandidateValue>> predicate;

  public StatisticalAnnotationValue() {
    component = ImmutableMap.of();
    predicate = ImmutableMap.of();
  }

  public StatisticalAnnotationValue(StatisticalAnnotation adaptee) {
    this.component = adaptee.getComponent();
    this.predicate = Annotations.toValues(adaptee.getPredicate());
  }

  /**
   * @return the component
   */
  @XmlAnyElement
  @JsonDeserialize(keyUsing = KnowledgeBaseKeyJsonDeserializer.class)
  @JsonSerialize(keyUsing = KnowledgeBaseKeyJsonSerializer.class)
  public Map<KnowledgeBase, ComponentTypeValue> getComponent() {
    return component;
  }

  /**
   * @param component the component to set
   */
  public void setComponent(
      Map<? extends KnowledgeBase, ? extends ComponentTypeValue> component) {
    final ImmutableMap.Builder<KnowledgeBase, ComponentTypeValue> componentBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends ComponentTypeValue> componentEntry : component
        .entrySet()) {
      componentBuilder.put(componentEntry.getKey(), componentEntry.getValue());
    }

    this.component = componentBuilder.build();
  }

  /**
   * @return the predicate
   */
  @XmlAnyElement
  @JsonDeserialize(keyUsing = KnowledgeBaseKeyJsonDeserializer.class,
      contentUsing = EntityCandidateValueSetDeserializer.class)
  @JsonSerialize(keyUsing = KnowledgeBaseKeyJsonSerializer.class,
      contentUsing = EntityCandidateValueSetSerializer.class)
  public Map<KnowledgeBase, Set<EntityCandidateValue>> getPredicate() {
    return predicate;
  }

  /**
   * @param predicate the predicate to set
   */
  public void setPredicate(
      Map<? extends KnowledgeBase, ? extends Set<? extends EntityCandidateValue>> predicate) {
    this.predicate = Annotations.copyValues(predicate);
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
