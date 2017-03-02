package uk.ac.shef.dcs.sti.core.extension.annotations;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * Encapsulates annotating entity and the score that is assigned to it.
 *
 * @author Václav Brodec
 *
 */
@Immutable
public final class EntityCandidate implements Comparable<EntityCandidate>, Serializable {

  private static final long serialVersionUID = 3072774254576336747L;

  private final Entity entity;

  private final Score score;

  /**
   * @param entity
   * @param score
   */
  public EntityCandidate(final Entity entity, final Score score) {
    Preconditions.checkNotNull(entity);

    this.entity = entity;
    this.score = score;
  }

  /**
   * Entity candidates are naturally ordered by their score in ascending order. In case of the equal
   * score the natural ordering of entities is taken into account.
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final EntityCandidate o) {
    final int likelihoodComparison = this.score.compareTo(o.score);

    if (likelihoodComparison == 0) {
      return this.entity.compareTo(o.entity);
    } else {
      return likelihoodComparison;
    }
  }

  /**
   * Compares for equality (only other candidates entity with the same score passes).
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
    final EntityCandidate other = (EntityCandidate) obj;
    if (this.entity == null) {
      if (other.entity != null) {
        return false;
      }
    } else if (!this.entity.equals(other.entity)) {
      return false;
    }
    if (this.score == null) {
      if (other.score != null) {
        return false;
      }
    } else if (!this.score.equals(other.score)) {
      return false;
    }
    return true;
  }

  /**
   * @return the entity
   */
  public Entity getEntity() {
    return this.entity;
  }

  /**
   * @return the score
   */
  public Score getScore() {
    return this.score;
  }

  /**
   * Computes hash code based on the entity and the score.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.entity == null) ? 0 : this.entity.hashCode());
    result = (prime * result) + ((this.score == null) ? 0 : this.score.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityCandidate [entity=" + this.entity + ", score=" + this.score + "]";
  }
}
