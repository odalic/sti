package cz.cuni.mff.xrg.odalic.tasks.configurations;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ConfigurationAdapter;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.files.File;

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

  private final Set<String> usedBases;

  private final String primaryBase;

  private final int rowsLimit;

  private final boolean statistical;

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
   * @param statistical true for processing of statistical data
   *
   * @throws IllegalArgumentException when the {@code rowsLimit} is a negative number or zero
   */
  public Configuration(final File input, final Set<? extends String> usedBases,
      final String primaryBase, final @Nullable Feedback feedback,
      @Nullable final Integer rowsLimit, @Nullable final Boolean statistical) {
    Preconditions.checkNotNull(input);
    Preconditions.checkNotNull(usedBases);
    Preconditions.checkNotNull(primaryBase);

    Preconditions.checkArgument((rowsLimit == null) || (rowsLimit > 0));
    Preconditions.checkArgument(usedBases.contains(primaryBase));

    this.input = input;
    this.usedBases = ImmutableSet.copyOf(usedBases);
    this.primaryBase = primaryBase;
    this.feedback = feedback == null ? new Feedback() : feedback;
    this.rowsLimit = rowsLimit == null ? MAXIMUM_ROWS_LIMIT : rowsLimit;
    this.statistical = statistical == null ? false : statistical;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Configuration other = (Configuration) obj;
    if (!this.feedback.equals(other.feedback)) {
      return false;
    }
    if (!this.input.equals(other.input)) {
      return false;
    }
    if (!this.usedBases.equals(other.usedBases)) {
      return false;
    }
    if (!this.primaryBase.equals(other.primaryBase)) {
      return false;
    }
    if (this.rowsLimit != other.rowsLimit) {
      return false;
    }
    if (this.statistical != other.statistical) {
      return false;
    }
    return true;
  }

  /**
   * @return the feedback
   */
  public Feedback getFeedback() {
    return this.feedback;
  }

  /**
   * @return the input
   */
  public File getInput() {
    return this.input;
  }

  /**
   * @return the primary knowledge base
   */
  public String getPrimaryBase() {
    return this.primaryBase;
  }

  /**
   * @return the maximum number of rows for the algorithm to process
   */
  public int getRowsLimit() {
    return this.rowsLimit;
  }

  /**
   * @return the bases selected for the task
   */
  public Set<String> getUsedBases() {
    return this.usedBases;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.feedback.hashCode();
    result = (prime * result) + this.input.hashCode();
    result = (prime * result) + this.usedBases.hashCode();
    result = (prime * result) + this.primaryBase.hashCode();
    result = (prime * result) + this.rowsLimit;
    result = (prime * result) + (this.statistical ? 1231 : 1237);
    return result;
  }

  /**
   * @return true for processing of statistical data
   */
  public boolean isStatistical() {
    return this.statistical;
  }

  @Override
  public String toString() {
    return "Configuration [input=" + this.input + ", feedback=" + this.feedback + ", usedBases="
        + this.usedBases + ", primaryBase=" + this.primaryBase + ", rowsLimit=" + this.rowsLimit
        + ", statistical=" + this.statistical + "]";
  }
}
