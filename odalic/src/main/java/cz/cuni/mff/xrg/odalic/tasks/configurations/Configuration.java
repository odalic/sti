package cz.cuni.mff.xrg.odalic.tasks.configurations;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ConfigurationAdapter;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Task configuration.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(ConfigurationAdapter.class)
public final class Configuration implements Serializable {

  private static final long serialVersionUID = -6359038623760039155L;
  
  private final File input;

  private final Feedback feedback;
  
  private final KnowledgeBase primaryBase;
  
  private final int rowsLimit;

  /**
   * Creates configuration without any feedback, thus implying fully automatic processing.
   * 
   * @param input
   * @param primaryBase
   * @param rowsLimit
   * @param rowsLimit maximum number of rows to let the algorithm process
   * 
   * @throws IllegalArgumentException when the {@code rowsLimit} is a negative number
   */
  public Configuration(final File input, final KnowledgeBase primaryBase, final int rowsLimit) {
    this(input, primaryBase, new Feedback(), rowsLimit);
  }
  
  /**
   * Creates configuration with provided feedback, which serves as hint for the processing algorithm.
   * 
   * @param input input specification
   * @param primaryBase primary knowledge base
   * @param feedback constraints for the algorithm
   * @param rowsLimit maximum number of rows to let the algorithm process
   * 
   * @throws IllegalArgumentException when the {@code rowsLimit} is a negative number
   */
  public Configuration(final File input, final KnowledgeBase primaryBase, final Feedback feedback, final int rowsLimit) {
    Preconditions.checkNotNull(input);
    Preconditions.checkNotNull(primaryBase);
    Preconditions.checkNotNull(feedback);
    
    Preconditions.checkArgument(rowsLimit >= 0);
    
    this.input = input;
    this.primaryBase = primaryBase;
    this.feedback = feedback;
    this.rowsLimit = rowsLimit;
  }

  /**
   * @return the input
   */
  public File getInput() {
    return input;
  }

  /**
   * @return the feedback
   */
  public Feedback getFeedback() {
    return feedback;
  }

  /**
   * @return the primary knowledge base
   */
  public KnowledgeBase getPrimaryBase() {
    return primaryBase;
  }

  /**
   * @return the maximum number of rows for the algorithm to process
   */
  public int getRowsLimit() {
    return rowsLimit;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + feedback.hashCode();
    result = prime * result + input.hashCode();
    result = prime * result + primaryBase.hashCode();
    result = prime * result + rowsLimit;
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Configuration other = (Configuration) obj;
    if (!feedback.equals(other.feedback)) {
      return false;
    }
    if (!input.equals(other.input)) {
      return false;
    }
    if (!primaryBase.equals(other.primaryBase)) {
      return false;
    }
    if (rowsLimit != other.rowsLimit) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Configuration [input=" + input + ", feedback=" + feedback + ", primaryBase="
        + primaryBase + ", rowsLimit=" + rowsLimit + "]";
  }
}
