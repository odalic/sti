package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;

/**
 * <p>
 * Domain class {@link EntityCandidate} adapted for REST API.
 * </p>
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "entityCandidate")
public final class EntityCandidateValue implements Serializable, Comparable<EntityCandidateValue> {

  private static final long serialVersionUID = 3072774254576336747L;

  private Entity entity;

  private Score score;

  public EntityCandidateValue() {}

  public EntityCandidateValue(final EntityCandidate adaptee) {
    this.entity = adaptee.getEntity();
    this.score = adaptee.getScore();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final EntityCandidateValue other) {
    final int likelihoodComparison = -1 * this.score.compareTo(other.score);
    if (likelihoodComparison != 0) {
      return likelihoodComparison;
    }

    return this.entity.compareTo(other.entity);
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
    final EntityCandidateValue other = (EntityCandidateValue) obj;
    return new EntityCandidate(this.entity, this.score)
        .equals(new EntityCandidate(other.entity, other.score));
  }

  /**
   * @return the entity
   */
  @XmlElement
  @Nullable
  public Entity getEntity() {
    return this.entity;
  }

  /**
   * @return the score
   */
  @XmlElement
  @Nullable
  public Score getScore() {
    return this.score;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + new EntityCandidate(this.entity, this.score).hashCode();
    return result;
  }

  /**
   * @param entity the entity to set
   */
  public void setEntity(final Entity entity) {
    Preconditions.checkNotNull(entity);

    this.entity = entity;
  }

  /**
   * @param score the score to set
   */
  public void setScore(final Score score) {
    Preconditions.checkNotNull(score);

    this.score = score;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityCandidateValue [entity=" + this.entity + ", score=" + this.score + "]";
  }
}
