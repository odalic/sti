package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;
import java.util.Set;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rdf.values.util.Annotations;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Domain class {@link Feedback} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/Feedback")
public final class FeedbackValue implements Serializable {

  private static final long serialVersionUID = -7968455903789693405L;

  private Set<KnowledgeBaseColumnPositionEntry> subjectColumnPositions;

  private Set<KnowledgeBaseColumnPositionSetEntry> otherSubjectColumnPositions;

  private Set<ColumnIgnoreValue> columnIgnores;

  private Set<ColumnAmbiguityValue> columnAmbiguities;

  private Set<ClassificationValue> classifications;

  private Set<ColumnRelationValue> columnRelations;

  private Set<DisambiguationValue> disambiguations;

  private Set<AmbiguityValue> ambiguities;

  private Set<DataCubeComponentValue> dataCubeComponents;

  public FeedbackValue() {
    this.subjectColumnPositions = ImmutableSet.of();
    this.otherSubjectColumnPositions = ImmutableSet.of();
    this.columnIgnores = ImmutableSet.of();
    this.columnAmbiguities = ImmutableSet.of();
    this.classifications = ImmutableSet.of();
    this.columnRelations = ImmutableSet.of();
    this.disambiguations = ImmutableSet.of();
    this.ambiguities = ImmutableSet.of();
    this.dataCubeComponents = ImmutableSet.of();
  }

  public FeedbackValue(final Feedback adaptee) {
    this.subjectColumnPositions = adaptee.getSubjectColumnPositions().entrySet().stream()
        .map(e -> new KnowledgeBaseColumnPositionEntry(new KnowledgeBaseValue(e.getKey()),
            new ColumnPositionValue(e.getValue())))
        .collect(ImmutableSet.toImmutableSet());
    this.otherSubjectColumnPositions = Annotations.toPositionValues(adaptee.getOtherSubjectColumnPositions());
    this.columnIgnores = adaptee.getColumnIgnores().stream().map(ColumnIgnoreValue::new)
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
   * @return the subject column positions
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/subjectColumnPositions")
  public Set<KnowledgeBaseColumnPositionEntry> getSubjectColumnPositions() {
    return this.subjectColumnPositions;
  }

  /**
   * @return the other subject column positions
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/otherSubjectColumnPositions")
  public Set<KnowledgeBaseColumnPositionSetEntry> getOtherSubjectColumnPositions() {
    return this.otherSubjectColumnPositions;
  }

  /**
   * @param ambiguities the ambiguities to set
   */
  public void setAmbiguities(final Set<? extends AmbiguityValue> ambiguities) {
    Preconditions.checkNotNull(ambiguities);

    this.ambiguities = ImmutableSet.copyOf(ambiguities);
  }

  /**
   * @param classifications the classifications to set
   */
  public void setClassifications(final Set<? extends ClassificationValue> classifications) {
    Preconditions.checkNotNull(classifications);

    this.classifications = ImmutableSet.copyOf(classifications);
  }

  /**
   * @param columnAmbiguities the column ambiguities to set
   */
  public void setColumnAmbiguities(final Set<? extends ColumnAmbiguityValue> columnAmbiguities) {
    Preconditions.checkNotNull(columnAmbiguities);

    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
  }

  /**
   * @param columnIgnores the column ignores to set
   */
  public void setColumnIgnores(final Set<? extends ColumnIgnoreValue> columnIgnores) {
    Preconditions.checkNotNull(columnIgnores);

    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
  }

  /**
   * @param columnRelations the column relations to set
   */
  public void setColumnRelations(final Set<? extends ColumnRelationValue> columnRelations) {
    Preconditions.checkNotNull(columnRelations);

    this.columnRelations = ImmutableSet.copyOf(columnRelations);
  }

  /**
   * @param dataCubeComponents the dataCubeComponents to set
   */
  public void setDataCubeComponents(
      final Set<? extends DataCubeComponentValue> dataCubeComponents) {
    Preconditions.checkNotNull(dataCubeComponents);

    this.dataCubeComponents = ImmutableSet.copyOf(dataCubeComponents);
  }

  /**
   * @param disambiguations the disambiguations to set
   */
  public void setDisambiguations(final Set<? extends DisambiguationValue> disambiguations) {
    Preconditions.checkNotNull(disambiguations);

    this.disambiguations = ImmutableSet.copyOf(disambiguations);
  }

  /**
   * @param subjectColumnPositions the subject column positions to set
   */
  public void setSubjectColumnPositions(
      final Set<? extends KnowledgeBaseColumnPositionEntry> subjectColumnPositions) {
    Preconditions.checkNotNull(subjectColumnPositions);

    this.subjectColumnPositions = ImmutableSet.copyOf(subjectColumnPositions);
  }

  /**
   * @param otherSubjectColumnPositions the subject column positions to set
   */
  public void setOtherSubjectColumnPositions(
      final Set<? extends KnowledgeBaseColumnPositionSetEntry> otherSubjectColumnPositions) {
    Preconditions.checkNotNull(otherSubjectColumnPositions);

    this.otherSubjectColumnPositions = ImmutableSet.copyOf(otherSubjectColumnPositions);
  }

  public Feedback toFeedback() {
    final ImmutableMap.Builder<KnowledgeBase, ColumnPosition> subjectColumnPositionsMapBuilder =
        ImmutableMap.builder();
    for (final KnowledgeBaseColumnPositionEntry entry : this.subjectColumnPositions) {
      subjectColumnPositionsMapBuilder.put(entry.getBase().toKnowledgeBase(),
          entry.getValue().toColumnPosition());
    }

    return new Feedback(subjectColumnPositionsMapBuilder.build(),
        Annotations.toPositionDomain(this.otherSubjectColumnPositions),
        this.columnIgnores.stream().map(ColumnIgnoreValue::toColumnIgnore)
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
    return "FeedbackValue [subjectColumnPositions=" + this.subjectColumnPositions
        + ", otherSubjectColumnPositions=" + this.otherSubjectColumnPositions
        + ", columnIgnores=" + this.columnIgnores + ", columnAmbiguities=" + this.columnAmbiguities
        + ", classifications=" + this.classifications + ", columnRelations=" + this.columnRelations
        + ", disambiguations=" + this.disambiguations + ", ambiguities=" + this.ambiguities
        + ", dataCubeComponents=" + this.dataCubeComponents + "]";
  }
}
