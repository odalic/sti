package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;

/**
 * Domain class {@link Configuration} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "configuration")
public final class ConfigurationValue implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;

  private String input;

  private Feedback feedback;

  private NavigableSet<KnowledgeBaseNameValue> usedBases;

  private KnowledgeBaseNameValue primaryBase;

  private Integer rowsLimit;

  private Boolean statistical;

  public ConfigurationValue() {}

  public ConfigurationValue(final Configuration adaptee) {
    this.input = adaptee.getInput().getId();
    this.feedback = adaptee.getFeedback();
    this.usedBases = adaptee.getUsedBases().stream().map(e -> new KnowledgeBaseNameValue(e)).collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
    this.primaryBase = new KnowledgeBaseNameValue(adaptee.getPrimaryBase());
    this.rowsLimit =
        adaptee.getRowsLimit() == Configuration.MAXIMUM_ROWS_LIMIT ? null : adaptee.getRowsLimit();
    this.statistical = adaptee.isStatistical();
  }

  /**
   * @return the feedback
   */
  @XmlElement
  @Nullable
  public Feedback getFeedback() {
    return this.feedback;
  }

  /**
   * @return the input
   */
  @XmlElement
  @Nullable
  public String getInput() {
    return this.input;
  }

  /**
   * @return the primary knowledge base
   */
  @XmlElement
  @Nullable
  public KnowledgeBaseNameValue getPrimaryBase() {
    return this.primaryBase;
  }

  /**
   * @return the maximum number of rows to process, {@code null} if no such limit set
   */
  @XmlElement
  @Nullable
  public Integer getRowsLimit() {
    return this.rowsLimit;
  }

  /**
   * @return the bases selected for the task
   */
  @XmlElement
  @Nullable
  public NavigableSet<KnowledgeBaseNameValue> getUsedBases() {
    return this.usedBases;
  }

  /**
   * @return true for processing of statistical data
   */
  @XmlElement
  @Nullable
  public Boolean isStatistical() {
    return this.statistical;
  }

  /**
   * @param feedback the feedback to set
   */
  public void setFeedback(final Feedback feedback) {
    Preconditions.checkNotNull(feedback, "The feedback cannot be null!");

    this.feedback = feedback;
  }

  /**
   * @param input the input to set
   */
  public void setInput(final String input) {
    Preconditions.checkNotNull(input, "The input cannot be null!");

    this.input = input;
  }

  /**
   * @param primaryBase the primary knowledge base to set
   */
  public void setPrimaryBase(final KnowledgeBaseNameValue primaryBase) {
    Preconditions.checkNotNull(primaryBase, "The primaryBase cannot be null!");

    this.primaryBase = primaryBase;
  }

  /**
   * @param rowsLimit the maximum number of rows to process to set
   */
  public void setRowsLimit(final @Nullable Integer rowsLimit) {
    Preconditions.checkArgument((rowsLimit == null) || (rowsLimit > 0), "The row position index must be positive, if present.");

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
  public void setUsedBases(final Set<? extends KnowledgeBaseNameValue> usedBases) {
    Preconditions.checkNotNull(usedBases, "The usedBases cannot be null!");

    this.usedBases = ImmutableSortedSet.copyOf(usedBases);
  }

  @Override
  public String toString() {
    return "ConfigurationValue [input=" + this.input + ", feedback=" + this.feedback
        + ", usedBases=" + this.usedBases + ", primaryBase=" + this.primaryBase + ", rowsLimit="
        + this.rowsLimit + ", statistical=" + this.statistical + "]";
  }
}
