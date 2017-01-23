package uk.ac.shef.dcs.sti.core.extension.constraints;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.extension.positions.ColumnPosition;

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

  private final ColumnPosition subjectColumnPosition;

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
  public Constraints() {
    this.subjectColumnPosition = null;
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
   * @param subjectColumnPosition position of the subject column (optional)
   * @param columnIgnores ignored columns
   * @param columnAmbiguities columns whose cells will not be disambiguated
   * @param classifications classification hints for columns
   * @param columnRelations hints with relation between columns
   * @param disambiguations custom disambiguations
   * @param ambiguities hints for cells to be left ambiguous
   * @param dataCubeComponents dataCubeComponents hints for columns
   */
  public Constraints(@Nullable ColumnPosition subjectColumnPosition,
      Set<? extends ColumnIgnore> columnIgnores, Set<? extends ColumnAmbiguity> columnAmbiguities,
      Set<? extends Classification> classifications, Set<? extends ColumnRelation> columnRelations,
      Set<? extends Disambiguation> disambiguations, Set<? extends Ambiguity> ambiguities,
      Set<? extends DataCubeComponent> dataCubeComponents) {
    Preconditions.checkNotNull(columnIgnores);
    Preconditions.checkNotNull(columnAmbiguities);
    Preconditions.checkNotNull(classifications);
    Preconditions.checkNotNull(columnRelations);
    Preconditions.checkNotNull(disambiguations);
    Preconditions.checkNotNull(ambiguities);

    this.subjectColumnPosition = subjectColumnPosition;
    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
    this.classifications = ImmutableSet.copyOf(classifications);
    this.columnRelations = ImmutableSet.copyOf(columnRelations);
    this.disambiguations = ImmutableSet.copyOf(disambiguations);
    this.ambiguities = ImmutableSet.copyOf(ambiguities);
    this.dataCubeComponents = ImmutableSet.copyOf(dataCubeComponents);
  }

  /**
   * @return the subject column position
   */
  @Nullable
  public ColumnPosition getSubjectColumnPosition() {
    return subjectColumnPosition;
  }

  /**
   * @return ignored columns
   */
  public Set<ColumnIgnore> getColumnIgnores() {
    return columnIgnores;
  }

  /**
   * @return ambiguous columns
   */
  public Set<ColumnAmbiguity> getColumnAmbiguities() {
    return columnAmbiguities;
  }

  /**
   * @return the classifications
   */
  public Set<Classification> getClassifications() {
    return classifications;
  }

  /**
   * @return the column relations
   */
  public Set<ColumnRelation> getColumnRelations() {
    return columnRelations;
  }

  /**
   * @return the disambiguations
   */
  public Set<Disambiguation> getDisambiguations() {
    return disambiguations;
  }

  /**
   * @return the forced ambiguous cells
   */
  public Set<Ambiguity> getAmbiguities() {
    return ambiguities;
  }

  /**
   * @return the dataCubeComponents
   */
  public Set<DataCubeComponent> getDataCubeComponents() {
    return dataCubeComponents;
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
    result = prime * result + ((ambiguities == null) ? 0 : ambiguities.hashCode());
    result = prime * result + ((classifications == null) ? 0 : classifications.hashCode());
    result = prime * result + ((columnAmbiguities == null) ? 0 : columnAmbiguities.hashCode());
    result = prime * result + ((columnIgnores == null) ? 0 : columnIgnores.hashCode());
    result = prime * result + ((columnRelations == null) ? 0 : columnRelations.hashCode());
    result = prime * result + ((disambiguations == null) ? 0 : disambiguations.hashCode());
    result = prime * result + ((dataCubeComponents == null) ? 0 : dataCubeComponents.hashCode());
    result =
        prime * result + ((subjectColumnPosition == null) ? 0 : subjectColumnPosition.hashCode());
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
    Constraints other = (Constraints) obj;
    if (ambiguities == null) {
      if (other.ambiguities != null) {
        return false;
      }
    } else if (!ambiguities.equals(other.ambiguities)) {
      return false;
    }
    if (classifications == null) {
      if (other.classifications != null) {
        return false;
      }
    } else if (!classifications.equals(other.classifications)) {
      return false;
    }
    if (columnAmbiguities == null) {
      if (other.columnAmbiguities != null) {
        return false;
      }
    } else if (!columnAmbiguities.equals(other.columnAmbiguities)) {
      return false;
    }
    if (columnIgnores == null) {
      if (other.columnIgnores != null) {
        return false;
      }
    } else if (!columnIgnores.equals(other.columnIgnores)) {
      return false;
    }
    if (columnRelations == null) {
      if (other.columnRelations != null) {
        return false;
      }
    } else if (!columnRelations.equals(other.columnRelations)) {
      return false;
    }
    if (disambiguations == null) {
      if (other.disambiguations != null) {
        return false;
      }
    } else if (!disambiguations.equals(other.disambiguations)) {
      return false;
    }
    if (dataCubeComponents == null) {
      if (other.dataCubeComponents != null) {
        return false;
      }
    } else if (!dataCubeComponents.equals(other.dataCubeComponents)) {
      return false;
    }
    if (subjectColumnPosition == null) {
      if (other.subjectColumnPosition != null) {
        return false;
      }
    } else if (!subjectColumnPosition.equals(other.subjectColumnPosition)) {
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Constraints [subjectColumnPosition=" + subjectColumnPosition + ", columnIgnores="
        + columnIgnores + ", columnAmbiguities=" + columnAmbiguities + ", classifications="
        + classifications + ", columnRelations=" + columnRelations + ", disambiguations="
        + disambiguations + ", ambiguities=" + ambiguities + ", dataCubeComponents="
        + dataCubeComponents + "]";
  }

  /**
   * 
   * @param columnIndex
   * @param rowIndex
   * 
   * @return entities chosen for disambiguation of given cell
   */
  public List<Entity> getDisambChosenForCell(int columnIndex, int rowIndex) {
    List<Entity> entities = new ArrayList<>();
    getDisambiguations().stream().filter(e -> e.getPosition().getColumnIndex() == columnIndex &&
        e.getPosition().getRowIndex() == rowIndex && !e.getAnnotation().getChosen().isEmpty())
      .forEach(e -> e.getAnnotation().getChosen()
          .forEach(ec -> entities.add(new Entity(ec.getEntity().getResource(), ec.getEntity().getLabel()))));
    return entities;
  }

  /**
   * 
   * @param columnIndex
   * @param rowIndex
   * 
   * @return true if there exist entities chosen for disambiguation of given cell
   */
  public boolean existDisambChosenForCell(int columnIndex, int rowIndex) {
    return getDisambiguations().stream().anyMatch(e -> e.getPosition().getColumnIndex() == columnIndex &&
        e.getPosition().getRowIndex() == rowIndex && !e.getAnnotation().getChosen().isEmpty());
  }

  /**
   * 
   * @param columnIndex column for skipping rows
   * @param rowsCount count of all rows in the column
   * 
   * @return indices of rows which will not be disambiguated for given column
   */
  public Set<Integer> getSkipRowsForColumn(int columnIndex, int rowsCount) {
    Set<Integer> skipRows = new HashSet<>();
    if (getColumnAmbiguities().stream().anyMatch(e -> e.getPosition().getIndex() == columnIndex)) {
      // when ColumnAmbiguities contain given columnIndex, all rows will be skipped
      for (int i = 0; i < rowsCount; i++) {
        skipRows.add(i);
      }
    }
    else {
      // when Ambiguities contain cells with given columnIndex, those rows will be skipped
      getAmbiguities().stream()
        .filter(e -> e.getPosition().getColumnIndex() == columnIndex)
        .forEach(e -> skipRows.add(e.getPosition().getRowIndex()));
      // when Disambiguations contain cells with given columnIndex and empty chosen annotation, those rows will be skipped
      getDisambiguations().stream()
        .filter(e -> e.getPosition().getColumnIndex() == columnIndex && e.getAnnotation().getChosen().isEmpty())
        .forEach(e -> skipRows.add(e.getPosition().getRowIndex()));
    }
    return skipRows;
  }
}
