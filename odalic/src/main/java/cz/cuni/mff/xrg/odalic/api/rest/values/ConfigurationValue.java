package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

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

  private KnowledgeBase primaryBase;

  private Integer rowsLimit;

  public ConfigurationValue() {}

  public ConfigurationValue(Configuration adaptee) {
    input = adaptee.getInput().getId();
    feedback = adaptee.getFeedback();
    primaryBase = adaptee.getPrimaryBase();
    rowsLimit =
        adaptee.getRowsLimit() == Configuration.MAXIMUM_ROWS_LIMIT ? null : adaptee.getRowsLimit();
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
    Preconditions.checkArgument(rowsLimit == null || rowsLimit >= 0);

    this.rowsLimit = rowsLimit;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ConfigurationValue [input=" + input + ", feedback=" + feedback + ", primaryBase="
        + primaryBase + ", rowsLimit=" + rowsLimit + "]";
  }
}
