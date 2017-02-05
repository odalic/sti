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
    candidates = ImmutableSet.of();
    chosen = ImmutableSet.of();
  }

  /**
   * @param entities
   */
  public ColumnRelationAnnotationValue(ColumnRelationAnnotation adaptee) {
    this.candidates = Annotations.toNavigableValues(adaptee.getCandidates());
    this.chosen = Annotations.toValues(adaptee.getChosen());
  }

  /**
   * @return the candidates
   */
  @RdfProperty("http://odalic.eu/internal/ColumnRelationAnnotation/Candidates")
  public Set<KnowledgeBaseEntityCandidateNavigableSetEntry> getCandidates() {
    return candidates;
  }

  /**
   * @param candidates the candidates to set
   */
  public void setCandidates(
      Set<? extends KnowledgeBaseEntityCandidateNavigableSetEntry> candidates) {
    this.candidates = Annotations.copyNavigableValues(candidates);
  }

  /**
   * @return the chosen
   */
  @RdfProperty("http://odalic.eu/internal/ColumnRelationAnnotation/Chosen")
  public Set<KnowledgeBaseEntityCandidateSetEntry> getChosen() {
    return chosen;
  }

  /**
   * @param chosen the chosen to set
   */
  public void setChosen(
      Set<? extends KnowledgeBaseEntityCandidateSetEntry> chosen) {
    this.chosen = Annotations.copyValues(chosen);
  }

  public ColumnRelationAnnotation toColumnRelationAnnotation() {
    return new ColumnRelationAnnotation(Annotations.toNavigableDomain(candidates), Annotations.toDomain(chosen));
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ColumnRelationAnnotationValue [candidates=" + candidates + ", chosen=" + chosen + "]";
  }
}
