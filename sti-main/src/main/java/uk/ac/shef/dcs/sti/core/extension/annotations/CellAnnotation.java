package uk.ac.shef.dcs.sti.core.extension.annotations;

import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;


/**
 * Annotates cell in a table.
 *
 * @author Václav Brodec
 *
 */
@Immutable
public final class CellAnnotation {

  private final NavigableSet<EntityCandidate> candidates;

  private final Set<EntityCandidate> chosen;

  /**
   * Creates new annotation.
   *
   * @param candidates all possible candidates for the assigned entity sorted by with their
   *        likelihood
   * @param chosen subset of candidates chosen to annotate the element
   */
  public CellAnnotation(final Set<? extends EntityCandidate> candidates,
      final Set<? extends EntityCandidate> chosen) {
    Preconditions.checkNotNull(candidates, "The candidates cannot be null!");
    Preconditions.checkNotNull(chosen, "The chosen cannot be null!");
    Preconditions.checkArgument(candidates.containsAll(chosen), "There are some chosen which are not present in the candidates!");

    this.candidates = ImmutableSortedSet.copyOf(candidates);
    this.chosen = ImmutableSet.copyOf(chosen);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
  public NavigableSet<EntityCandidate> getCandidates() {
    return this.candidates;
  }

  /**
   * @return the chosen
   */
  public Set<EntityCandidate> getChosen() {
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
