package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;
import java.util.Set;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rdf.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;

/**
 * Domain class {@link Feedback} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/Feedback")
public final class FeedbackValue implements Serializable {

  private static final long serialVersionUID = -7968455903789693405L;

  private Set<KnowledgeBaseColumnPositionSetEntry> subjectColumnsPositions;

  private Set<ColumnIgnoreValue> columnIgnores;

  private Set<ColumnCompulsoryValue> columnCompulsory;

  private Set<ColumnAmbiguityValue> columnAmbiguities;

  private Set<ClassificationValue> classifications;

  private Set<ColumnRelationValue> columnRelations;

  private Set<DisambiguationValue> disambiguations;

  private Set<AmbiguityValue> ambiguities;

  private Set<DataCubeComponentValue> dataCubeComponents;

  public FeedbackValue() {
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

  public FeedbackValue(final Feedback adaptee) {
    this.subjectColumnsPositions = Annotations.toPositionValues(adaptee.getSubjectColumnsPositions());
    this.columnIgnores = adaptee.getColumnIgnores().stream().map(ColumnIgnoreValue::new)
        .collect(ImmutableSet.toImmutableSet());
    this.columnCompulsory = adaptee.getColumnCompulsory().stream().map(ColumnCompulsoryValue::new)
        .collect(ImmutableSet.toImmutableSet());
    this.columnAmbiguities = adaptee.getColumnAmbiguities().stream().map(ColumnAmbiguityValue::new)
        .collect(ImmutableSet.toImmutableSet());
    this.classifications = adaptee.getClassifications().stream().map(ClassificationValue::new)
        .collect(ImmutableSet.toImmutableSet());
    this.columnRelations = adaptee.getColumnRelations().stream().map(ColumnRelationValue::new)
        .collect(ImmutableSet.toImmutableSet());
    this.disambiguations = adaptee.getDisambiguations().stream().map(DisambiguationValue::new)
        .collect(ImmutableSet.toImmutableSet());
    this.ambiguities = adaptee.getAmbiguities().stream().map(AmbiguityValue::new)
        .collect(ImmutableSet.toImmutableSet());
    this.dataCubeComponents = adaptee.getDataCubeComponents().stream()
        .map(DataCubeComponentValue::new).collect(ImmutableSet.toImmutableSet());
  }

  /**
   * @return the ambiguities
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/ambiguity")
  public Set<AmbiguityValue> getAmbiguities() {
    return this.ambiguities;
  }

  /**
   * @return the classifications
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/classification")
  public Set<ClassificationValue> getClassifications() {
    return this.classifications;
  }

  /**
   * @return the column ambiguities
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/columnAmbiguity")
  public Set<ColumnAmbiguityValue> getColumnAmbiguities() {
    return this.columnAmbiguities;
  }

  /**
   * @return the column ignores
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/columnIgnore")
  public Set<ColumnIgnoreValue> getColumnIgnores() {
    return this.columnIgnores;
  }

  /**
   * @return the column compulsory
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/columnCompulsory")
  public Set<ColumnCompulsoryValue> getColumnCompulsory() {
    return this.columnCompulsory;
  }

  /**
   * @return the column relations
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/columnRelation")
  public Set<ColumnRelationValue> getColumnRelations() {
    return this.columnRelations;
  }

  /**
   * @return the dataCubeComponents
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/dataCubeComponent")
  public Set<DataCubeComponentValue> getDataCubeComponents() {
    return this.dataCubeComponents;
  }

  /**
   * @return the disambiguations
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/disambiguation")
  public Set<DisambiguationValue> getDisambiguations() {
    return this.disambiguations;
  }

  /**
   * @return the subject columns positions
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/subjectColumnsPositions")
  public Set<KnowledgeBaseColumnPositionSetEntry> getSubjectColumnsPositions() {
    return this.subjectColumnsPositions;
  }

  /**
   * @param ambiguities the ambiguities to set
   */
  public void setAmbiguities(final Set<? extends AmbiguityValue> ambiguities) {
    Preconditions.checkNotNull(ambiguities, "The ambiguities cannot be null!");

    this.ambiguities = ImmutableSet.copyOf(ambiguities);
  }

  /**
   * @param classifications the classifications to set
   */
  public void setClassifications(final Set<? extends ClassificationValue> classifications) {
    Preconditions.checkNotNull(classifications, "The classifications cannot be null!");

    this.classifications = ImmutableSet.copyOf(classifications);
  }

  /**
   * @param columnAmbiguities the column ambiguities to set
   */
  public void setColumnAmbiguities(final Set<? extends ColumnAmbiguityValue> columnAmbiguities) {
    Preconditions.checkNotNull(columnAmbiguities, "The columnAmbiguities cannot be null!");

    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
  }

  /**
   * @param columnIgnores the column ignores to set
   */
  public void setColumnIgnores(final Set<? extends ColumnIgnoreValue> columnIgnores) {
    Preconditions.checkNotNull(columnIgnores, "The columnIgnores cannot be null!");

    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
  }

  /**
   * @param columnCompulsory the column compulsory to set
   */
  public void setColumnCompulsory(final Set<? extends ColumnCompulsoryValue> columnCompulsory) {
    Preconditions.checkNotNull(columnCompulsory, "The columnCompulsory cannot be null!");

    this.columnCompulsory = ImmutableSet.copyOf(columnCompulsory);
  }

  /**
   * @param columnRelations the column relations to set
   */
  public void setColumnRelations(final Set<? extends ColumnRelationValue> columnRelations) {
    Preconditions.checkNotNull(columnRelations, "The columnRelations cannot be null!");

    this.columnRelations = ImmutableSet.copyOf(columnRelations);
  }

  /**
   * @param dataCubeComponents the dataCubeComponents to set
   */
  public void setDataCubeComponents(
      final Set<? extends DataCubeComponentValue> dataCubeComponents) {
    Preconditions.checkNotNull(dataCubeComponents, "The dataCubeComponents cannot be null!");

    this.dataCubeComponents = ImmutableSet.copyOf(dataCubeComponents);
  }

  /**
   * @param disambiguations the disambiguations to set
   */
  public void setDisambiguations(final Set<? extends DisambiguationValue> disambiguations) {
    Preconditions.checkNotNull(disambiguations, "The disambiguations cannot be null!");

    this.disambiguations = ImmutableSet.copyOf(disambiguations);
  }

  /**
   * @param subjectColumnsPositions the subject columns positions to set
   */
  public void setSubjectColumnsPositions(
      final Set<? extends KnowledgeBaseColumnPositionSetEntry> subjectColumnsPositions) {
    Preconditions.checkNotNull(subjectColumnsPositions, "The subjectColumnsPositions cannot be null!");

    this.subjectColumnsPositions = ImmutableSet.copyOf(subjectColumnsPositions);
  }

  public Feedback toFeedback() {
    return new Feedback(Annotations.toPositionDomain(this.subjectColumnsPositions),
        this.columnIgnores.stream().map(ColumnIgnoreValue::toColumnIgnore)
            .collect(ImmutableSet.toImmutableSet()),
        this.columnCompulsory.stream().map(ColumnCompulsoryValue::toColumnCompulsory)
            .collect(ImmutableSet.toImmutableSet()),
        this.columnAmbiguities.stream().map(ColumnAmbiguityValue::toColumnAmbiguity)
            .collect(ImmutableSet.toImmutableSet()),
        this.classifications.stream().map(ClassificationValue::toClassification)
            .collect(ImmutableSet.toImmutableSet()),
        this.columnRelations.stream().map(ColumnRelationValue::toColumnRelation)
            .collect(ImmutableSet.toImmutableSet()),
        this.disambiguations.stream().map(DisambiguationValue::toDisambiguation)
            .collect(ImmutableSet.toImmutableSet()),
        this.ambiguities.stream().map(AmbiguityValue::toAmbiguity)
            .collect(ImmutableSet.toImmutableSet()),
        this.dataCubeComponents.stream().map(DataCubeComponentValue::toDataCubeComponent)
            .collect(ImmutableSet.toImmutableSet()));
  }

  @Override
  public String toString() {
    return "FeedbackValue [subjectColumnsPositions=" + subjectColumnsPositions + ", columnIgnores="
        + columnIgnores + ", columnCompulsory=" + columnCompulsory + ", columnAmbiguities="
        + columnAmbiguities + ", classifications=" + classifications + ", columnRelations="
        + columnRelations + ", disambiguations=" + disambiguations + ", ambiguities=" + ambiguities
        + ", dataCubeComponents=" + dataCubeComponents + "]";
  }
}
