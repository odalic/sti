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
import cz.cuni.mff.xrg.odalic.api.rest.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;

/**
 * <p>
 * Domain class {@link CellAnnotation} adapted for REST API.
 * </p>
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "cellAnnotation")
public final class CellAnnotationValue {

  private Map<String, NavigableSet<EntityCandidateValue>> candidates;

  private Map<String, Set<EntityCandidateValue>> chosen;

  public CellAnnotationValue() {
    this.candidates = ImmutableMap.of();
    this.chosen = ImmutableMap.of();
  }

  public CellAnnotationValue(final CellAnnotation adaptee) {
    this.candidates = Annotations.toNavigableValues(adaptee.getCandidates());
    this.chosen = Annotations.toValues(adaptee.getChosen());
  }

  /**
   * @return the candidates
   */
  @XmlAnyElement
  @JsonDeserialize(contentUsing = EntityCandidateValueNavigableSetDeserializer.class)
  @JsonSerialize(contentUsing = EntityCandidateValueSetSerializer.class)
  public Map<String, NavigableSet<EntityCandidateValue>> getCandidates() {
    return this.candidates;
  }

  /**
   * @return the chosen
   */
  @XmlAnyElement
  @JsonDeserialize(contentUsing = EntityCandidateValueSetDeserializer.class)
  @JsonSerialize(contentUsing = EntityCandidateValueSetSerializer.class)
  public Map<String, Set<EntityCandidateValue>> getChosen() {
    return this.chosen;
  }

  /**
   * @param candidates the candidates to set
   */
  public void setCandidates(
      final Map<? extends String, ? extends NavigableSet<? extends EntityCandidateValue>> candidates) {
    this.candidates = Annotations.copyNavigableValues(candidates);
  }

  /**
   * @param chosen the chosen to set
   */
  public void setChosen(
      final Map<? extends String, ? extends Set<? extends EntityCandidateValue>> chosen) {
    this.chosen = Annotations.copyValues(chosen);
  }

  @Override
  public String toString() {
    return "CellAnnotationValue [candidates=" + this.candidates + ", chosen=" + this.chosen + "]";
  }
}
