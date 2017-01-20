package cz.cuni.mff.xrg.odalic.tasks.configurations;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ConfigurationAdapter;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import jersey.repackaged.com.google.common.collect.ImmutableSet;

/**
 * Task configuration.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(ConfigurationAdapter.class)
public final class Configuration implements Serializable {

  /**
   * Maximum number of rows that can be processed.
   */
  public static final int MAXIMUM_ROWS_LIMIT = Integer.MAX_VALUE;

  private static final long serialVersionUID = -6359038623760039155L;

  private final File input;

  private final Feedback feedback;

  private final Set<KnowledgeBase> usedBases;

  private final KnowledgeBase primaryBase;

  private final int rowsLimit;
  /**
   * Creates configuration with provided feedback, which serves as hint for the processing
   * algorithm.
   * 
   * @param input input specification
   * @param usedBases bases selected for the task
   * @param primaryBase primary knowledge base
   * @param feedback constraints for the algorithm, when {@code null}, a default empty
   *        {@link Feedback} is used
   * @param rowsLimit maximum number of rows to let the algorithm process
   * 
   * @throws IllegalArgumentException when the {@code rowsLimit} is a negative number or zero
   */
  public Configuration(final File input, final Set<? extends KnowledgeBase> usedBases,
      final KnowledgeBase primaryBase, final @Nullable Feedback feedback,
      @Nullable final Integer rowsLimit) {
    Preconditions.checkNotNull(input);
    Preconditions.checkNotNull(usedBases);
    Preconditions.checkNotNull(primaryBase);

    Preconditions.checkArgument(rowsLimit == null || rowsLimit > 0);
    Preconditions.checkArgument(usedBases.contains(primaryBase));

    this.input = input;
    this.usedBases = ImmutableSet.copyOf(usedBases);
    this.primaryBase = primaryBase;
    this.feedback = feedback == null ? new Feedback() : feedback;
    this.rowsLimit = rowsLimit == null ? MAXIMUM_ROWS_LIMIT : rowsLimit;
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
   * @return the bases selected for the task
   */
  public Set<KnowledgeBase> getUsedBases() {
    return usedBases;
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + feedback.hashCode();
    result = prime * result + input.hashCode();
    result = prime * result + usedBases.hashCode();
    result = prime * result + primaryBase.hashCode();
    result = prime * result + rowsLimit;
    return result;
  }

  /*
   * (non-Javadoc)
   * 
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
    if (!usedBases.equals(other.usedBases)) {
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Configuration [input=" + input + ", feedback=" + feedback + ", usedBases=" + usedBases
        + ", primaryBase=" + primaryBase + ", rowsLimit=" + rowsLimit + "]";
  }
}
