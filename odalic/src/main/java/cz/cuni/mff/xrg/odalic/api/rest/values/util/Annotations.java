package cz.cuni.mff.xrg.odalic.api.rest.values.util;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.api.rest.values.EntityCandidateValue;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnPositionValue;

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
  public static Map<String, NavigableSet<EntityCandidateValue>> copyNavigableValues(
      final Map<? extends String, ? extends NavigableSet<? extends EntityCandidateValue>> candidates) {
    final ImmutableMap.Builder<String, NavigableSet<EntityCandidateValue>> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends String, ? extends Set<? extends EntityCandidateValue>> candidateEntry : candidates
        .entrySet()) {
      candidatesBuilder.put(candidateEntry.getKey(),
          ImmutableSortedSet.copyOf(candidateEntry.getValue()));
    }

    return candidatesBuilder.build();
  }

  /**
   * Makes a copy of the argument. 
   * 
   * @param chosen chosen
   * @return copied chosen
   */
  public static Map<String, Set<EntityCandidateValue>> copyValues(
      final Map<? extends String, ? extends Set<? extends EntityCandidateValue>> chosen) {
    final ImmutableMap.Builder<String, Set<EntityCandidateValue>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends String, ? extends Set<? extends EntityCandidateValue>> chosenEntry : chosen
        .entrySet()) {
      chosenBuilder.put(chosenEntry.getKey(), ImmutableSet.copyOf(chosenEntry.getValue()));
    }

    return chosenBuilder.build();
  }

  /**
   * Converts from values to domain objects.
   * 
   * @param chosenValues values
   * @return domain objects
   */
  public static Map<String, Set<EntityCandidate>> toDomain(
      final Map<? extends String, ? extends Set<? extends EntityCandidateValue>> chosenValues) {
    final ImmutableMap.Builder<String, Set<EntityCandidate>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends String, ? extends Set<? extends EntityCandidateValue>> entry : chosenValues
        .entrySet()) {
      final String baseName = entry.getKey();
      final Set<? extends EntityCandidateValue> values = entry.getValue();

      chosenBuilder.put(baseName, ImmutableSet.copyOf(
          values.stream().map(e -> new EntityCandidate(e.getEntity(), e.getScore())).iterator()));
    }
    final Map<String, Set<EntityCandidate>> chosen = chosenBuilder.build();
    return chosen;
  }
  
  /**
   * Converts from values to domain objects.
   * 
   * @param chosenValues values
   * @return domain objects
   */
  public static Map<String, Set<ColumnPosition>> toColumnPositionsDomain(
      final Map<? extends String, ? extends Set<? extends ColumnPositionValue>> chosenValues) {
    final ImmutableMap.Builder<String, Set<ColumnPosition>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends String, ? extends Set<? extends ColumnPositionValue>> entry : chosenValues
        .entrySet()) {
      final String baseName = entry.getKey();
      final Set<? extends ColumnPositionValue> values = entry.getValue();

      chosenBuilder.put(baseName, ImmutableSet.copyOf(
          values.stream().map(e -> new ColumnPosition(e.getIndex())).iterator()));
    }
    final Map<String, Set<ColumnPosition>> chosen = chosenBuilder.build();
    return chosen;
  }

  /**
   * Converts from values to domain objects.
   * 
   * @param candidateValues values
   * @return domain objects
   */
  public static Map<String, NavigableSet<EntityCandidate>> toNavigableDomain(
      final Map<? extends String, ? extends NavigableSet<? extends EntityCandidateValue>> candidateValues) {
    final ImmutableMap.Builder<String, NavigableSet<EntityCandidate>> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends String, ? extends NavigableSet<? extends EntityCandidateValue>> entry : candidateValues
        .entrySet()) {
      final String baseName = entry.getKey();
      final Set<? extends EntityCandidateValue> values = entry.getValue();

      candidatesBuilder.put(baseName, ImmutableSortedSet.copyOf(
          values.stream().map(e -> new EntityCandidate(e.getEntity(), e.getScore())).iterator()));
    }
    final Map<String, NavigableSet<EntityCandidate>> candidates = candidatesBuilder.build();
    return candidates;
  }

  /**
   * Converts from domain objects to values.
   * 
   * @param candidates domain objects
   * @return values
   */
  public static Map<String, NavigableSet<EntityCandidateValue>> toNavigableValues(
      final Map<? extends String, ? extends NavigableSet<? extends EntityCandidate>> candidates) {
    final ImmutableMap.Builder<String, NavigableSet<EntityCandidateValue>> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends String, ? extends NavigableSet<? extends EntityCandidate>> entry : candidates
        .entrySet()) {
      final String baseName = entry.getKey();
      final NavigableSet<? extends EntityCandidate> baseCandidates = entry.getValue();

      final NavigableSet<EntityCandidateValue> values =
          baseCandidates.stream().map(e -> new EntityCandidateValue(e)).collect(
              ImmutableSortedSet.toImmutableSortedSet((first, second) -> first.compareTo(second)));
      candidatesBuilder.put(baseName, ImmutableSortedSet.copyOf(values));
    }
    return candidatesBuilder.build();
  }

  /**
   * Converts from domain objects to values.
   * 
   * @param chosen domain objects
   * @return values
   */
  public static Map<String, Set<EntityCandidateValue>> toValues(
      final Map<String, Set<EntityCandidate>> chosen) {
    final ImmutableMap.Builder<String, Set<EntityCandidateValue>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<String, Set<EntityCandidate>> entry : chosen.entrySet()) {
      final String baseName = entry.getKey();
      final Set<EntityCandidate> baseChosen = entry.getValue();

      final Set<EntityCandidateValue> values = baseChosen.stream()
          .map(e -> new EntityCandidateValue(e)).collect(ImmutableSet.toImmutableSet());
      chosenBuilder.put(baseName, values);
    }
    return chosenBuilder.build();
  }
  
  /**
   * Converts from domain objects to values.
   * 
   * @param chosen domain objects
   * @return values
   */
  public static Map<String, Set<ColumnPositionValue>> toColumnPositionValues(
      final Map<String, Set<ColumnPosition>> chosen) {
    final ImmutableMap.Builder<String, Set<ColumnPositionValue>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<String, Set<ColumnPosition>> entry : chosen.entrySet()) {
      final String baseName = entry.getKey();
      final Set<ColumnPosition> baseChosen = entry.getValue();

      final Set<ColumnPositionValue> values = baseChosen.stream()
          .map(e -> new ColumnPositionValue(e)).collect(ImmutableSet.toImmutableSet());
      chosenBuilder.put(baseName, values);
    }
    return chosenBuilder.build();
  }

  private Annotations() {}

}
