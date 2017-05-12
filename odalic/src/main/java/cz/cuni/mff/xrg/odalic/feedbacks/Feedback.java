package cz.cuni.mff.xrg.odalic.feedbacks;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.FeedbackAdapter;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

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


  private final Map<String, Set<ColumnPosition>> subjectColumnsPositions;

  private final Set<ColumnIgnore> columnIgnores;

  private final Set<ColumnCompulsory> columnCompulsory;

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
    this.subjectColumnsPositions = ImmutableMap.of();
    this.columnIgnores = ImmutableSet.of();
    this.columnCompulsory = ImmutableSet.of();
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
   * @param subjectColumnsPositions positions of the subject columns
   * @param columnIgnores ignored columns
   * @param columnCompulsory compulsory columns
   * @param columnAmbiguities columns whose cells will not be disambiguated
   * @param classifications classification hints for columns
   * @param columnRelations hints with relation between columns
   * @param disambiguations custom disambiguations
   * @param ambiguities hints for cells to be left ambiguous
   * @param dataCubeComponents dataCubeComponents hints for columns
   */
  public Feedback(
      final Map<? extends String, Set<ColumnPosition>> subjectColumnsPositions,
      final Set<? extends ColumnIgnore> columnIgnores,
      final Set<? extends ColumnCompulsory> columnCompulsory,
      final Set<? extends ColumnAmbiguity> columnAmbiguities,
      final Set<? extends Classification> classifications,
      final Set<? extends ColumnRelation> columnRelations,
      final Set<? extends Disambiguation> disambiguations,
      final Set<? extends Ambiguity> ambiguities,
      final Set<? extends DataCubeComponent> dataCubeComponents) {
    Preconditions.checkNotNull(subjectColumnsPositions);
    Preconditions.checkNotNull(columnIgnores);
    Preconditions.checkNotNull(columnCompulsory);
    Preconditions.checkNotNull(columnAmbiguities);
    Preconditions.checkNotNull(classifications);
    Preconditions.checkNotNull(columnRelations);
    Preconditions.checkNotNull(disambiguations);
    Preconditions.checkNotNull(ambiguities);

    this.subjectColumnsPositions = ImmutableMap.copyOf(subjectColumnsPositions);
    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
    this.columnCompulsory = ImmutableSet.copyOf(columnCompulsory);
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
    if (this.columnCompulsory == null) {
      if (other.columnCompulsory != null) {
        return false;
      }
    } else if (!this.columnCompulsory.equals(other.columnCompulsory)) {
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
    if (this.subjectColumnsPositions == null) {
      if (other.subjectColumnsPositions != null) {
        return false;
      }
    } else if (!this.subjectColumnsPositions.equals(other.subjectColumnsPositions)) {
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
   * @return compulsory columns
   */
  public Set<ColumnCompulsory> getColumnCompulsory() {
    return this.columnCompulsory;
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
   * @return the subject columns positions
   */
  public Map<String, Set<ColumnPosition>> getSubjectColumnsPositions() {
    return this.subjectColumnsPositions;
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
    result = (prime * result) + ((this.columnCompulsory == null) ? 0 : this.columnCompulsory.hashCode());
    result =
        (prime * result) + ((this.columnRelations == null) ? 0 : this.columnRelations.hashCode());
    result =
        (prime * result) + ((this.disambiguations == null) ? 0 : this.disambiguations.hashCode());
    result = (prime * result)
        + ((this.dataCubeComponents == null) ? 0 : this.dataCubeComponents.hashCode());
    result = (prime * result)
        + ((this.subjectColumnsPositions == null) ? 0 : this.subjectColumnsPositions.hashCode());
    return result;
  }

  private void checkConflicts() {
    // check the conflict when ignore columns contain some subject column
    for (final String baseName : this.subjectColumnsPositions.keySet()) {
      final Set<ColumnPosition> subjectPositions = this.subjectColumnsPositions.get(baseName);
      if (subjectPositions == null) {
        continue;
      }

      for (ColumnPosition subjCol : subjectPositions) {
        if (this.columnIgnores.stream()
            .anyMatch(e -> e.getPosition().getIndex() == subjCol.getIndex())) {
          throw new IllegalArgumentException("The column (position: " + subjCol.getIndex()
              + ") which is ignored does not have to be a subject column.");
        }

        for (final Classification classification : this.classifications) {
          if ((classification.getPosition().getIndex() == subjCol.getIndex())
              && (classification.getAnnotation().getChosen().get(baseName) != null)
              && classification.getAnnotation().getChosen().get(baseName).isEmpty()) {
            throw new IllegalArgumentException("The column (position: " + subjCol.getIndex()
                + ") which has empty chosen classification set (for " + baseName
                + " KB) does not have to be a subject column (for that KB).");
          }
        }
      }
    }

    // check the conflict when ignore columns contain some compulsory column
    for (ColumnCompulsory compCol : this.columnCompulsory) {
      if (this.columnIgnores.stream()
          .anyMatch(e -> e.getPosition().getIndex() == compCol.getPosition().getIndex())) {
        throw new IllegalArgumentException("The column (position: " + compCol.getPosition().getIndex()
            + ") which is ignored does not have to be a compulsory column.");
      }
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Feedback [subjectColumnsPositions=" + subjectColumnsPositions + ", columnIgnores="
        + columnIgnores + ", columnCompulsory=" + columnCompulsory + ", classifications="
        + classifications + ", columnAmbiguities=" + columnAmbiguities + ", ambiguities="
        + ambiguities + ", disambiguations=" + disambiguations + ", columnRelations="
        + columnRelations + ", dataCubeComponents=" + dataCubeComponents + "]";
  }
}
