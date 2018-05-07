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

  private final boolean postProcessed;

  public EntityCandidate(final Entity entity, final Score score) {
    this(entity, score, false);
  }

  public EntityCandidate(final Entity entity, final Score score, final boolean postProcessed) {
    Preconditions.checkNotNull(entity, "The entity cannot be null!");

    this.entity = entity;
    this.score = score;
    this.postProcessed = postProcessed;
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
  public int compareTo(final EntityCandidate o) {
    final int postProcessedComparison = Boolean.compare(this.postProcessed, o.postProcessed);

    if (postProcessedComparison == 0) {
      final int scoreComparison = this.score.compareTo(o.score);

      if (scoreComparison == 0) {
        return this.entity.compareTo(o.entity);
      } else {
        return scoreComparison;
      }
    } else {
      return postProcessedComparison;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EntityCandidate other = (EntityCandidate) obj;
    if (entity == null) {
      if (other.entity != null) {
        return false;
      }
    } else if (!entity.equals(other.entity)) {
      return false;
    }
    if (postProcessed != other.postProcessed) {
      return false;
    }
    if (score == null) {
      if (other.score != null) {
        return false;
      }
    } else if (!score.equals(other.score)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entity == null) ? 0 : entity.hashCode());
    result = prime * result + (postProcessed ? 1231 : 1237);
    result = prime * result + ((score == null) ? 0 : score.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "EntityCandidate [entity=" + entity + ", score=" + score + ", postProcessed="
        + postProcessed + "]";
  }
}
