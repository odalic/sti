package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.FeedbackAdapter;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * User feedback for the result of annotating algorithm. Expresses also input constraints for the
 * next run.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(FeedbackAdapter.class)
public final class Feedback implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private final Map<KnowledgeBase, ColumnPosition> subjectColumnPositions;

  private final Map<KnowledgeBase, Set<ColumnPosition>> otherSubjectColumnPositions;

  private final Set<ColumnIgnore> columnIgnores;

  private final Set<Classification> classifications;

  private final Set<ColumnAmbiguity> columnAmbiguities;

  private final Set<Ambiguity> ambiguities;

  private final Set<Disambiguation> disambiguations;

  private final Set<ColumnRelation> columnRelations;

  private final Set<DataCubeComponent> dataCubeComponents;


  /**
   * Creates empty feedback.
   */
  public Feedback() {
    this.subjectColumnPositions = ImmutableMap.of();
    this.otherSubjectColumnPositions = ImmutableMap.of();
    this.columnIgnores = ImmutableSet.of();
    this.columnAmbiguities = ImmutableSet.of();
    this.classifications = ImmutableSet.of();
    this.columnRelations = ImmutableSet.of();
    this.disambiguations = ImmutableSet.of();
    this.ambiguities = ImmutableSet.of();
    this.dataCubeComponents = ImmutableSet.of();
  }

  /**
   * Creates feedback.
   *
   * @param subjectColumnPositions positions of the subject columns
   * @param otherSubjectColumnPositions positions of the other subject columns
   * @param columnIgnores ignored columns
   * @param columnAmbiguities columns whose cells will not be disambiguated
   * @param classifications classification hints for columns
   * @param columnRelations hints with relation between columns
   * @param disambiguations custom disambiguations
   * @param ambiguities hints for cells to be left ambiguous
   * @param dataCubeComponents dataCubeComponents hints for columns
   */
  public Feedback(
      final Map<? extends KnowledgeBase, ? extends ColumnPosition> subjectColumnPositions,
      final Map<? extends KnowledgeBase, Set<ColumnPosition>> otherSubjectColumnPositions,
      final Set<? extends ColumnIgnore> columnIgnores,
      final Set<? extends ColumnAmbiguity> columnAmbiguities,
      final Set<? extends Classification> classifications,
      final Set<? extends ColumnRelation> columnRelations,
      final Set<? extends Disambiguation> disambiguations,
      final Set<? extends Ambiguity> ambiguities,
      final Set<? extends DataCubeComponent> dataCubeComponents) {
    Preconditions.checkNotNull(columnIgnores);
    Preconditions.checkNotNull(columnAmbiguities);
    Preconditions.checkNotNull(classifications);
    Preconditions.checkNotNull(columnRelations);
    Preconditions.checkNotNull(disambiguations);
    Preconditions.checkNotNull(ambiguities);

    this.subjectColumnPositions = ImmutableMap.copyOf(subjectColumnPositions);
    this.otherSubjectColumnPositions = ImmutableMap.copyOf(otherSubjectColumnPositions);
    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
    this.classifications = ImmutableSet.copyOf(classifications);
    this.columnRelations = ImmutableSet.copyOf(columnRelations);
    this.disambiguations = ImmutableSet.copyOf(disambiguations);
    this.ambiguities = ImmutableSet.copyOf(ambiguities);
    this.dataCubeComponents = ImmutableSet.copyOf(dataCubeComponents);

    checkConflicts();
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
    final Feedback other = (Feedback) obj;
    if (this.ambiguities == null) {
      if (other.ambiguities != null) {
        return false;
      }
    } else if (!this.ambiguities.equals(other.ambiguities)) {
      return false;
    }
    if (this.classifications == null) {
      if (other.classifications != null) {
        return false;
      }
    } else if (!this.classifications.equals(other.classifications)) {
      return false;
    }
    if (this.columnAmbiguities == null) {
      if (other.columnAmbiguities != null) {
        return false;
      }
    } else if (!this.columnAmbiguities.equals(other.columnAmbiguities)) {
      return false;
    }
    if (this.columnIgnores == null) {
      if (other.columnIgnores != null) {
        return false;
      }
    } else if (!this.columnIgnores.equals(other.columnIgnores)) {
      return false;
    }
    if (this.columnRelations == null) {
      if (other.columnRelations != null) {
        return false;
      }
    } else if (!this.columnRelations.equals(other.columnRelations)) {
      return false;
    }
    if (this.disambiguations == null) {
      if (other.disambiguations != null) {
        return false;
      }
    } else if (!this.disambiguations.equals(other.disambiguations)) {
      return false;
    }
    if (this.dataCubeComponents == null) {
      if (other.dataCubeComponents != null) {
        return false;
      }
    } else if (!this.dataCubeComponents.equals(other.dataCubeComponents)) {
      return false;
    }
    if (this.subjectColumnPositions == null) {
      if (other.subjectColumnPositions != null) {
        return false;
      }
    } else if (!this.subjectColumnPositions.equals(other.subjectColumnPositions)) {
      return false;
    }
    if (this.otherSubjectColumnPositions == null) {
      if (other.otherSubjectColumnPositions != null) {
        return false;
      }
    } else if (!this.otherSubjectColumnPositions.equals(other.otherSubjectColumnPositions)) {
      return false;
    }
    return true;
  }

  /**
   * @return the forced ambiguous cells
   */
  public Set<Ambiguity> getAmbiguities() {
    return this.ambiguities;
  }

  /**
   * @return the classifications
   */
  public Set<Classification> getClassifications() {
    return this.classifications;
  }

  /**
   * @return ambiguous columns
   */
  public Set<ColumnAmbiguity> getColumnAmbiguities() {
    return this.columnAmbiguities;
  }

  /**
   * @return ignored columns
   */
  public Set<ColumnIgnore> getColumnIgnores() {
    return this.columnIgnores;
  }

  /**
   * @return the column relations
   */
  public Set<ColumnRelation> getColumnRelations() {
    return this.columnRelations;
  }

  /**
   * @return the dataCubeComponents
   */
  public Set<DataCubeComponent> getDataCubeComponents() {
    return this.dataCubeComponents;
  }

  /**
   * @return the disambiguations
   */
  public Set<Disambiguation> getDisambiguations() {
    return this.disambiguations;
  }

  /**
   * @return the subject column position
   */
  @Nullable
  public Map<KnowledgeBase, ColumnPosition> getSubjectColumnPositions() {
    return this.subjectColumnPositions;
  }

  /**
   * @return the other subject column positions
   */
  @Nullable
  public Map<KnowledgeBase, Set<ColumnPosition>> getOtherSubjectColumnPositions() {
    return this.otherSubjectColumnPositions;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.ambiguities == null) ? 0 : this.ambiguities.hashCode());
    result =
        (prime * result) + ((this.classifications == null) ? 0 : this.classifications.hashCode());
    result = (prime * result)
        + ((this.columnAmbiguities == null) ? 0 : this.columnAmbiguities.hashCode());
    result = (prime * result) + ((this.columnIgnores == null) ? 0 : this.columnIgnores.hashCode());
    result =
        (prime * result) + ((this.columnRelations == null) ? 0 : this.columnRelations.hashCode());
    result =
        (prime * result) + ((this.disambiguations == null) ? 0 : this.disambiguations.hashCode());
    result = (prime * result)
        + ((this.dataCubeComponents == null) ? 0 : this.dataCubeComponents.hashCode());
    result = (prime * result)
        + ((this.subjectColumnPositions == null) ? 0 : this.subjectColumnPositions.hashCode());
    result = (prime * result)
        + ((this.otherSubjectColumnPositions == null) ? 0 : this.otherSubjectColumnPositions.hashCode());
    return result;
  }

  private void checkConflicts() {
    // check the conflict when ignore columns contain the subject column
    if (this.subjectColumnPositions == null) {
      return;
    }

    for (final KnowledgeBase base : this.subjectColumnPositions.keySet()) {
      final ColumnPosition subjCol = this.subjectColumnPositions.get(base);
      if (subjCol == null) {
        return;
      }

      if (this.columnIgnores.stream()
          .anyMatch(e -> e.getPosition().getIndex() == subjCol.getIndex())) {
        throw new IllegalArgumentException("The column (position: " + subjCol.getIndex()
            + ") which is ignored does not have to be a subject column.");
      }

      for (final Classification classification : this.classifications) {
        if ((classification.getPosition().getIndex() == subjCol.getIndex())
            && (classification.getAnnotation().getChosen().get(base) != null)
            && classification.getAnnotation().getChosen().get(base).isEmpty()) {
          throw new IllegalArgumentException("The column (position: " + subjCol.getIndex()
              + ") which has empty chosen classification set (for " + base.getName()
              + " KB) does not have to be a subject column (for that KB).");
        }
      }
    }
  }

  @Override
  public String toString() {
    return "Feedback [subjectColumnPositions=" + this.subjectColumnPositions
        + ", otherSubjectColumnPositions=" + this.otherSubjectColumnPositions + ", columnIgnores="
        + this.columnIgnores + ", columnAmbiguities=" + this.columnAmbiguities
        + ", classifications=" + this.classifications + ", columnRelations=" + this.columnRelations
        + ", disambiguations=" + this.disambiguations + ", ambiguities=" + this.ambiguities
        + ", dataCubeComponents=" + this.dataCubeComponents + "]";
  }
}
