package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.EntityCandidateValueNavigableSetDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.EntityCandidateValueSetDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.EntityCandidateValueSetSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * <p>
 * Domain class {@link ColumnRelationAnnotation} adapted for REST API.
 * </p>
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "columnRelationAnnotation")
public final class ColumnRelationAnnotationValue {

  private Map<KnowledgeBase, NavigableSet<EntityCandidateValue>> candidates;

  private Map<KnowledgeBase, Set<EntityCandidateValue>> chosen;

  public ColumnRelationAnnotationValue() {
    this.candidates = ImmutableMap.of();
    this.chosen = ImmutableMap.of();
  }

  public ColumnRelationAnnotationValue(final ColumnRelationAnnotation adaptee) {
    this.candidates = Annotations.toNavigableValues(adaptee.getCandidates());
    this.chosen = Annotations.toValues(adaptee.getChosen());
  }

  /**
   * @return the candidates
   */
  @XmlAnyElement
  @JsonDeserialize(keyUsing = KnowledgeBaseKeyJsonDeserializer.class,
      contentUsing = EntityCandidateValueNavigableSetDeserializer.class)
  @JsonSerialize(keyUsing = KnowledgeBaseKeyJsonSerializer.class,
      contentUsing = EntityCandidateValueSetSerializer.class)
  public Map<KnowledgeBase, NavigableSet<EntityCandidateValue>> getCandidates() {
    return this.candidates;
  }

  /**
   * @return the chosen
   */
  @XmlAnyElement
  @JsonDeserialize(keyUsing = KnowledgeBaseKeyJsonDeserializer.class,
      contentUsing = EntityCandidateValueSetDeserializer.class)
  @JsonSerialize(keyUsing = KnowledgeBaseKeyJsonSerializer.class,
      contentUsing = EntityCandidateValueSetSerializer.class)
  public Map<KnowledgeBase, Set<EntityCandidateValue>> getChosen() {
    return this.chosen;
  }

  /**
   * @param candidates the candidates to set
   */
  public void setCandidates(
      final Map<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidateValue>> candidates) {
    this.candidates = Annotations.copyNavigableValues(candidates);
  }

  /**
   * @param chosen the chosen to set
   */
  public void setChosen(
      final Map<? extends KnowledgeBase, ? extends Set<? extends EntityCandidateValue>> chosen) {
    this.chosen = Annotations.copyValues(chosen);
  }

  @Override
  public String toString() {
    return "ColumnRelationAnnotationValue [candidates=" + this.candidates + ", chosen="
        + this.chosen + "]";
  }
}
