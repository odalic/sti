package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;

/**
 * <p>
 * Domain class {@link EntityCandidate} adapted for RDF serialization.
 * </p>
 *
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/EntityCandidate")
public final class EntityCandidateValue implements Serializable, Comparable<EntityCandidateValue> {

  private static final long serialVersionUID = 3072774254576336747L;

  private EntityValue entity;

  private ScoreValue score;

  public EntityCandidateValue() {}

  public EntityCandidateValue(final EntityCandidate adaptee) {
    this.entity = new EntityValue(adaptee.getEntity());
    this.score = new ScoreValue(adaptee.getScore());
  }

  @Override
  public int compareTo(final EntityCandidateValue other) {
    final int likelihoodComparison = -1 * this.score.toScore().compareTo(other.score.toScore());
    if (likelihoodComparison != 0) {
      return likelihoodComparison;
    }

    return this.entity.toEntity().compareTo(other.entity.toEntity());
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
    final EntityCandidateValue other = (EntityCandidateValue) obj;
    return new EntityCandidate(this.entity.toEntity(), this.score.toScore())
        .equals(new EntityCandidate(other.entity.toEntity(), other.score.toScore()));
  }

  /**
   * @return the entity
   */
  @RdfProperty("http://odalic.eu/internal/EntityCandidate/entity")
  @Nullable
  public EntityValue getEntity() {
    return this.entity;
  }

  /**
   * @return the score
   */
  @RdfProperty("http://odalic.eu/internal/EntityCandidate/score")
  @Nullable
  public ScoreValue getScore() {
    return this.score;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + new EntityCandidate(this.entity.toEntity(), this.score.toScore()).hashCode();
    return result;
  }

  /**
   * @param entity the entity to set
   */
  public void setEntity(final EntityValue entity) {
    Preconditions.checkNotNull(entity);

    this.entity = entity;
  }

  /**
   * @param score the score to set
   */
  public void setScore(final ScoreValue score) {
    Preconditions.checkNotNull(score);

    this.score = score;
  }

  public EntityCandidate toEntityCandidate() {
    return new EntityCandidate(this.entity.toEntity(), this.score.toScore());
  }

  @Override
  public String toString() {
    return "EntityCandidateValue [entity=" + this.entity + ", score=" + this.score + "]";
  }
}
