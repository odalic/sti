package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.KnowledgeBaseKeyJsonSerializer;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * <p>
 * Domain class {@link HeaderAnnotation} adapted for REST API.
 * </p>
 * 
 * <p>
 * In contrast to the adapted class, annotation in this version have only one set of candidates and
 * the chosen ones are indicated by flags on each element.
 * </p>
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "headerAnnotation")
public final class HeaderAnnotationValue {

  private Map<KnowledgeBase, NavigableSet<EntityCandidateValue>> candidates;

  public HeaderAnnotationValue() {
    candidates = ImmutableMap.of();
  }

  /**
   * @param entities
   */
  public HeaderAnnotationValue(HeaderAnnotation adaptee) {
    final Map<KnowledgeBase, Set<EntityCandidate>> chosen = adaptee.getChosen();

    final ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidateValue>> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<KnowledgeBase, NavigableSet<EntityCandidate>> entry : adaptee
        .getCandidates().entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final Set<EntityCandidate> baseChosen = chosen.get(base);
      final NavigableSet<EntityCandidate> baseCandidates = entry.getValue();

      final Stream<EntityCandidateValue> stream =
          baseCandidates.stream().map(e -> new EntityCandidateValue(e, baseChosen.contains(e)));
      candidatesBuilder.put(entry.getKey(), ImmutableSortedSet.copyOf(stream.iterator()));
    }

    this.candidates = candidatesBuilder.build();
  }

  /**
   * @return the candidates
   */
  @XmlElement
  @JsonDeserialize(keyUsing = KnowledgeBaseKeyJsonDeserializer.class)
  @JsonSerialize(keyUsing = KnowledgeBaseKeyJsonSerializer.class)
  public Map<KnowledgeBase, NavigableSet<EntityCandidateValue>> getCandidates() {
    return candidates;
  }

  /**
   * @param candidates the candidates to set
   */
  public void setCandidates(
      Map<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidateValue>> candidates) {
    ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidateValue>> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends Set<? extends EntityCandidateValue>> candidateEntry : candidates
        .entrySet()) {
      candidatesBuilder.put(candidateEntry.getKey(),
          ImmutableSortedSet.copyOf(candidateEntry.getValue()));
    }

    this.candidates = candidatesBuilder.build();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "HeaderAnnotationValue [candidates=" + candidates + "]";
  }
}
