package cz.cuni.mff.xrg.odalic.api.rdf.values.util;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rdf.values.ColumnPositionSetWrapper;
import cz.cuni.mff.xrg.odalic.api.rdf.values.ColumnPositionValue;
import cz.cuni.mff.xrg.odalic.api.rdf.values.EntityCandidateNavigableSetWrapper;
import cz.cuni.mff.xrg.odalic.api.rdf.values.EntityCandidateSetWrapper;
import cz.cuni.mff.xrg.odalic.api.rdf.values.EntityCandidateValue;
import cz.cuni.mff.xrg.odalic.api.rdf.values.KnowledgeBaseColumnPositionSetEntry;
import cz.cuni.mff.xrg.odalic.api.rdf.values.KnowledgeBaseEntityCandidateNavigableSetEntry;
import cz.cuni.mff.xrg.odalic.api.rdf.values.KnowledgeBaseEntityCandidateSetEntry;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;

/**
 * Annotation conversion utilities.
 *
 * @author Václav Brodec
 *
 */
@Immutable
public final class Annotations {

  /**
   * Makes a copy of the argument. 
   * 
   * @param candidates candidates
   * @return copied candidates
   */
  public static Set<KnowledgeBaseEntityCandidateNavigableSetEntry> copyNavigableValues(
      final Set<? extends KnowledgeBaseEntityCandidateNavigableSetEntry> candidates) {
    Preconditions.checkNotNull(candidates);

    return ImmutableSet.copyOf(candidates);
  }

  /**
   * Makes a copy of the argument.
   * 
   * @param chosen chosen
   * @return copied chosen
   */
  public static Set<KnowledgeBaseEntityCandidateSetEntry> copyValues(
      final Set<? extends KnowledgeBaseEntityCandidateSetEntry> chosen) {
    Preconditions.checkNotNull(chosen);

    return ImmutableSet.copyOf(chosen);
  }

  /**
   * Makes a copy of the argument.
   * 
   * @param chosen chosen
   * @return copied chosen
   */
  public static Set<KnowledgeBaseColumnPositionSetEntry> copyPositionValues(
      final Set<? extends KnowledgeBaseColumnPositionSetEntry> chosen) {
    Preconditions.checkNotNull(chosen);

    return ImmutableSet.copyOf(chosen);
  }

  /**
   * Converts from values to domain objects.
   * 
   * @param candidateValues values
   * @return domain objects
   */
  public static Map<String, Set<EntityCandidate>> toDomain(
      final Set<? extends KnowledgeBaseEntityCandidateSetEntry> candidateValues) {
    Preconditions.checkNotNull(candidateValues);

    final ImmutableMap.Builder<String, Set<EntityCandidate>> candidatesBuilder =
        ImmutableMap.builder();
    for (final KnowledgeBaseEntityCandidateSetEntry entry : candidateValues) {
      final String base = entry.getBase();
      final Set<EntityCandidateValue> values = entry.getSet().getValue();

      final Set<EntityCandidate> domainValues = values.stream()
          .map(e -> new EntityCandidate(e.getEntity().toEntity(), e.getScore().toScore()))
          .collect(ImmutableSet.toImmutableSet());

      candidatesBuilder.put(base, domainValues);
    }
    return candidatesBuilder.build();
  }

  /**
   * Converts from values to domain objects.
   * 
   * @param candidateValues values
   * @return domain objects
   */
  public static Map<String, Set<ColumnPosition>> toPositionDomain(
      final Set<? extends KnowledgeBaseColumnPositionSetEntry> candidateValues) {
    Preconditions.checkNotNull(candidateValues);

    final ImmutableMap.Builder<String, Set<ColumnPosition>> candidatesBuilder =
        ImmutableMap.builder();
    for (final KnowledgeBaseColumnPositionSetEntry entry : candidateValues) {
      final String base = entry.getBase();
      final Set<ColumnPositionValue> values = entry.getSet().getValue();

      final Set<ColumnPosition> domainValues = values.stream()
          .map(e -> new ColumnPosition(e.getIndex()))
          .collect(ImmutableSet.toImmutableSet());

      candidatesBuilder.put(base, domainValues);
    }
    return candidatesBuilder.build();
  }

