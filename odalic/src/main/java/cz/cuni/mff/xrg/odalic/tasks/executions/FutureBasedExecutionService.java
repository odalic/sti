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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.feedbacks.FeedbackToConstraintsAdapter;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.input.CsvInputParser;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.InputToTableAdapter;
import cz.cuni.mff.xrg.odalic.input.ParsingResult;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.FeedbackService;
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

  private final TaskService taskService;
  private final FeedbackService feedbackService;
  private final FileService fileService;
  private final AnnotationToResultAdapter annotationResultAdapter;
  private final SemanticTableInterpreterFactory semanticTableInterpreterFactory;
  private final FeedbackToConstraintsAdapter feedbackToConstraintsAdapter;
  private final CsvInputParser csvInputParser;
  private final InputToTableAdapter inputToTableAdapter;
  private final ExecutorService executorService = Executors.newFixedThreadPool(1);

  private final Map<Task, Future<Result>> tasksToResults = new HashMap<>();

  @Autowired
  public FutureBasedExecutionService(final TaskService taskService, final FeedbackService feedbackService, final FileService fileService,
      final AnnotationToResultAdapter annotationToResultAdapter,
      final SemanticTableInterpreterFactory semanticTableInterpreterFactory,
      final FeedbackToConstraintsAdapter feedbackToConstraintsAdapter,
      final CsvInputParser csvInputParser, final InputToTableAdapter inputToTableAdapter) {
    Preconditions.checkNotNull(taskService);
    Preconditions.checkNotNull(feedbackService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(annotationToResultAdapter);
    Preconditions.checkNotNull(semanticTableInterpreterFactory);
    Preconditions.checkNotNull(feedbackToConstraintsAdapter);
    Preconditions.checkNotNull(csvInputParser);
    Preconditions.checkNotNull(inputToTableAdapter);

    this.taskService = taskService;
    this.feedbackService = feedbackService;
    this.fileService = fileService;
    this.annotationResultAdapter = annotationToResultAdapter;
    this.semanticTableInterpreterFactory = semanticTableInterpreterFactory;
    this.feedbackToConstraintsAdapter = feedbackToConstraintsAdapter;
    this.csvInputParser = csvInputParser;
    this.inputToTableAdapter = inputToTableAdapter;
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService#submitForTaskId(java.lang.String)
   */
  @Override
  public void submitForTaskId(String userId, String taskId)
      throws IllegalStateException, IOException {
    final Task task = taskService.getById(userId, taskId);

    checkNotAlreadyScheduled(task);

    final Configuration configuration = task.getConfiguration();

    final String fileId = configuration.getInput().getId();
    final Feedback feedback = configuration.getFeedback();
    final Set<KnowledgeBase> usedBases = configuration.getUsedBases();
    final int rowsLimit = configuration.getRowsLimit();

    final ParsingResult parsingResult = parse(userId, fileId, rowsLimit);
    final Input input = parsingResult.getInput();

    feedbackService.setInputSnapshotForTaskid(userId, taskId, input);

    final Callable<Result> execution = () -> {
      try {
        final Table table = inputToTableAdapter.toTable(input);
        final boolean isStatistical = configuration.isStatistical();

        final Map<String, SemanticTableInterpreter> interpreters =
            semanticTableInterpreterFactory.getInterpreters();

        final Map<KnowledgeBase, TAnnotation> results = new HashMap<>();

        for (Map.Entry<String, SemanticTableInterpreter> interpreterEntry : interpreters
            .entrySet()) {
          final KnowledgeBase base = new KnowledgeBase(interpreterEntry.getKey());
          if (!usedBases.contains(base)) {
            continue;
          }

          final Constraints constraints =
              feedbackToConstraintsAdapter.toConstraints(feedback, base);
          final SemanticTableInterpreter interpreter = interpreterEntry.getValue();

          final TAnnotation annotationResult = interpreter.start(table, isStatistical, constraints);

          results.put(base, annotationResult);
        }

        return annotationResultAdapter.toResult(results);
      } catch (final Exception e) {
        logger.error("Error during task execution!", e);

        throw e;
      }
    };

    final Future<Result> future = executorService.submit(execution);
    tasksToResults.put(task, future);
  }

  private ParsingResult parse(final String userId, final String fileId, final int rowsLimit)
      throws IOException {
    final String data = fileService.getDataById(userId, fileId);
    final Format format = fileService.getFormatForFileId(userId, fileId);

    final ParsingResult result = csvInputParser.parse(data, fileId, format, rowsLimit);
    fileService.setFormatForFileId(userId, fileId, result.getFormat());

    return result;
  }

  private void checkNotAlreadyScheduled(final Task task) {
    final Future<Result> resultFuture = tasksToResults.get(task);
    Preconditions.checkState(resultFuture == null || resultFuture.isDone());
  }


  @Override
  public void unscheduleForTaskId(String userId, String fileId) {
    final Task task = taskService.getById(userId, fileId);
    final Future<Result> resultFuture = tasksToResults.remove(task);
    if (resultFuture == null) {
      return;
    }

    resultFuture.cancel(false);
  }

  @Override
  public Result getResultForTaskId(String userId, String taskId)
      throws InterruptedException, ExecutionException, CancellationException {
    final Task task = taskService.getById(userId, taskId);
    final Future<Result> resultFuture = tasksToResults.get(task);

    Preconditions.checkArgument(resultFuture != null);

    return resultFuture.get();
  }

  @Override
  public void cancelForTaskId(String userId, String taskId) {
    final Task task = taskService.getById(userId, taskId);
    final Future<Result> resultFuture = tasksToResults.get(task);

    Preconditions.checkArgument(resultFuture != null);

    Preconditions.checkState(resultFuture.cancel(false));
  }

  @Override
  public boolean isDoneForTaskId(String userId, String taskId) {
    final Task task = taskService.getById(userId, taskId);
    final Future<Result> resultFuture = tasksToResults.get(task);

    Preconditions.checkArgument(resultFuture != null);

    return resultFuture.isDone();
  }

  @Override
  public boolean isCanceledForTaskId(String userId, String taskId) {
    final Task task = taskService.getById(userId, taskId);
    final Future<Result> resultFuture = tasksToResults.get(task);

    Preconditions.checkArgument(resultFuture != null);

    return resultFuture.isCancelled();
  }

  @Override
  public boolean hasBeenScheduledForTaskId(String userId, String taskId) {
    final Task task = taskService.getById(userId, taskId);
    final Future<Result> resultFuture = tasksToResults.get(task);

    return resultFuture != null;
  }

  @Override
  public boolean isSuccessForTasksId(String userId, String taskId) {
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

  @Override
  public boolean isWarnedForTasksId(String userId, String taskId) {
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
  public boolean hasFailedForTasksId(String userId, String taskId) {
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
}
