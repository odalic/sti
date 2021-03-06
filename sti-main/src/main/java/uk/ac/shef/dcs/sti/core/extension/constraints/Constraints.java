package uk.ac.shef.dcs.sti.core.extension.constraints;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import uk.ac.shef.dcs.kbproxy.ProxyResult;
import uk.ac.shef.dcs.kbproxy.Proxy;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition;
import uk.ac.shef.dcs.sti.core.model.EntityResult;

/**
 * User feedback for the result of annotating algorithm. Expresses also input constraints for the
 * next run.
 *
 * @author Václav Brodec
 *
 */
@Immutable
public final class Constraints implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private final Set<ColumnPosition> subjectColumnsPositions;

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
  public Constraints() {
    this.subjectColumnsPositions = ImmutableSet.of();
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
  public Constraints(
      final Set<? extends ColumnPosition> subjectColumnsPositions,
      final Set<? extends ColumnIgnore> columnIgnores,
      final Set<? extends ColumnCompulsory> columnCompulsory,
      final Set<? extends ColumnAmbiguity> columnAmbiguities,
      final Set<? extends Classification> classifications,
      final Set<? extends ColumnRelation> columnRelations,
      final Set<? extends Disambiguation> disambiguations,
      final Set<? extends Ambiguity> ambiguities,
      final Set<? extends DataCubeComponent> dataCubeComponents) {
    Preconditions.checkNotNull(subjectColumnsPositions, "The subjectColumnsPositions cannot be null!");
    Preconditions.checkNotNull(columnIgnores, "The columnIgnores cannot be null!");
    Preconditions.checkNotNull(columnCompulsory, "The columnCompulsory cannot be null!");
    Preconditions.checkNotNull(columnAmbiguities, "The columnAmbiguities cannot be null!");
    Preconditions.checkNotNull(classifications, "The classifications cannot be null!");
    Preconditions.checkNotNull(columnRelations, "The columnRelations cannot be null!");
    Preconditions.checkNotNull(disambiguations, "The disambiguations cannot be null!");
    Preconditions.checkNotNull(ambiguities, "The ambiguities cannot be null!");

    this.subjectColumnsPositions = ImmutableSet.copyOf(subjectColumnsPositions);
    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
    this.columnCompulsory = ImmutableSet.copyOf(columnCompulsory);
    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
    this.classifications = ImmutableSet.copyOf(classifications);
    this.columnRelations = ImmutableSet.copyOf(columnRelations);
    this.disambiguations = ImmutableSet.copyOf(disambiguations);
    this.ambiguities = ImmutableSet.copyOf(ambiguities);
    this.dataCubeComponents = ImmutableSet.copyOf(dataCubeComponents);
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
    final Constraints other = (Constraints) obj;
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
   *
   * @param columnIndex
   * @param rowIndex
   *
   * @return true if there exist entities chosen for disambiguation of given cell
   */
  public boolean existDisambChosenForCell(final int columnIndex, final int rowIndex) {
    return getDisambiguations().stream()
        .anyMatch(e -> (e.getPosition().getColumnIndex() == columnIndex)
            && (e.getPosition().getRowIndex() == rowIndex)
            && !e.getAnnotation().getChosen().isEmpty());
  }

  private ProxyResult<Entity> findOrCreateEntity(final String resource, final String label,
      final Proxy kbProxy) {
    ProxyResult<Entity> entity = kbProxy.loadEntity(resource);

    if (entity == null) {
      entity = new ProxyResult<Entity>(new Entity(resource, label));
    }
    return entity;
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
   *
   * @param columnIndex
   * @param rowIndex
   *
   * @return entities chosen for disambiguation of given cell
   */
  public EntityResult getDisambChosenForCell(final int columnIndex, final int rowIndex,
      final Proxy kbProxy) {
    final List<Entity> entities = new ArrayList<>();
    final List<String> warnings = new ArrayList<>();

    getDisambiguations().stream()
        .filter(e -> (e.getPosition().getColumnIndex() == columnIndex)
            && (e.getPosition().getRowIndex() == rowIndex)
            && !e.getAnnotation().getChosen().isEmpty())
        .forEach(e -> e.getAnnotation().getChosen().forEach(ec -> {
          final ProxyResult<Entity> entityResult =
              findOrCreateEntity(ec.getEntity().getResource(), ec.getEntity().getLabel(), kbProxy);

          entityResult.appendExistingWarning(warnings);
          entities.add(entityResult.getResult());
        }));
    return new EntityResult(entities, warnings);
  }

  /**
   * @return the disambiguations
   */
  public Set<Disambiguation> getDisambiguations() {
    return this.disambiguations;
  }

  /**
   *
   * @param columnIndex column for skipping rows
   * @param rowsCount count of all rows in the column
   *
   * @return indices of rows which will not be disambiguated for given column
   */
  public Set<Integer> getSkipRowsForColumn(final int columnIndex, final int rowsCount) {
    final Set<Integer> skipRows = new HashSet<>();
    if (getColumnAmbiguities().stream().anyMatch(e -> e.getPosition().getIndex() == columnIndex)) {
      // when ColumnAmbiguities contain given columnIndex, all rows will be skipped
      for (int i = 0; i < rowsCount; i++) {
        skipRows.add(i);
      }
    } else {
      // when Ambiguities contain cells with given columnIndex, those rows will be skipped
      getAmbiguities().stream().filter(e -> e.getPosition().getColumnIndex() == columnIndex)
          .forEach(e -> skipRows.add(e.getPosition().getRowIndex()));
      // when Disambiguations contain cells with given columnIndex and empty chosen annotation,
      // those rows will be skipped
      getDisambiguations().stream()
          .filter(e -> (e.getPosition().getColumnIndex() == columnIndex)
              && e.getAnnotation().getChosen().isEmpty())
          .forEach(e -> skipRows.add(e.getPosition().getRowIndex()));
    }
    return skipRows;
  }

  /**
   * @return the subject columns positions
   */
  public Set<ColumnPosition> getSubjectColumnsPositions() {
    return this.subjectColumnsPositions;
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Constraints [subjectColumnsPositions=" + subjectColumnsPositions + ", columnIgnores="
        + columnIgnores + ", columnCompulsory=" + columnCompulsory + ", classifications="
        + classifications + ", columnAmbiguities=" + columnAmbiguities + ", ambiguities="
        + ambiguities + ", disambiguations=" + disambiguations + ", columnRelations="
        + columnRelations + ", dataCubeComponents=" + dataCubeComponents + "]";
  }
}
