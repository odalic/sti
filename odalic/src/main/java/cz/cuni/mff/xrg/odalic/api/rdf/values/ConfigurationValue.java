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
@RdfsClass("http://odalic.eu/internal/Configuration")
public final class ConfigurationValue implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private String input;

  private FeedbackValue feedback;

  private List<KnowledgeBaseValue> usedBases;

  private KnowledgeBaseValue primaryBase;

  private Integer rowsLimit;

  private Boolean statistical;

  public ConfigurationValue() {}

  public ConfigurationValue(Configuration adaptee) {
    input = adaptee.getInput().getId();
    feedback = adaptee.getFeedback() == null ? null : new FeedbackValue(adaptee.getFeedback());
    usedBases = adaptee.getUsedBases().stream().map(KnowledgeBaseValue::new).collect(ImmutableList.toImmutableList());
    primaryBase = new KnowledgeBaseValue(adaptee.getPrimaryBase());
    rowsLimit =
        adaptee.getRowsLimit() == Configuration.MAXIMUM_ROWS_LIMIT ? null : adaptee.getRowsLimit();
    statistical = adaptee.isStatistical();
  }

  /**
   * @return the input
   */
  @XmlElement
  @Nullable
  @RdfProperty("http://odalic.eu/internal/Configuration/Input")
  public String getInput() {
    return input;
  }

  /**
   * @param input the input to set
   */
  public void setInput(String input) {
    Preconditions.checkNotNull(input);

    this.input = input;
  }

  /**
   * @return the feedback
   */
  @XmlElement
  @Nullable
  @RdfProperty("http://odalic.eu/internal/Configuration/Feedback")
  public FeedbackValue getFeedback() {
    return feedback;
  }

  /**
   * @param feedback the feedback to set
   */
  public void setFeedback(FeedbackValue feedback) {
    Preconditions.checkNotNull(feedback);

    this.feedback = feedback;
  }

  /**
   * @return the bases selected for the task
   */
  @XmlElement
  @Nullable
  @RdfProperty("http://odalic.eu/internal/Configuration/UsedBase")
  public List<KnowledgeBaseValue> getUsedBases() {
    return usedBases;
  }

  /**
   * @param usedBases the bases selected for the task to set
   */
  public void setUsedBases(List<? extends KnowledgeBaseValue> usedBases) {
    Preconditions.checkNotNull(usedBases);

    this.usedBases = ImmutableList.copyOf(usedBases);
  }

  /**
   * @return the primary knowledge base
   */
  @XmlElement
  @Nullable
  @RdfProperty("http://odalic.eu/internal/Configuration/PrimaryBase")
  public KnowledgeBaseValue getPrimaryBase() {
    return primaryBase;
  }

  /**
   * @param primaryBase the primary knowledge base to set
   */
  public void setPrimaryBase(KnowledgeBaseValue primaryBase) {
    Preconditions.checkNotNull(primaryBase);

    this.primaryBase = primaryBase;
  }

  /**
   * @return the maximum number of rows to process, {@code null} if no such limit set
   */
  @XmlElement
  @Nullable
  @RdfProperty(value = "http://odalic.eu/internal/Configuration/RowsLimit",
      datatype = "http://www.w3.org/2001/XMLSchema#positiveInteger")
  public Integer getRowsLimit() {
    return rowsLimit;
  }

  /**
   * @param rowsLimit the maximum number of rows to process to set
   */
  public void setRowsLimit(final @Nullable Integer rowsLimit) {
    Preconditions.checkArgument(rowsLimit == null || rowsLimit > 0);

    this.rowsLimit = rowsLimit;
  }

  /**
   * @return true for processing of statistical data
   */
  @XmlElement
  @Nullable
  @RdfProperty(value = "http://odalic.eu/internal/Configuration/Statistical",
      datatype = "http://www.w3.org/2001/XMLSchema#boolean")
  public Boolean isStatistical() {
    return statistical;
  }

  /**
   * @param statistical true for processing of statistical data
   */
  public void setStatistical(final @Nullable Boolean statistical) {
    this.statistical = statistical;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ConfigurationValue [input=" + input + ", feedback=" + feedback + ", usedBases="
        + usedBases + ", primaryBase=" + primaryBase + ", rowsLimit=" + rowsLimit + ", statistical="
        + statistical + "]";
  }
}
