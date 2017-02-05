package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.util.Map;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.collect.ImmutableMap;

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

  private Map<KnowledgeBaseValue, EntityCandidateNavigableSetWrapper> candidates;

  private Map<KnowledgeBaseValue, EntityCandidateSetWrapper> chosen;

  public HeaderAnnotationValue() {
    candidates = ImmutableMap.of();
    chosen = ImmutableMap.of();
  }

  /**
   * @param entities
   */
  public HeaderAnnotationValue(HeaderAnnotation adaptee) {
    this.candidates = Annotations.toNavigableValues(adaptee.getCandidates());
    this.chosen = Annotations.toValues(adaptee.getChosen());
  }

  /**
   * @return the candidates
   */
  @RdfProperty("http://odalic.eu/internal/HeaderAnnotation/Candidates")
  public Map<KnowledgeBaseValue, EntityCandidateNavigableSetWrapper> getCandidates() {
    return candidates;
  }

  /**
   * @param candidates the candidates to set
   */
  public void setCandidates(
      Map<? extends KnowledgeBaseValue, ? extends EntityCandidateNavigableSetWrapper> candidates) {
    this.candidates = Annotations.copyNavigableValues(candidates);
  }

  /**
   * @return the chosen
   */
  @RdfProperty("http://odalic.eu/internal/HeaderAnnotation/Chosen")
  public Map<KnowledgeBaseValue, EntityCandidateSetWrapper> getChosen() {
    return chosen;
  }

  /**
   * @param chosen the chosen to set
   */
  public void setChosen(
      Map<? extends KnowledgeBaseValue, ? extends EntityCandidateSetWrapper> chosen) {
    this.chosen = Annotations.copyValues(chosen);
  }

  public HeaderAnnotation toHeaderAnnotation() {
    return new HeaderAnnotation(Annotations.toNavigableDomain(candidates), Annotations.toDomain(chosen));
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "HeaderAnnotationValue [candidates=" + candidates + ", chosen=" + chosen + "]";
  }
}
