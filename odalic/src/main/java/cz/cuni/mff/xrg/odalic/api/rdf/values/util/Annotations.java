package cz.cuni.mff.xrg.odalic.api.rdf.values.util;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rdf.values.EntityCandidateNavigableSetWrapper;
import cz.cuni.mff.xrg.odalic.api.rdf.values.EntityCandidateSetWrapper;
import cz.cuni.mff.xrg.odalic.api.rdf.values.EntityCandidateValue;
import cz.cuni.mff.xrg.odalic.api.rdf.values.KnowledgeBaseEntityCandidateNavigableSetEntry;
import cz.cuni.mff.xrg.odalic.api.rdf.values.KnowledgeBaseEntityCandidateSetEntry;
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

  public static Set<KnowledgeBaseEntityCandidateNavigableSetEntry> toNavigableValues(
      final Map<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidate>> candidates) {
    Preconditions.checkNotNull(candidates);

    final ImmutableSet.Builder<KnowledgeBaseEntityCandidateNavigableSetEntry> candidatesBuilder =
        ImmutableSet.builder();
    for (final Map.Entry<? extends KnowledgeBase, ? extends NavigableSet<? extends EntityCandidate>> entry : candidates
        .entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final NavigableSet<? extends EntityCandidate> baseCandidates = entry.getValue();

      final NavigableSet<EntityCandidateValue> values =
          baseCandidates.stream().map(e -> new EntityCandidateValue(e)).collect(
              ImmutableSortedSet.toImmutableSortedSet((first, second) -> first.compareTo(second)));
      candidatesBuilder.add(new KnowledgeBaseEntityCandidateNavigableSetEntry(
          new KnowledgeBaseValue(base), new EntityCandidateNavigableSetWrapper(values)));
    }
    return candidatesBuilder.build();
  }

  public static Set<KnowledgeBaseEntityCandidateSetEntry> toValues(
      final Map<KnowledgeBase, Set<EntityCandidate>> chosen) {
    Preconditions.checkNotNull(chosen);

    final ImmutableSet.Builder<KnowledgeBaseEntityCandidateSetEntry> chosenBuilder =
        ImmutableSet.builder();
    for (final Map.Entry<KnowledgeBase, Set<EntityCandidate>> entry : chosen.entrySet()) {
      final KnowledgeBase base = entry.getKey();
      final Set<EntityCandidate> baseChosen = entry.getValue();

      final Set<EntityCandidateValue> values = baseChosen.stream()
          .map(e -> new EntityCandidateValue(e)).collect(ImmutableSet.toImmutableSet());
      chosenBuilder.add(new KnowledgeBaseEntityCandidateSetEntry(new KnowledgeBaseValue(base),
          new EntityCandidateSetWrapper(values)));
    }
    return chosenBuilder.build();
  }

  public static Set<KnowledgeBaseEntityCandidateNavigableSetEntry> copyNavigableValues(
      Set<? extends KnowledgeBaseEntityCandidateNavigableSetEntry> candidates) {
    Preconditions.checkNotNull(candidates);

    return ImmutableSet.copyOf(candidates);
  }

  public static Set<KnowledgeBaseEntityCandidateSetEntry> copyValues(
      Set<? extends KnowledgeBaseEntityCandidateSetEntry> chosen) {
    Preconditions.checkNotNull(chosen);

    return ImmutableSet.copyOf(chosen);
  }

  public static Map<KnowledgeBase, NavigableSet<EntityCandidate>> toNavigableDomain(
      final Set<? extends KnowledgeBaseEntityCandidateNavigableSetEntry> candidates) {
    Preconditions.checkNotNull(candidates);

    final ImmutableMap.Builder<KnowledgeBase, NavigableSet<EntityCandidate>> chosenBuilder =
        ImmutableMap.builder();
    for (final KnowledgeBaseEntityCandidateNavigableSetEntry entry : candidates) {
      final KnowledgeBase base = entry.getBase().toKnowledgeBase();
      final Set<EntityCandidateValue> values = entry.getSet().getValue();

      final NavigableSet<EntityCandidate> domainValues = values.stream()
          .map(e -> new EntityCandidate(e.getEntity().toEntity(), e.getScore().toScore())).collect(
              ImmutableSortedSet.toImmutableSortedSet((first, second) -> first.compareTo(second)));

      chosenBuilder.put(base, domainValues);
    }
    return chosenBuilder.build();
  }

  public static Map<KnowledgeBase, Set<EntityCandidate>> toDomain(
      final Set<? extends KnowledgeBaseEntityCandidateSetEntry> candidateValues) {
    Preconditions.checkNotNull(candidateValues);

    final ImmutableMap.Builder<KnowledgeBase, Set<EntityCandidate>> candidatesBuilder =
        ImmutableMap.builder();
    for (final KnowledgeBaseEntityCandidateSetEntry entry : candidateValues) {
      final KnowledgeBase base = entry.getBase().toKnowledgeBase();
      final Set<EntityCandidateValue> values = entry.getSet().getValue();

      final Set<EntityCandidate> domainValues = values.stream()
          .map(e -> new EntityCandidate(e.getEntity().toEntity(), e.getScore().toScore()))
          .collect(ImmutableSet.toImmutableSet());

      candidatesBuilder.put(base, domainValues);
    }
    return candidatesBuilder.build();
  }

}
