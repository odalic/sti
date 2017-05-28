package cz.cuni.mff.xrg.odalic.tasks.results;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ResultAdapter;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnProcessingAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;

/**
 * <p>
 * This class represents a partial result of the table annotation process.
 * </p>
 *
 * <p>
 * It includes all the data necessary to produce the final triples and also serves as the base for
 * user-defined hints to the annotating algorithm.
 * </p>
 *
 * <p>
 * Any benchmarking, debugging or temporary result data are not included.
 * </p>
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(ResultAdapter.class)
public class Result implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private final Map<String, Set<ColumnPosition>> subjectColumnsPositions;

  private final List<HeaderAnnotation> headerAnnotations;

  private final CellAnnotation[][] cellAnnotations;

  private final Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelationAnnotations;

  private final List<StatisticalAnnotation> statisticalAnnotations;

  private final List<ColumnProcessingAnnotation> columnProcessingAnnotations;

  private final List<String> warnings;

  public Result(
      final Map<? extends String, Set<ColumnPosition>> subjectColumnsPositions,
      final List<? extends HeaderAnnotation> headerAnnotations,
      final CellAnnotation[][] cellAnnotations,
      final Map<? extends ColumnRelationPosition, ? extends ColumnRelationAnnotation> columnRelationAnnotations,
      final List<? extends StatisticalAnnotation> statisticalAnnotations,
      final List<? extends ColumnProcessingAnnotation> columnProcessingAnnotations,
      final List<? extends String> warnings) {
    Preconditions.checkNotNull(subjectColumnsPositions, "The subjectColumnsPositions cannot be null!");
    Preconditions.checkNotNull(headerAnnotations, "The headerAnnotations cannot be null!");
    Preconditions.checkNotNull(cellAnnotations, "The cellAnnotations cannot be null!");
    Preconditions.checkNotNull(columnRelationAnnotations, "The columnRelationAnnotations cannot be null!");
    Preconditions.checkNotNull(statisticalAnnotations, "The statisticalAnnotations cannot be null!");
    Preconditions.checkNotNull(columnProcessingAnnotations, "The columnProcessingAnnotations cannot be null!");
    Preconditions.checkNotNull(warnings, "The warnings cannot be null!");
    Preconditions.checkArgument(!cz.cuni.mff.xrg.odalic.util.Arrays.containsNull(cellAnnotations));
    Preconditions.checkArgument(cz.cuni.mff.xrg.odalic.util.Arrays.isMatrix(cellAnnotations));

    this.subjectColumnsPositions = ImmutableMap.copyOf(subjectColumnsPositions);
    this.headerAnnotations = ImmutableList.copyOf(headerAnnotations);
    this.cellAnnotations =
        cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
    this.columnRelationAnnotations = ImmutableMap.copyOf(columnRelationAnnotations);
    this.statisticalAnnotations = ImmutableList.copyOf(statisticalAnnotations);
    this.columnProcessingAnnotations = ImmutableList.copyOf(columnProcessingAnnotations);
    this.warnings = ImmutableList.copyOf(warnings);
  }

  /**
   * Compares to another object for equality (only another Result composed from equal parts passes).
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
    final Result other = (Result) obj;
    if (!Arrays.deepEquals(this.cellAnnotations, other.cellAnnotations)) {
      return false;
    }
    if (this.columnRelationAnnotations == null) {
      if (other.columnRelationAnnotations != null) {
        return false;
      }
    } else if (!this.columnRelationAnnotations.equals(other.columnRelationAnnotations)) {
      return false;
    }
    if (this.headerAnnotations == null) {
      if (other.headerAnnotations != null) {
        return false;
      }
    } else if (!this.headerAnnotations.equals(other.headerAnnotations)) {
      return false;
    }
    if (this.subjectColumnsPositions == null) {
      if (other.subjectColumnsPositions != null) {
        return false;
      }
    } else if (!this.subjectColumnsPositions.equals(other.subjectColumnsPositions)) {
      return false;
    }
    if (this.statisticalAnnotations == null) {
      if (other.statisticalAnnotations != null) {
        return false;
      }
    } else if (!this.statisticalAnnotations.equals(other.statisticalAnnotations)) {
      return false;
    }
    if (this.columnProcessingAnnotations == null) {
      if (other.columnProcessingAnnotations != null) {
        return false;
      }
    } else if (!this.columnProcessingAnnotations.equals(other.columnProcessingAnnotations)) {
      return false;
    }
    if (this.warnings == null) {
      if (other.warnings != null) {
        return false;
      }
    } else if (!this.warnings.equals(other.warnings)) {
      return false;
    }
    return true;
  }

  /**
   * @return the cell annotations
   */
  public CellAnnotation[][] getCellAnnotations() {
    return cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, this.cellAnnotations);
  }

  /**
   * @return the column processing annotations
   */
  public List<ColumnProcessingAnnotation> getColumnProcessingAnnotations() {
    return this.columnProcessingAnnotations;
  }

  /**
   * @return the column relation annotations
   */
  public Map<ColumnRelationPosition, ColumnRelationAnnotation> getColumnRelationAnnotations() {
    return this.columnRelationAnnotations;
  }

  /**
   * @return the header annotations
   */
  public List<HeaderAnnotation> getHeaderAnnotations() {
    return this.headerAnnotations;
  }

  /**
   * @return the statistical annotations
   */
  public List<StatisticalAnnotation> getStatisticalAnnotations() {
    return this.statisticalAnnotations;
  }

  /**
   * @return the subject columns positions
   */
  public Map<String, Set<ColumnPosition>> getSubjectColumnsPositions() {
    return this.subjectColumnsPositions;
  }

  /**
   * @return the warnings in order of appearance
   */
  public List<String> getWarnings() {
    return this.warnings;
  }

  /**
   * Computes hash code based on all its parts.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Arrays.deepHashCode(this.cellAnnotations);
    result = (prime * result) + ((this.columnRelationAnnotations == null) ? 0
        : this.columnRelationAnnotations.hashCode());
    result = (prime * result)
        + ((this.headerAnnotations == null) ? 0 : this.headerAnnotations.hashCode());
    result = (prime * result)
        + ((this.subjectColumnsPositions == null) ? 0 : this.subjectColumnsPositions.hashCode());
    result = (prime * result)
        + ((this.statisticalAnnotations == null) ? 0 : this.statisticalAnnotations.hashCode());
    result = (prime * result) + ((this.columnProcessingAnnotations == null) ? 0
        : this.columnProcessingAnnotations.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Result [subjectColumnsPositions=" + this.subjectColumnsPositions + ", headerAnnotations="
        + this.headerAnnotations + ", cellAnnotations=" + Arrays.deepToString(this.cellAnnotations)
        + ", columnRelationAnnotations=" + this.columnRelationAnnotations
        + ", statisticalAnnotations=" + this.statisticalAnnotations
        + ", columnProcessingAnnotations=" + this.columnProcessingAnnotations + ", warnings="
        + this.warnings + "]";
  }
}
