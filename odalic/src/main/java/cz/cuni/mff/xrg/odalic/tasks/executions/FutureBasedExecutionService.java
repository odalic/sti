/**
 *
 */
package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.input.ml.TaskMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.feedbacks.FeedbackToConstraintsAdapter;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.input.CsvInputParser;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.InputToTableAdapter;
import cz.cuni.mff.xrg.odalic.input.ParsingResult;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.snapshots.InputSnapshotsService;
import cz.cuni.mff.xrg.odalic.tasks.results.AnnotationToResultAdapter;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * <p>
 * Implementation of {@link ExecutionService} based on {@link Future} and {@link ExecutorService}
 * implementations.
 * </p>
 *
 * <p>
 * Provides no persistence whatsoever
 * </p>
 *
 * @author Václav Brodec
 *
 */
public final class FutureBasedExecutionService implements ExecutionService {

  private static final Logger logger = LoggerFactory.getLogger(FutureBasedExecutionService.class);

  private final ConfigurationService configurationService;
  private final InputSnapshotsService inputSnapshotsService;
  private final FileService fileService;
  private final BasesService basesService;
  private final AnnotationToResultAdapter annotationResultAdapter;
  private final SemanticTableInterpreterFactory semanticTableInterpreterFactory;
  private final FeedbackToConstraintsAdapter feedbackToConstraintsAdapter;
  private final CsvInputParser csvInputParser;
  private final InputToTableAdapter inputToTableAdapter;
  private final ExecutorService executorService = Executors.newFixedThreadPool(1);

  /**
   * Table of result futures where rows are indexed by user IDs and the columns by task IDs.
   */
  private final com.google.common.collect.Table<String, String, Future<Result>> userTaskIdsToResults;

  @Autowired
  public FutureBasedExecutionService(final ConfigurationService configurationService,
      final InputSnapshotsService inputSnapshotsService, final FileService fileService,
      final BasesService basesService, final AnnotationToResultAdapter annotationToResultAdapter,
      final SemanticTableInterpreterFactory semanticTableInterpreterFactory,
      final FeedbackToConstraintsAdapter feedbackToConstraintsAdapter,
      final CsvInputParser csvInputParser, final InputToTableAdapter inputToTableAdapter) {
    Preconditions.checkNotNull(configurationService, "The configurationService cannot be null!");
    Preconditions.checkNotNull(inputSnapshotsService, "The inputSnapshotsService cannot be null!");
    Preconditions.checkNotNull(fileService, "The fileService cannot be null!");
    Preconditions.checkNotNull(basesService, "The basesService cannot be null!");
    Preconditions.checkNotNull(annotationToResultAdapter, "The annotationToResultAdapter cannot be null!");
    Preconditions.checkNotNull(semanticTableInterpreterFactory, "The semanticTableInterpreterFactory cannot be null!");
    Preconditions.checkNotNull(feedbackToConstraintsAdapter, "The feedbackToConstraintsAdapter cannot be null!");
    Preconditions.checkNotNull(csvInputParser, "The csvInputParser cannot be null!");
    Preconditions.checkNotNull(inputToTableAdapter, "The inputToTableAdapter cannot be null!");

    this.configurationService = configurationService;
    this.inputSnapshotsService = inputSnapshotsService;
    this.fileService = fileService;
    this.basesService = basesService;
    this.annotationResultAdapter = annotationToResultAdapter;
    this.semanticTableInterpreterFactory = semanticTableInterpreterFactory;
    this.feedbackToConstraintsAdapter = feedbackToConstraintsAdapter;
    this.csvInputParser = csvInputParser;
    this.inputToTableAdapter = inputToTableAdapter;

    this.userTaskIdsToResults = HashBasedTable.create();
  }

  @Override
  public void cancelForTaskId(final String userId, final String taskId) {
    final Future<Result> resultFuture = this.userTaskIdsToResults.get(userId, taskId);

    Preconditions.checkArgument(resultFuture != null, String.format("There is no scheduled execution of task %s registered to user %s", taskId, userId));

    Preconditions.checkState(resultFuture.cancel(false), String.format("The task %s could not be canceled!", taskId));
  }

  @Override
  public Result getResultForTaskId(final String userId, final String taskId)
      throws InterruptedException, ExecutionException, CancellationException {
    final Future<Result> resultFuture = this.userTaskIdsToResults.get(userId, taskId);

    Preconditions.checkArgument(resultFuture != null, String.format("There is no scheduled execution of task %s registered to user %s", taskId, userId));

    return resultFuture.get();
  }

  @Override
  public boolean hasBeenScheduledForTaskId(final String userId, final String taskId) {
    final Future<Result> resultFuture = this.userTaskIdsToResults.get(userId, taskId);

    return resultFuture != null;
  }


  @Override
  public boolean hasBeenWarnedForTaskId(final String userId, final String taskId) {
    if (!isDoneForTaskId(userId, taskId)) {
      return false;
    }

    if (isCanceledForTaskId(userId, taskId)) {
      return false;
    }

    try {
      final Result result = getResultForTaskId(userId, taskId);

      return !result.getWarnings().isEmpty();
    } catch (final InterruptedException | ExecutionException e) {
      return false;
    }
  }

  @Override
  public boolean hasFailedForTaskId(final String userId, final String taskId) {
    if (!isDoneForTaskId(userId, taskId)) {
      return false;
    }

    if (isCanceledForTaskId(userId, taskId)) {
      return false;
    }

    try {
      getResultForTaskId(userId, taskId);

      return false;
    } catch (final InterruptedException | ExecutionException e) {
      return true;
    }
  }

