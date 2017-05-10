package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionKeyJsonDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionKeyJsonSerializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionToColumnRelationAnnotationMapDeserializer;
import cz.cuni.mff.xrg.odalic.api.rest.conversions.ColumnPositionToColumnRelationAnnotationMapSerializer;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnProcessingAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Domain class {@link Result} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "result")
public final class ResultValue implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private Map<String, ColumnPosition> subjectColumnPositions;

  private Map<String, Set<ColumnPosition>> otherSubjectColumnPositions;

  private List<HeaderAnnotation> headerAnnotations;

  private CellAnnotation[][] cellAnnotations;

  private Map<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotations;

  private List<StatisticalAnnotation> statisticalAnnotations;

  private List<ColumnProcessingAnnotation> columnProcessingAnnotations;

  private List<String> warnings;

  public ResultValue() {
    this.subjectColumnPositions = ImmutableMap.of();
    this.otherSubjectColumnPositions = ImmutableMap.of();
    this.headerAnnotations = ImmutableList.of();
    this.cellAnnotations = new CellAnnotation[0][0];;
    this.columnRelationAnnotations = ImmutableMap.of();
    this.statisticalAnnotations = ImmutableList.of();
    this.columnProcessingAnnotations = ImmutableList.of();
    this.warnings = ImmutableList.of();
  }

  public ResultValue(final Result adaptee) {
    this.subjectColumnPositions = adaptee.getSubjectColumnPositions();
    this.otherSubjectColumnPositions = adaptee.getOtherSubjectColumnPositions();
    this.headerAnnotations = adaptee.getHeaderAnnotations();
    this.cellAnnotations = adaptee.getCellAnnotations();
    this.statisticalAnnotations = adaptee.getStatisticalAnnotations();
    this.columnProcessingAnnotations = adaptee.getColumnProcessingAnnotations();
    this.warnings = adaptee.getWarnings();

    initializeColumnRelationAnnotations(adaptee);
  }

  /**
   * @return the cell annotations
   */
  @XmlElement
  public CellAnnotation[][] getCellAnnotations() {
    return cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, this.cellAnnotations);
  }

  /**
   * @return the column processing annotations
   */
  @XmlElement
  public List<ColumnProcessingAnnotation> getColumnProcessingAnnotations() {
    return this.columnProcessingAnnotations;
  }

  /**
   * @return the column relation Annotations
   */
  @XmlElement
  @JsonDeserialize(keyUsing = ColumnPositionKeyJsonDeserializer.class,
      contentUsing = ColumnPositionToColumnRelationAnnotationMapDeserializer.class)
  @JsonSerialize(keyUsing = ColumnPositionKeyJsonSerializer.class,
      contentUsing = ColumnPositionToColumnRelationAnnotationMapSerializer.class)
  public Map<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> getColumnRelationAnnotations() {
    return this.columnRelationAnnotations;
  }

  /**
   * @return the header annotations
   */
  @XmlElement
  public List<HeaderAnnotation> getHeaderAnnotations() {
    return this.headerAnnotations;
  }

  /**
   * @return the statistical annotations
   */
  @XmlElement
  public List<StatisticalAnnotation> getStatisticalAnnotations() {
    return this.statisticalAnnotations;
  }

  /**
   * @return the subject column position
   */
  @XmlElement
  public Map<String, ColumnPosition> getSubjectColumnPositions() {
    return this.subjectColumnPositions;
  }

  /**
   * @return the other subject column positions
   */
  @XmlElement
  public Map<String, Set<ColumnPosition>> getOtherSubjectColumnPositions() {
    return this.otherSubjectColumnPositions;
  }

  /**
   * @return the warnings
   */
  @XmlElement
  public List<String> getWarnings() {
    return this.warnings;
  }

  private void initializeColumnRelationAnnotations(final Result adaptee) {
    this.columnRelationAnnotations = new HashMap<>();
    for (final Map.Entry<ColumnRelationPosition, ColumnRelationAnnotation> entry : adaptee
        .getColumnRelationAnnotations().entrySet()) {
      final ColumnRelationPosition key = entry.getKey();
      final ColumnPosition firstColumn = key.getFirst();
      final ColumnPosition secondColumn = key.getSecond();
      final ColumnRelationAnnotation annotation = entry.getValue();

      final Map<ColumnPosition, ColumnRelationAnnotation> subMap =
          this.columnRelationAnnotations.get(firstColumn);
      if (subMap == null) {
        this.columnRelationAnnotations.put(firstColumn,
            new HashMap<>(ImmutableMap.of(secondColumn, annotation)));
      } else {
        subMap.put(secondColumn, annotation);
      }
    }
  }

  /**
   * @param cellAnnotations the cell annotations to set
   */
  public void setCellAnnotations(final CellAnnotation[][] cellAnnotations) {
    Preconditions.checkNotNull(cellAnnotations);

    this.cellAnnotations =
        cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, cellAnnotations);
  }

  /**
   * @param columnProcessingAnnotations the column processing annotations to set
   */
  public void setColumnProcessingAnnotations(
      final List<ColumnProcessingAnnotation> columnProcessingAnnotations) {
    Preconditions.checkNotNull(columnProcessingAnnotations);

    this.columnProcessingAnnotations = ImmutableList.copyOf(columnProcessingAnnotations);
  }

  /**
   * @param columnRelationAnnotations the column relation annotations to set
   */
  public void setColumnRelationAnnotations(
      final Map<? extends ColumnPosition, Map<? extends ColumnPosition, ? extends ColumnRelationAnnotation>> columnRelationAnnotations) {
    final ImmutableMap.Builder<ColumnPosition, Map<ColumnPosition, ColumnRelationAnnotation>> columnRelationAnnotationsBuilder =
        ImmutableMap.builder();
    for (final Map.Entry<? extends ColumnPosition, Map<? extends ColumnPosition, ? extends ColumnRelationAnnotation>> entry : columnRelationAnnotations
        .entrySet()) {
      columnRelationAnnotationsBuilder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
    }
    this.columnRelationAnnotations = columnRelationAnnotationsBuilder.build();
  }

  /**
   * @param headerAnnotations the header annotations to set
   */
  public void setHeaderAnnotations(final List<HeaderAnnotation> headerAnnotations) {
    Preconditions.checkNotNull(headerAnnotations);

    this.headerAnnotations = ImmutableList.copyOf(headerAnnotations);
  }

  /**
   * @param statisticalAnnotations the statistical annotations to set
   */
  public void setStatisticalAnnotations(final List<StatisticalAnnotation> statisticalAnnotations) {
    Preconditions.checkNotNull(statisticalAnnotations);

    this.statisticalAnnotations = ImmutableList.copyOf(statisticalAnnotations);
  }

  /**
   * @param subjectColumnPositions the subject column position to set
   */
  public void setSubjectColumnPosition(
      final Map<? extends String, ? extends ColumnPosition> subjectColumnPositions) {
    Preconditions.checkNotNull(subjectColumnPositions);

    this.subjectColumnPositions = ImmutableMap.copyOf(subjectColumnPositions);
  }

  /**
   * @param otherSubjectColumnPositions the other subject column positions to set
   */
  public void setOtherSubjectColumnPosition(
      final Map<String, Set<ColumnPosition>> otherSubjectColumnPositions) {
    Preconditions.checkNotNull(otherSubjectColumnPositions);

    this.otherSubjectColumnPositions = ImmutableMap.copyOf(otherSubjectColumnPositions);
  }

  /**
   * @param warnings the warnings to set
   */
  public void setWarnings(final List<String> warnings) {
    Preconditions.checkNotNull(warnings);

    this.warnings = ImmutableList.copyOf(warnings);
  }

  @Override
  public String toString() {
    return "ResultValue [subjectColumnPositions=" + this.subjectColumnPositions
        + ", otherSubjectColumnPositions=" + this.otherSubjectColumnPositions
        + ", headerAnnotations=" + this.headerAnnotations + ", cellAnnotations="
        + Arrays.toString(this.cellAnnotations) + ", columnRelationAnnotations="
        + this.columnRelationAnnotations + ", statisticalAnnotations=" + this.statisticalAnnotations
        + ", warnings=" + this.warnings + "]";
  }
}
