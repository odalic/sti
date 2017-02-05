package cz.cuni.mff.xrg.odalic.api.rdf.values.util;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rdf.values.EntityCandidateNavigableSetWrapper;
import cz.cuni.mff.xrg.odalic.api.rdf.values.EntityCandidateSetWrapper;
import cz.cuni.mff.xrg.odalic.api.rdf.values.EntityCandidateValue;
import cz.cuni.mff.xrg.odalic.api.rdf.values.KnowledgeBaseValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Annotation conversion utilities.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public final class Annotations {

  private Annotations() {}

  public static Map<KnowledgeBaseValue, EntityCandidateNavigableSetWrapper> toNavigableValues(
      final Map<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidate>> candidates) {
    final ImmutableMap.Builder<KnowledgeBaseValue, EntityCandidateNavigableSetWrapper> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidate>> entry : candidates
        .entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final NavigableSet<? extends EntityCandidate> baseCandidates = entry.getValue();

      final Stream<EntityCandidateValue> stream =
          baseCandidates.stream().map(e -> new EntityCandidateValue(e));
      candidatesBuilder.put(new KnowledgeBaseValue(base), new EntityCandidateNavigableSetWrapper(ImmutableSortedSet.copyOf(stream.iterator())));
    }
    return candidatesBuilder.build();
  }

  public static Map<KnowledgeBaseValue, EntityCandidateSetWrapper> toValues(
      final Map<KnowledgeBase, Set<EntityCandidate>> chosen) {
    final ImmutableMap.Builder<KnowledgeBaseValue, EntityCandidateSetWrapper> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<KnowledgeBase, Set<EntityCandidate>> entry : chosen.entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final Set<EntityCandidate> baseChosen = entry.getValue();

      final Stream<EntityCandidateValue> stream =
          baseChosen.stream().map(e -> new EntityCandidateValue(e));
      chosenBuilder.put(new KnowledgeBaseValue(base), new EntityCandidateSetWrapper(ImmutableSet.copyOf(stream.iterator())));
    }
    return chosenBuilder.build();
  }

  public static Map<KnowledgeBaseValue, EntityCandidateNavigableSetWrapper> copyNavigableValues(
      Map<? extends KnowledgeBaseValue, ? extends EntityCandidateNavigableSetWrapper> candidates) {
    final ImmutableMap.Builder<KnowledgeBaseValue, EntityCandidateNavigableSetWrapper> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBaseValue, ? extends EntityCandidateNavigableSetWrapper> candidateEntry : candidates
        .entrySet()) {
      candidatesBuilder.put(candidateEntry.getKey(), candidateEntry.getValue());
    }

    return candidatesBuilder.build();
  }

  public static Map<KnowledgeBaseValue, EntityCandidateSetWrapper> copyValues(
      Map<? extends KnowledgeBaseValue, ? extends EntityCandidateSetWrapper> chosen) {
    final ImmutableMap.Builder<KnowledgeBaseValue, EntityCandidateSetWrapper> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBaseValue, ? extends EntityCandidateSetWrapper> chosenEntry : chosen
        .entrySet()) {
      chosenBuilder.put(chosenEntry.getKey(), chosenEntry.getValue());
    }

    return chosenBuilder.build();
  }

  public static Map<KnowledgeBase, Set<EntityCandidate>> toNavigableDomain(
      final Map<KnowledgeBaseValue, EntityCandidateNavigableSetWrapper> candidates) {
    final ImmutableMap.Builder<KnowledgeBase, Set<EntityCandidate>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBaseValue, ? extends EntityCandidateNavigableSetWrapper> entry : candidates
        .entrySet()) {
      final KnowledgeBase base = entry.getKey().toKnowledgeBase();
      final Set<EntityCandidateValue> values = entry.getValue().getValue();

      chosenBuilder.put(base, ImmutableSet.copyOf(
          values.stream().map(e -> new EntityCandidate(e.getEntity().toEntity(), e.getScore().toScore())).iterator()));
    }
    final Map<KnowledgeBase, Set<EntityCandidate>> chosen = chosenBuilder.build();
    return chosen;
  }

  public static Map<KnowledgeBase, Set<EntityCandidate>> toDomain(
      final Map<? extends KnowledgeBaseValue, ? extends EntityCandidateSetWrapper> candidateValues) {
    final ImmutableMap.Builder<KnowledgeBase, Set<EntityCandidate>> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends KnowledgeBaseValue, ? extends EntityCandidateSetWrapper> entry : candidateValues
        .entrySet()) {
      final KnowledgeBase base = entry.getKey().toKnowledgeBase();
      final Set<EntityCandidateValue> values = entry.getValue().getValue();

      candidatesBuilder.put(base, ImmutableSet.copyOf(
          values.stream().map(e -> new EntityCandidate(e.getEntity().toEntity(), e.getScore().toScore())).iterator()));
    }
    final Map<KnowledgeBase, Set<EntityCandidate>> candidates = candidatesBuilder.build();
    return candidates;
  }

}
