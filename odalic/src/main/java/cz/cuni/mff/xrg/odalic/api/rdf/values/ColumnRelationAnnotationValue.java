package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.util.Set;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rdf.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;

/**
 * <p>
 * Domain class {@link ColumnRelationAnnotation} adapted for RDF serialization.
 * </p>
 *
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/ColumnRelationAnnotation")
public final class ColumnRelationAnnotationValue {

  private Set<KnowledgeBaseEntityCandidateNavigableSetEntry> candidates;

  private Set<KnowledgeBaseEntityCandidateSetEntry> chosen;

  public ColumnRelationAnnotationValue() {
    this.candidates = ImmutableSet.of();
    this.chosen = ImmutableSet.of();
  }

  /**
   * @param entities
   */
  public ColumnRelationAnnotationValue(final ColumnRelationAnnotation adaptee) {
    this.candidates = Annotations.toNavigableValues(adaptee.getCandidates());
    this.chosen = Annotations.toValues(adaptee.getChosen());
  }

  /**
   * @return the candidates
   */
  @RdfProperty("http://odalic.eu/internal/ColumnRelationAnnotation/candidates")
  public Set<KnowledgeBaseEntityCandidateNavigableSetEntry> getCandidates() {
    return this.candidates;
  }

  /**
   * @return the chosen
   */
  @RdfProperty("http://odalic.eu/internal/ColumnRelationAnnotation/chosen")
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

  public ColumnRelationAnnotation toColumnRelationAnnotation() {
    return new ColumnRelationAnnotation(Annotations.toNavigableDomain(this.candidates),
        Annotations.toDomain(this.chosen));
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnRelationAnnotationValue [candidates=" + this.candidates + ", chosen="
        + this.chosen + "]";
  }
}
