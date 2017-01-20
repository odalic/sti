package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.NavigableSet;
import java.util.Set;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
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

  private NavigableSet<KnowledgeBase> usedBases;
  
  private KnowledgeBase primaryBase;

  private Integer rowsLimit;

  private Boolean isStatistical;

  public ConfigurationValue() {}

  public ConfigurationValue(Configuration adaptee) {
    input = adaptee.getInput().getId();
    feedback = adaptee.getFeedback();
    setUsedBases(ImmutableSortedSet.copyOf(adaptee.getUsedBases()));
    primaryBase = adaptee.getPrimaryBase();
    rowsLimit =
        adaptee.getRowsLimit() == Configuration.MAXIMUM_ROWS_LIMIT ? null : adaptee.getRowsLimit();
    isStatistical = adaptee.getIsStatistical();
  }

  /**
   * @return the input
   */
  @XmlElement
  @Nullable
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
  public Feedback getFeedback() {
    return feedback;
  }

  /**
   * @param feedback the feedback to set
   */
  public void setFeedback(Feedback feedback) {
    Preconditions.checkNotNull(feedback);

    this.feedback = feedback;
  }

  /**
   * @return the bases selected for the task
   */
  @XmlElement
  @Nullable
  public NavigableSet<KnowledgeBase> getUsedBases() {
    return usedBases;
  }

  /**
   * @param usedBases the bases selected for the task to set
   */
  public void setUsedBases(Set<? extends KnowledgeBase> usedBases) {
    Preconditions.checkNotNull(usedBases);
    
    this.usedBases = ImmutableSortedSet.copyOf(usedBases);
  }

  /**
   * @return the primary knowledge base
   */
  @XmlElement
  @Nullable
  public KnowledgeBase getPrimaryBase() {
    return primaryBase;
  }

  /**
   * @param primaryBase the primary knowledge base to set
   */
  public void setPrimaryBase(KnowledgeBase primaryBase) {
    Preconditions.checkNotNull(primaryBase);

    this.primaryBase = primaryBase;
  }

  /**
   * @return the maximum number of rows to process, {@code null} if no such limit set
   */
  @Nullable
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
   * @return true for processing statistical data
   */
  @Nullable
  public Boolean getIsStatistical() {
    return isStatistical;
  }

  /**
   * @param isStatistical true for processing statistical data
   */
  public void setIsStatistical(final @Nullable Boolean isStatistical) {
    this.isStatistical = isStatistical;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ConfigurationValue [input=" + input + ", feedback=" + feedback + ", usedBases=" + usedBases + ", primaryBase="
        + primaryBase + ", rowsLimit=" + rowsLimit + ", isStatistical=" + isStatistical + "]";
  }
}
