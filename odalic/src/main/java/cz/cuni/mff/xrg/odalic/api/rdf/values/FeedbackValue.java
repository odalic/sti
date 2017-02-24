package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;
import java.util.Set;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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

  private Set<ColumnIgnoreValue> columnIgnores;

  private Set<ColumnAmbiguityValue> columnAmbiguities;

  private Set<ClassificationValue> classifications;

  private Set<ColumnRelationValue> columnRelations;

  private Set<DisambiguationValue> disambiguations;

  private Set<AmbiguityValue> ambiguities;

  private Set<DataCubeComponentValue> dataCubeComponents;

  public FeedbackValue() {
    subjectColumnPositions = ImmutableSet.of();
    columnIgnores = ImmutableSet.of();
    columnAmbiguities = ImmutableSet.of();
    classifications = ImmutableSet.of();
    columnRelations = ImmutableSet.of();
    disambiguations = ImmutableSet.of();
    ambiguities = ImmutableSet.of();
    dataCubeComponents = ImmutableSet.of();
  }

  public FeedbackValue(Feedback adaptee) {
    subjectColumnPositions = adaptee.getSubjectColumnPositions().entrySet().stream()
        .map(e -> new KnowledgeBaseColumnPositionEntry(new KnowledgeBaseValue(e.getKey()),
            new ColumnPositionValue(e.getValue())))
        .collect(ImmutableSet.toImmutableSet());
    columnIgnores = adaptee.getColumnIgnores().stream().map(ColumnIgnoreValue::new)
        .collect(ImmutableSet.toImmutableSet());
    columnAmbiguities = adaptee.getColumnAmbiguities().stream().map(ColumnAmbiguityValue::new)
        .collect(ImmutableSet.toImmutableSet());
    classifications = adaptee.getClassifications().stream().map(ClassificationValue::new)
        .collect(ImmutableSet.toImmutableSet());
    columnRelations = adaptee.getColumnRelations().stream().map(ColumnRelationValue::new)
        .collect(ImmutableSet.toImmutableSet());
    disambiguations = adaptee.getDisambiguations().stream().map(DisambiguationValue::new)
        .collect(ImmutableSet.toImmutableSet());
    ambiguities = adaptee.getAmbiguities().stream().map(AmbiguityValue::new)
        .collect(ImmutableSet.toImmutableSet());
    dataCubeComponents = adaptee.getDataCubeComponents().stream().map(DataCubeComponentValue::new)
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * @return the subject column positions
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/subjectColumnPositions")
  public Set<KnowledgeBaseColumnPositionEntry> getSubjectColumnPositions() {
    return subjectColumnPositions;
  }

  /**
   * @param subjectColumnPositions the subject column positions to set
   */
  public void setSubjectColumnPositions(
      Set<? extends KnowledgeBaseColumnPositionEntry> subjectColumnPositions) {
    Preconditions.checkNotNull(subjectColumnPositions);

    this.subjectColumnPositions = ImmutableSet.copyOf(subjectColumnPositions);
  }

  /**
   * @return the column ignores
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/columnIgnore")
  public Set<ColumnIgnoreValue> getColumnIgnores() {
    return columnIgnores;
  }

  /**
   * @param columnIgnores the column ignores to set
   */
  public void setColumnIgnores(Set<? extends ColumnIgnoreValue> columnIgnores) {
    Preconditions.checkNotNull(columnIgnores);

    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
  }

  /**
   * @return the column ambiguities
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/columnAmbiguity")
  public Set<ColumnAmbiguityValue> getColumnAmbiguities() {
    return columnAmbiguities;
  }

  /**
   * @param columnAmbiguities the column ambiguities to set
   */
  public void setColumnAmbiguities(Set<? extends ColumnAmbiguityValue> columnAmbiguities) {
    Preconditions.checkNotNull(columnAmbiguities);

    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
  }

  /**
   * @return the classifications
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/classification")
  public Set<ClassificationValue> getClassifications() {
    return classifications;
  }

  /**
   * @param classifications the classifications to set
   */
  public void setClassifications(Set<? extends ClassificationValue> classifications) {
    Preconditions.checkNotNull(classifications);

    this.classifications = ImmutableSet.copyOf(classifications);
  }

  /**
   * @return the column relations
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/columnRelation")
  public Set<ColumnRelationValue> getColumnRelations() {
    return columnRelations;
  }

  /**
   * @param columnRelations the column relations to set
   */
  public void setColumnRelations(Set<? extends ColumnRelationValue> columnRelations) {
    Preconditions.checkNotNull(columnRelations);

    this.columnRelations = ImmutableSet.copyOf(columnRelations);
  }

  /**
   * @return the disambiguations
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/disambiguation")
  public Set<DisambiguationValue> getDisambiguations() {
    return disambiguations;
  }

  /**
   * @param disambiguations the disambiguations to set
   */
  public void setDisambiguations(Set<? extends DisambiguationValue> disambiguations) {
    Preconditions.checkNotNull(disambiguations);

    this.disambiguations = ImmutableSet.copyOf(disambiguations);
  }

  /**
   * @return the ambiguities
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/ambiguity")
  public Set<AmbiguityValue> getAmbiguities() {
    return ambiguities;
  }

  /**
   * @param ambiguities the ambiguities to set
   */
  public void setAmbiguities(Set<? extends AmbiguityValue> ambiguities) {
    Preconditions.checkNotNull(ambiguities);

    this.ambiguities = ImmutableSet.copyOf(ambiguities);
  }

  /**
   * @return the dataCubeComponents
   */
  @RdfProperty("http://odalic.eu/internal/Feedback/dataCubeComponent")
  public Set<DataCubeComponentValue> getDataCubeComponents() {
    return dataCubeComponents;
  }

  /**
   * @param dataCubeComponents the dataCubeComponents to set
   */
  public void setDataCubeComponents(Set<? extends DataCubeComponentValue> dataCubeComponents) {
    Preconditions.checkNotNull(dataCubeComponents);

    this.dataCubeComponents = ImmutableSet.copyOf(dataCubeComponents);
  }

  public Feedback toFeedback() {
    final ImmutableMap.Builder<KnowledgeBase, ColumnPosition> subjectColumnPositionsMapBuilder =
        ImmutableMap.builder();
    for (final KnowledgeBaseColumnPositionEntry entry : subjectColumnPositions) {
      subjectColumnPositionsMapBuilder.put(entry.getBase().toKnowledgeBase(),
          entry.getValue().toColumnPosition());
    }

    return new Feedback(subjectColumnPositionsMapBuilder.build(),
        columnIgnores.stream().map(ColumnIgnoreValue::toColumnIgnore)
            .collect(ImmutableSet.toImmutableSet()),
        columnAmbiguities.stream().map(ColumnAmbiguityValue::toColumnAmbiguity)
            .collect(ImmutableSet.toImmutableSet()),
        classifications.stream().map(ClassificationValue::toClassification)
            .collect(ImmutableSet.toImmutableSet()),
        columnRelations.stream().map(ColumnRelationValue::toColumnRelation)
            .collect(ImmutableSet.toImmutableSet()),
        disambiguations.stream().map(DisambiguationValue::toDisambiguation)
            .collect(ImmutableSet.toImmutableSet()),
        ambiguities.stream().map(AmbiguityValue::toAmbiguity)
            .collect(ImmutableSet.toImmutableSet()),
        dataCubeComponents.stream().map(DataCubeComponentValue::toDataCubeComponent)
            .collect(ImmutableSet.toImmutableSet()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FeedbackValue [subjectColumnPositions=" + subjectColumnPositions + ", columnIgnores="
        + columnIgnores + ", columnAmbiguities=" + columnAmbiguities + ", classifications="
        + classifications + ", columnRelations=" + columnRelations + ", disambiguations="
        + disambiguations + ", ambiguities=" + ambiguities + ", dataCubeComponents="
        + dataCubeComponents + "]";
  }
}
