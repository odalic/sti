package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;

/**
 * Domain class {@link Configuration} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "configuration")
@RdfsClass("http://odalic.eu/internal/configuration")
public final class ConfigurationValue implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private String input;

  private FeedbackValue feedback;

  private List<KnowledgeBaseValue> usedBases;

  private KnowledgeBaseValue primaryBase;

  private Integer rowsLimit;

  private Boolean statistical;

  public ConfigurationValue() {}

  public ConfigurationValue(final Configuration adaptee) {
    this.input = adaptee.getInput().getId();
    this.feedback = adaptee.getFeedback() == null ? null : new FeedbackValue(adaptee.getFeedback());
    this.usedBases = adaptee.getUsedBases().stream().map(KnowledgeBaseValue::new)
        .collect(ImmutableList.toImmutableList());
    this.primaryBase = new KnowledgeBaseValue(adaptee.getPrimaryBase());
    this.rowsLimit =
        adaptee.getRowsLimit() == Configuration.MAXIMUM_ROWS_LIMIT ? null : adaptee.getRowsLimit();
    this.statistical = adaptee.isStatistical();
  }

  /**
   * @return the feedback
   */
  @XmlElement
  @Nullable
  @RdfProperty("http://odalic.eu/internal/Configuration/feedback")
  public FeedbackValue getFeedback() {
    return this.feedback;
  }

  /**
   * @return the input
   */
  @XmlElement
  @Nullable
  @RdfProperty("http://odalic.eu/internal/Configuration/input")
  public String getInput() {
    return this.input;
  }

  /**
   * @return the primary knowledge base
   */
  @XmlElement
  @Nullable
  @RdfProperty("http://odalic.eu/internal/Configuration/primaryBase")
  public KnowledgeBaseValue getPrimaryBase() {
    return this.primaryBase;
  }

  /**
   * @return the maximum number of rows to process, {@code null} if no such limit set
   */
  @XmlElement
  @Nullable
  @RdfProperty(value = "http://odalic.eu/internal/Configuration/rowsLimit",
      datatype = "http://www.w3.org/2001/XMLSchema#positiveInteger")
  public Integer getRowsLimit() {
    return this.rowsLimit;
  }

  /**
   * @return the bases selected for the task
   */
  @XmlElement
  @Nullable
  @RdfProperty("http://odalic.eu/internal/Configuration/usedBase")
  public List<KnowledgeBaseValue> getUsedBases() {
    return this.usedBases;
  }

  /**
   * @return true for processing of statistical data
   */
  @XmlElement
  @Nullable
  @RdfProperty(value = "http://odalic.eu/internal/Configuration/statistical",
      datatype = "http://www.w3.org/2001/XMLSchema#boolean")
  public Boolean isStatistical() {
    return this.statistical;
  }

  /**
   * @param feedback the feedback to set
   */
  public void setFeedback(final FeedbackValue feedback) {
    Preconditions.checkNotNull(feedback);

    this.feedback = feedback;
  }

  /**
   * @param input the input to set
   */
  public void setInput(final String input) {
    Preconditions.checkNotNull(input);

    this.input = input;
  }

  /**
   * @param primaryBase the primary knowledge base to set
   */
  public void setPrimaryBase(final KnowledgeBaseValue primaryBase) {
    Preconditions.checkNotNull(primaryBase);

    this.primaryBase = primaryBase;
  }

  /**
   * @param rowsLimit the maximum number of rows to process to set
   */
  public void setRowsLimit(final @Nullable Integer rowsLimit) {
    Preconditions.checkArgument((rowsLimit == null) || (rowsLimit > 0));

    this.rowsLimit = rowsLimit;
  }

  /**
   * @param statistical true for processing of statistical data
   */
  public void setStatistical(final @Nullable Boolean statistical) {
    this.statistical = statistical;
  }

  /**
   * @param usedBases the bases selected for the task to set
   */
  public void setUsedBases(final List<? extends KnowledgeBaseValue> usedBases) {
    Preconditions.checkNotNull(usedBases);

    this.usedBases = ImmutableList.copyOf(usedBases);
  }

  @Override
  public String toString() {
    return "ConfigurationValue [input=" + this.input + ", feedback=" + this.feedback
        + ", usedBases=" + this.usedBases + ", primaryBase=" + this.primaryBase + ", rowsLimit="
        + this.rowsLimit + ", statistical=" + this.statistical + "]";
  }
}
