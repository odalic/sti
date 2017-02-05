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

  public EntityCandidateValue(EntityCandidate adaptee) {
    entity = new EntityValue(adaptee.getEntity());
    score = new ScoreValue(adaptee.getScore());
  }

  /**
   * @return the entity
   */
  @RdfProperty("http://odalic.eu/internal/EntityCandidate/Entity")
  @Nullable
  public EntityValue getEntity() {
    return entity;
  }

  /**
   * @param entity the entity to set
   */
  public void setEntity(EntityValue entity) {
    Preconditions.checkNotNull(entity);

    this.entity = entity;
  }

  /**
   * @return the score
   */
  @RdfProperty("http://odalic.eu/internal/EntityCandidate/Score")
  @Nullable
  public ScoreValue getScore() {
    return score;
  }

  /**
   * @param score the score to set
   */
  public void setScore(ScoreValue score) {
    Preconditions.checkNotNull(score);

    this.score = score;
  }
  
  public EntityCandidate toEntityCandidate() {
    return new EntityCandidate(entity.toEntity(), score.toScore());
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + new EntityCandidate(entity.toEntity(), score.toScore()).hashCode();
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    EntityCandidateValue other = (EntityCandidateValue) obj;
    return new EntityCandidate(entity.toEntity(), score.toScore()).equals(new EntityCandidate(other.entity.toEntity(), other.score.toScore()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(EntityCandidateValue other) {
    final int likelihoodComparison = -1 * score.toScore().compareTo(other.score.toScore());
    if (likelihoodComparison != 0) {
      return likelihoodComparison;
    }

    return entity.toEntity().compareTo(other.entity.toEntity());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityCandidateValue [entity=" + entity + ", score=" + score + "]";
  }
}