  /**
   * Converts from values to domain objects.
   * 
   * @param candidateValues values
   * @return domain objects
   */
  public static Map<String, NavigableSet<EntityCandidate>> toNavigableDomain(
      final Set<? extends KnowledgeBaseEntityCandidateNavigableSetEntry> candidateValues) {
    Preconditions.checkNotNull(candidateValues);

    final ImmutableMap.Builder<String, NavigableSet<EntityCandidate>> chosenBuilder =
        ImmutableMap.builder();
    for (final KnowledgeBaseEntityCandidateNavigableSetEntry entry : candidateValues) {
      final String base = entry.getBase();
      final Set<EntityCandidateValue> values = entry.getSet().getValue();

      final NavigableSet<EntityCandidate> domainValues = values.stream()
          .map(e -> new EntityCandidate(e.getEntity().toEntity(), e.getScore().toScore())).collect(
              ImmutableSortedSet.toImmutableSortedSet((first, second) -> first.compareTo(second)));

      chosenBuilder.put(base, domainValues);
    }
    return chosenBuilder.build();
  }

  /**
   * Converts from domain objects to values.
   * 
   * @param candidates domain objects
   * @return values
   */
  public static Set<KnowledgeBaseEntityCandidateNavigableSetEntry> toNavigableValues(
      final Map<? extends String, ? extends NavigableSet<? extends EntityCandidate>> candidates) {
    Preconditions.checkNotNull(candidates);

    final ImmutableSet.Builder<KnowledgeBaseEntityCandidateNavigableSetEntry> candidatesBuilder =
        ImmutableSet.builder();
    for (final Map.Entry<? extends String, ? extends NavigableSet<? extends EntityCandidate>> entry : candidates
        .entrySet()) {
      final String base = entry.getKey();
      final NavigableSet<? extends EntityCandidate> baseCandidates = entry.getValue();

      final NavigableSet<EntityCandidateValue> values =
          baseCandidates.stream().map(e -> new EntityCandidateValue(e)).collect(
              ImmutableSortedSet.toImmutableSortedSet((first, second) -> first.compareTo(second)));
      candidatesBuilder.add(new KnowledgeBaseEntityCandidateNavigableSetEntry(
          base, new EntityCandidateNavigableSetWrapper(values)));
    }
    return candidatesBuilder.build();
  }

  /**
   * Converts from domain objects to values.
   * 
   * @param chosen domain objects
   * @return values
   */
  public static Set<KnowledgeBaseEntityCandidateSetEntry> toValues(
      final Map<String, Set<EntityCandidate>> chosen) {
    Preconditions.checkNotNull(chosen);

    final ImmutableSet.Builder<KnowledgeBaseEntityCandidateSetEntry> chosenBuilder =
        ImmutableSet.builder();
    for (final Map.Entry<String, Set<EntityCandidate>> entry : chosen.entrySet()) {
      final String base = entry.getKey();
      final Set<EntityCandidate> baseChosen = entry.getValue();

      final Set<EntityCandidateValue> values = baseChosen.stream()
          .map(e -> new EntityCandidateValue(e)).collect(ImmutableSet.toImmutableSet());
      chosenBuilder.add(new KnowledgeBaseEntityCandidateSetEntry(base,
          new EntityCandidateSetWrapper(values)));
    }
    return chosenBuilder.build();
  }

  /**
   * Converts from domain objects to values.
   * 
   * @param chosen domain objects
   * @return values
   */
  public static Set<KnowledgeBaseColumnPositionSetEntry> toPositionValues(
      final Map<String, Set<ColumnPosition>> chosen) {
    Preconditions.checkNotNull(chosen);

    final ImmutableSet.Builder<KnowledgeBaseColumnPositionSetEntry> chosenBuilder =
        ImmutableSet.builder();
    for (final Map.Entry<String, Set<ColumnPosition>> entry : chosen.entrySet()) {
      final String base = entry.getKey();
      final Set<ColumnPosition> baseChosen = entry.getValue();

      final Set<ColumnPositionValue> values = baseChosen.stream()
          .map(e -> new ColumnPositionValue(e)).collect(ImmutableSet.toImmutableSet());
      chosenBuilder.add(new KnowledgeBaseColumnPositionSetEntry(base,
          new ColumnPositionSetWrapper(values)));
    }
    return chosenBuilder.build();
  }

  private Annotations() {}
}
