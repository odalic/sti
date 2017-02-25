package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.util.Set;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rdf.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;

/**
 * <p>
 * Domain class {@link HeaderAnnotation} adapted for RDF serialization.
 * </p>
 *
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/HeaderAnnotation")
public final class HeaderAnnotationValue {

  private Set<KnowledgeBaseEntityCandidateNavigableSetEntry> candidates;

  private Set<KnowledgeBaseEntityCandidateSetEntry> chosen;

  public HeaderAnnotationValue() {
    this.candidates = ImmutableSet.of();
    this.chosen = ImmutableSet.of();
  }

  /**
   * @param entities
   */
  public HeaderAnnotationValue(final HeaderAnnotation adaptee) {
    this.candidates = Annotations.toNavigableValues(adaptee.getCandidates());
    this.chosen = Annotations.toValues(adaptee.getChosen());
  }

  /**
   * @return the candidates
   */
  @RdfProperty("http://odalic.eu/internal/HeaderAnnotation/candidates")
  public Set<KnowledgeBaseEntityCandidateNavigableSetEntry> getCandidates() {
    return this.candidates;
  }

  /**
   * @return the chosen
   */
  @RdfProperty("http://odalic.eu/internal/HeaderAnnotation/chosen")
  public Set<KnowledgeBaseEntityCandidateSetEntry> getChosen() {
    return this.chosen;
  }

  /**
   * @param candidates the candidates to set
   */
  public void setCandidates(
      final Set<? extends KnowledgeBaseEntityCandidateNavigableSetEntry> candidates) {
    this.candidates = Annotations.copyNavigableValues(candidates);
  }

  /**
   * @param chosen the chosen to set
   */
  public void setChosen(final Set<? extends KnowledgeBaseEntityCandidateSetEntry> chosen) {
    this.chosen = Annotations.copyValues(chosen);
  }

  public HeaderAnnotation toHeaderAnnotation() {
    return new HeaderAnnotation(Annotations.toNavigableDomain(this.candidates),
        Annotations.toDomain(this.chosen));
  }

  @Override
  public String toString() {
    return "HeaderAnnotationValue [candidates=" + this.candidates + ", chosen=" + this.chosen + "]";
  }
}
