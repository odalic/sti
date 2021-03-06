package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.EntityCandidateAdapter;

/**
 * Encapsulates annotating entity and the score that is assigned to it.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(EntityCandidateAdapter.class)
public final class EntityCandidate implements Comparable<EntityCandidate>, Serializable {

  private static final long serialVersionUID = 3072774254576336747L;

  private final Entity entity;

  private final Score score;

  public EntityCandidate(final Entity entity, final Score score) {
    Preconditions.checkNotNull(entity, "The entity cannot be null!");

    this.entity = entity;
    this.score = score;
  }

 @Override
  public int compareTo(final EntityCandidate o) {
    final int scoreComparison = this.score.compareTo(o.score);

    if (scoreComparison == 0) {
      return this.entity.compareTo(o.entity);
    } else {
      return scoreComparison;
    }
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.entity == null) ? 0 : this.entity.hashCode());
    result = (prime * result) + ((this.score == null) ? 0 : this.score.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "EntityCandidate [entity=" + this.entity + ", score=" + this.score + "]";
  }
}
