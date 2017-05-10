package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.feedbacks.Ambiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.Classification;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnAmbiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnCompulsory;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnRelation;
import cz.cuni.mff.xrg.odalic.feedbacks.DataCubeComponent;
import cz.cuni.mff.xrg.odalic.feedbacks.Disambiguation;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;

/**
 * Domain class {@link Feedback} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "feedback")
public final class FeedbackValue implements Serializable {

  private static final long serialVersionUID = -7968455903789693405L;

  private Map<String, ColumnPosition> subjectColumnPositions;

  private Map<String, Set<ColumnPosition>> otherSubjectColumnPositions;

  private Set<ColumnIgnore> columnIgnores;

  private Set<ColumnCompulsory> columnCompulsory;

  private Set<ColumnAmbiguity> columnAmbiguities;

  private Set<Classification> classifications;

  private Set<ColumnRelation> columnRelations;

  private Set<Disambiguation> disambiguations;

  private Set<Ambiguity> ambiguities;

  private Set<DataCubeComponent> dataCubeComponents;

  public FeedbackValue() {
    this.subjectColumnPositions = ImmutableMap.of();
    this.otherSubjectColumnPositions = ImmutableMap.of();
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
    this.subjectColumnPositions = adaptee.getSubjectColumnPositions();
    this.otherSubjectColumnPositions = adaptee.getOtherSubjectColumnPositions();
    this.columnIgnores = adaptee.getColumnIgnores();
    this.columnCompulsory = adaptee.getColumnCompulsory();
    this.columnAmbiguities = adaptee.getColumnAmbiguities();
    this.classifications = adaptee.getClassifications();
    this.columnRelations = adaptee.getColumnRelations();
    this.disambiguations = adaptee.getDisambiguations();
    this.ambiguities = adaptee.getAmbiguities();
    this.dataCubeComponents = adaptee.getDataCubeComponents();
  }

  /**
   * @return the ambiguities
   */
  @XmlElement
  public Set<Ambiguity> getAmbiguities() {
    return this.ambiguities;
  }

  /**
   * @return the classifications
   */
  @XmlElement
  public Set<Classification> getClassifications() {
    return this.classifications;
  }

  /**
   * @return the column ambiguities
   */
  @XmlElement
  public Set<ColumnAmbiguity> getColumnAmbiguities() {
    return this.columnAmbiguities;
  }

  /**
   * @return the column ignores
   */
  @XmlElement
  public Set<ColumnIgnore> getColumnIgnores() {
    return this.columnIgnores;
  }

  /**
   * @return the column compulsory
   */
  @XmlElement
  public Set<ColumnCompulsory> getColumnCompulsory() {
    return this.columnCompulsory;
  }

  /**
   * @return the column relations
   */
  @XmlElement
  public Set<ColumnRelation> getColumnRelations() {
    return this.columnRelations;
  }

  /**
   * @return the dataCubeComponents
   */
  @XmlElement
  public Set<DataCubeComponent> getDataCubeComponents() {
    return this.dataCubeComponents;
  }

  /**
   * @return the disambiguations
   */
  @XmlElement
  public Set<Disambiguation> getDisambiguations() {
    return this.disambiguations;
  }

  /**
   * @return the subject column positions
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
   * @param ambiguities the ambiguities to set
   */
  public void setAmbiguities(final Set<? extends Ambiguity> ambiguities) {
    Preconditions.checkNotNull(ambiguities);

    this.ambiguities = ImmutableSet.copyOf(ambiguities);
  }

  /**
   * @param classifications the classifications to set
   */
  public void setClassifications(final Set<? extends Classification> classifications) {
    Preconditions.checkNotNull(classifications);

    this.classifications = ImmutableSet.copyOf(classifications);
  }

  /**
   * @param columnAmbiguities the column ambiguities to set
   */
  public void setColumnAmbiguities(final Set<? extends ColumnAmbiguity> columnAmbiguities) {
    Preconditions.checkNotNull(columnAmbiguities);

    this.columnAmbiguities = ImmutableSet.copyOf(columnAmbiguities);
  }

  /**
   * @param columnIgnores the column ignores to set
   */
  public void setColumnIgnores(final Set<? extends ColumnIgnore> columnIgnores) {
    Preconditions.checkNotNull(columnIgnores);

    this.columnIgnores = ImmutableSet.copyOf(columnIgnores);
  }

  /**
   * @param columnCompulsory the column compulsory to set
   */
  public void setColumnCompulsory(final Set<? extends ColumnCompulsory> columnCompulsory) {
    Preconditions.checkNotNull(columnCompulsory);

    this.columnCompulsory = ImmutableSet.copyOf(columnCompulsory);
  }

  /**
   * @param columnRelations the column relations to set
   */
  public void setColumnRelations(final Set<? extends ColumnRelation> columnRelations) {
    Preconditions.checkNotNull(columnRelations);

    this.columnRelations = ImmutableSet.copyOf(columnRelations);
  }

  /**
   * @param dataCubeComponents the dataCubeComponents to set
   */
  public void setDataCubeComponents(final Set<? extends DataCubeComponent> dataCubeComponents) {
    Preconditions.checkNotNull(dataCubeComponents);

    this.dataCubeComponents = ImmutableSet.copyOf(dataCubeComponents);
  }

  /**
   * @param disambiguations the disambiguations to set
   */
  public void setDisambiguations(final Set<? extends Disambiguation> disambiguations) {
    Preconditions.checkNotNull(disambiguations);

    this.disambiguations = ImmutableSet.copyOf(disambiguations);
  }

  /**
   * @param subjectColumnPositions the subject column positions to set
   */
  public void setSubjectColumnPositions(
      final Map<? extends String, ? extends ColumnPosition> subjectColumnPositions) {
    Preconditions.checkNotNull(subjectColumnPositions);

    this.subjectColumnPositions = ImmutableMap.copyOf(subjectColumnPositions);
  }

  /**
   * @param otherSubjectColumnPositions the other subject column positions to set
   */
  public void setOtherSubjectColumnPositions(
      final Map<String, Set<ColumnPosition>> otherSubjectColumnPositions) {
    Preconditions.checkNotNull(otherSubjectColumnPositions);

    this.otherSubjectColumnPositions = ImmutableMap.copyOf(otherSubjectColumnPositions);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FeedbackValue [subjectColumnPositions=" + subjectColumnPositions
        + ", otherSubjectColumnPositions=" + otherSubjectColumnPositions + ", columnIgnores="
        + columnIgnores + ", columnCompulsory=" + columnCompulsory + ", columnAmbiguities="
        + columnAmbiguities + ", classifications=" + classifications + ", columnRelations="
        + columnRelations + ", disambiguations=" + disambiguations + ", ambiguities=" + ambiguities
        + ", dataCubeComponents=" + dataCubeComponents + "]";
  }
}