  private void checkNotAlreadyScheduled(final String userId, final String taskId) {
    final Future<Result> resultFuture = this.userTaskIdsToResults.get(userId, taskId);
    Preconditions.checkState((resultFuture == null) || resultFuture.isDone(), String.format("The task %s is already scheduled and in progress!", taskId));
  }

  @Override
  public boolean isCanceledForTaskId(final String userId, final String taskId) {
    final Future<Result> resultFuture = this.userTaskIdsToResults.get(userId, taskId);

    Preconditions.checkArgument(resultFuture != null, String.format("There is no scheduled execution of task %s registered to user %s", taskId, userId));

    return resultFuture.isCancelled();
  }

  @Override
  public boolean isDoneForTaskId(final String userId, final String taskId) {
    final Future<Result> resultFuture = this.userTaskIdsToResults.get(userId, taskId);

    Preconditions.checkArgument(resultFuture != null, String.format("There is no scheduled execution of task %s registered to user %s", taskId, userId));

    return resultFuture.isDone();
  }

  @Override
  public boolean isSuccessForTasksId(final String userId, final String taskId) {
    if (!isDoneForTaskId(userId, taskId)) {
      return false;
    }

    if (isCanceledForTaskId(userId, taskId)) {
      return false;
    }

    try {
      final Result result = getResultForTaskId(userId, taskId);

      return result.getWarnings().isEmpty();
    } catch (final InterruptedException | ExecutionException e) {
      return false;
    }
  }

  private ParsingResult parse(final String userId, final String fileId, final int rowsLimit)
      throws IOException {
    final String data = this.fileService.getDataById(userId, fileId);
    final Format format = this.fileService.getFormatForFileId(userId, fileId);

    final ParsingResult result = this.csvInputParser.parse(data, fileId, format, rowsLimit);
    this.fileService.setFormatForFileId(userId, fileId, result.getFormat());

    return result;
  }

  private File loadMLTrainingDatasetFile(final String userId, final String fileId) {
    return this.fileService.getById(userId, fileId);
  }

  private TaskMLConfiguration createTaskMLConfiguration(final String userId, final String fileId,
                                                        final Configuration configuration) throws IOException {
    // load ml training dataset file & its format
    TaskMLConfiguration taskMlConfig;
    if (configuration.isUseMLClassifier()) {
      ParsingResult trainingDatasetParsed = parse(userId, fileId, Configuration.MAXIMUM_ROWS_LIMIT);
      taskMlConfig = new TaskMLConfiguration(configuration.isUseMLClassifier(), trainingDatasetParsed);
    } else {
      taskMlConfig = TaskMLConfiguration.disabled();
    }
    return taskMlConfig;
  }

  @Override
  public void submitForTaskId(final String userId, final String taskId)
      throws IllegalStateException, IOException {
    checkNotAlreadyScheduled(userId, taskId);

    final Configuration configuration = this.configurationService.getForTaskId(userId, taskId);

    final String fileId = configuration.getInput().getId();
    final Feedback feedback = configuration.getFeedback();
    final Set<String> usedBaseNames = configuration.getUsedBases();
    final int rowsLimit = configuration.getRowsLimit();

    final ParsingResult parsingResult = parse(userId, fileId, rowsLimit);
    final Input input = parsingResult.getInput();

    final TaskMLConfiguration taskMlConfig = createTaskMLConfiguration(userId, fileId, configuration);

    this.inputSnapshotsService.setInputSnapshotForTaskid(userId, taskId, input);

    final Callable<Result> execution = () -> {
      try {
        final Table table = this.inputToTableAdapter.toTable(input);
        final boolean isStatistical = configuration.isStatistical();

        final Set<KnowledgeBase> usedBases =
            usedBaseNames.stream().map(e -> this.basesService.getByName(userId, e))
                .collect(ImmutableSet.toImmutableSet());

        final Map<String, SemanticTableInterpreter> interpreters =
            this.semanticTableInterpreterFactory.getInterpreters(userId, usedBases, taskMlConfig);

        final Map<KnowledgeBase, TAnnotation> results = new HashMap<>();

        for (final Map.Entry<String, SemanticTableInterpreter> interpreterEntry : interpreters
            .entrySet()) {
          final KnowledgeBase base = this.basesService.getByName(userId, interpreterEntry.getKey());

          final Constraints constraints =
              this.feedbackToConstraintsAdapter.toConstraints(feedback, base);
          final SemanticTableInterpreter interpreter = interpreterEntry.getValue();

          final TAnnotation annotationResult = interpreter.start(table, isStatistical, constraints);

          results.put(base, annotationResult);
        }

        return this.annotationResultAdapter.toResult(results);
      } catch (final Exception e) {
        logger.error("Error during task execution!", e);

        throw e;
      }
    };

    final Future<Result> future = this.executorService.submit(execution);
    this.userTaskIdsToResults.put(userId, taskId, future);
  }

  @Override
  public void unscheduleAll(final String userId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");

    this.userTaskIdsToResults.row(userId).entrySet().stream()
        .forEach(e -> e.getValue().cancel(false));
  }

  @Override
  public void unscheduleForTaskId(final String userId, final String taskId) {
    final Future<Result> resultFuture = this.userTaskIdsToResults.remove(userId, taskId);
    if (resultFuture == null) {
      return;
    }

    resultFuture.cancel(false);
  }

  @Override
  public void mergeWithResultForTaskId(String userId, String taskId, Feedback feedback) {
    throw new UnsupportedOperationException(); //TODO: Implement.
  }
}
