package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.CellAnnotationAdapter;


/**
 * Annotates cell in a table.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(CellAnnotationAdapter.class)
public final class CellAnnotation implements Serializable {

  private static final long serialVersionUID = 2348206296236512507L;

  private final Map<String, NavigableSet<EntityCandidate>> candidates;

  private final Map<String, Set<EntityCandidate>> chosen;

  /**
   * Creates new annotation.
   *
   * @param candidates all possible candidates for the assigned entity sorted by with their score
   * @param chosen subset of candidates chosen to annotate the element
   */
  public CellAnnotation(
      final Map<? extends String, ? extends Set<? extends EntityCandidate>> candidates,
      final Map<? extends String, ? extends Set<? extends EntityCandidate>> chosen) {
    Preconditions.checkNotNull(candidates);
    Preconditions.checkNotNull(chosen);

    final ImmutableMap.Builder<String, NavigableSet<EntityCandidate>> candidatesBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends String, ? extends Set<? extends EntityCandidate>> candidateEntry : candidates
        .entrySet()) {
      candidatesBuilder.put(candidateEntry.getKey(),
          ImmutableSortedSet.copyOf(candidateEntry.getValue()));
    }
    this.candidates = candidatesBuilder.build();

    final ImmutableMap.Builder<String, Set<EntityCandidate>> chosenBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends String, ? extends Set<? extends EntityCandidate>> chosenEntry : chosen
        .entrySet()) {
      final String chosenBaseName = chosenEntry.getKey();

      final Set<EntityCandidate> baseCandidates = this.candidates.get(chosenBaseName);
      Preconditions.checkArgument(baseCandidates != null);
      Preconditions.checkArgument(baseCandidates.containsAll(chosenEntry.getValue()));

      chosenBuilder.put(chosenEntry.getKey(), ImmutableSet.copyOf(chosenEntry.getValue()));
    }
    this.chosen = chosenBuilder.build();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CellAnnotation other = (CellAnnotation) obj;
    if (this.candidates == null) {
      if (other.candidates != null) {
        return false;
      }
    } else if (!this.candidates.equals(other.candidates)) {
      return false;
    }
    if (this.chosen == null) {
      if (other.chosen != null) {
        return false;
      }
    } else if (!this.chosen.equals(other.chosen)) {
      return false;
    }
    return true;
  }

  /**
   * @return the candidates
   */
  public Map<String, NavigableSet<EntityCandidate>> getCandidates() {
    return this.candidates;
  }

  /**
   * @return the chosen
   */
  public Map<String, Set<EntityCandidate>> getChosen() {
    return this.chosen;
  }

  /**
   * Computes hash code based on the candidates and the chosen.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.candidates == null) ? 0 : this.candidates.hashCode());
    result = (prime * result) + ((this.chosen == null) ? 0 : this.chosen.hashCode());
    return result;
  }

  /**
   * Merges with the other annotation.
   *
   * @param other annotation based on different set of knowledge bases
   * @return merged annotation
   * @throws IllegalArgumentException If both this and the other annotation have some candidates
   *         from the same knowledge base
   */
  public CellAnnotation merge(final CellAnnotation other) throws IllegalArgumentException {
    final ImmutableMap.Builder<String, NavigableSet<EntityCandidate>> candidatesBuilder =
        ImmutableMap.builder();
    candidatesBuilder.putAll(this.candidates);
    candidatesBuilder.putAll(other.candidates);

    final ImmutableMap.Builder<String, Set<EntityCandidate>> chosenBuilder =
        ImmutableMap.builder();
    chosenBuilder.putAll(this.chosen);
    chosenBuilder.putAll(other.chosen);

    return new CellAnnotation(candidatesBuilder.build(), chosenBuilder.build());
  }

  /**
   * Compares for equality (only other annotation of the same kind with equally ordered set of
   * candidates and the same chosen set passes).
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public String toString() {
    return "CellAnnotation [candidates=" + this.candidates + ", chosen=" + this.chosen + "]";
  }
}
