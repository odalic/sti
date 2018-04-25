package cz.cuni.mff.xrg.odalic.tasks.configurations;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.ConfigurationAdapter;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.files.File;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

  private final boolean useMLClassifier;

  private final File mlTrainingDatasetFile;

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
      @Nullable final Integer rowsLimit, @Nullable final Boolean statistical,
      @Nullable final Boolean useMLClassifier, @Nullable final File mlTrainingDatasetFile) {
    Preconditions.checkNotNull(input, "The input cannot be null!");
    Preconditions.checkNotNull(usedBases, "The usedBases cannot be null!");
    Preconditions.checkNotNull(primaryBase, "The primaryBase cannot be null!");

    Preconditions.checkArgument((rowsLimit == null) || (rowsLimit > 0), "The rows limit must be positive, if present!");
    Preconditions.checkArgument(usedBases.contains(primaryBase), "The primary base is not among the used ones!");

    Preconditions.checkArgument(trainingDatasetGivenIfUseMl(useMLClassifier, mlTrainingDatasetFile),
            "The ML Training Dataset file needs to be provided, if ML classifier should be used!");

    this.input = input;
    this.usedBases = ImmutableSet.copyOf(usedBases);
    this.primaryBase = primaryBase;
    this.feedback = feedback == null ? new Feedback() : feedback;
    this.rowsLimit = rowsLimit == null ? MAXIMUM_ROWS_LIMIT : rowsLimit;
    this.statistical = statistical == null ? false : statistical;
    this.useMLClassifier = useMLClassifier == null ? false : useMLClassifier;
    this.mlTrainingDatasetFile = mlTrainingDatasetFile;
  }

  private boolean trainingDatasetGivenIfUseMl(final Boolean useMLClassifier, final File mlTrainingDatasetFile) {
    if (useMLClassifier != null) {
      if (useMLClassifier) {
        return mlTrainingDatasetFile != null;
      }
      return true;
    }
    return true;

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Configuration that = (Configuration) o;
    return rowsLimit == that.rowsLimit &&
            statistical == that.statistical &&
            useMLClassifier == that.useMLClassifier &&
            Objects.equals(input, that.input) &&
            Objects.equals(feedback, that.feedback) &&
            Objects.equals(usedBases, that.usedBases) &&
            Objects.equals(primaryBase, that.primaryBase) &&
            Objects.equals(mlTrainingDatasetFile, that.mlTrainingDatasetFile);
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
    return Objects.hash(input, feedback, usedBases, primaryBase, rowsLimit, statistical, useMLClassifier, mlTrainingDatasetFile);
  }

  /**
   * @return true for processing of statistical data
   */
  public boolean isStatistical() {
    return this.statistical;
  }

  public boolean isUseMLClassifier() {
    return useMLClassifier;
  }

  public File getMlTrainingDatasetFile() {
    return mlTrainingDatasetFile;
  }

  @Override
  public String toString() {
    return "Configuration [input=" + this.input + ", feedback=" + this.feedback + ", usedBases="
        + this.usedBases + ", primaryBase=" + this.primaryBase + ", rowsLimit=" + this.rowsLimit
        + ", statistical=" + this.statistical + ", useMLClassifier= " + this.useMLClassifier
        + ", mlTrainingDatasetFile="  + this.mlTrainingDatasetFile +  "]";
  }
}
