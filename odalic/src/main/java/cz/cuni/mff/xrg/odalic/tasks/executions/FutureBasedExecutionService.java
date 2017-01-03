/**
 * 
 */
package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.feedbacks.FeedbackToConstraintsAdapter;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.files.formats.FormatService;
import cz.cuni.mff.xrg.odalic.input.CsvInputParser;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.InputToTableAdapter;
import cz.cuni.mff.xrg.odalic.input.ParsingResult;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
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

  private final TaskService taskService;
  private final FileService fileService;
  private final FormatService formatService;
  private final AnnotationToResultAdapter annotationResultAdapter;
  private final SemanticTableInterpreterFactory semanticTableInterpreterFactory;
  private final FeedbackToConstraintsAdapter feedbackToConstraintsAdapter;
  private final CsvInputParser csvInputParser;
  private final InputToTableAdapter inputToTableAdapter;
  private final ExecutorService executorService = Executors.newFixedThreadPool(1);
  private final Map<Task, Future<Result>> tasksToResults = new HashMap<>();

  @Autowired
  public FutureBasedExecutionService(final TaskService taskService, final FileService fileService,
      final FormatService formatService, final AnnotationToResultAdapter annotationToResultAdapter,
      final SemanticTableInterpreterFactory semanticTableInterpreterFactory,
      final FeedbackToConstraintsAdapter feedbackToConstraintsAdapter,
      final CsvInputParser csvInputParser, final InputToTableAdapter inputToTableAdapter) {
    Preconditions.checkNotNull(taskService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(formatService);
    Preconditions.checkNotNull(annotationToResultAdapter);
    Preconditions.checkNotNull(semanticTableInterpreterFactory);
    Preconditions.checkNotNull(feedbackToConstraintsAdapter);
    Preconditions.checkNotNull(csvInputParser);
    Preconditions.checkNotNull(inputToTableAdapter);

    this.taskService = taskService;
    this.fileService = fileService;
    this.formatService = formatService;
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
  public void submitForTaskId(String id) throws IllegalStateException {
    final Task task = taskService.getById(id);

    final Future<Result> resultFuture = tasksToResults.get(task);
    Preconditions.checkState(resultFuture == null || resultFuture.isDone());

    final Configuration configuration = task.getConfiguration();
    final File file = configuration.getInput();
    final String fileId = file.getId();

    final Callable<Result> execution = () -> {
      //TODO: DRY access to the input.
      
      final String data = fileService.getDataById(fileId);
      final Format format = formatService.getForFileId(fileId);

      final ParsingResult parsingResult = csvInputParser.parse(data, fileId, format, configuration.getRowsLimit());
      formatService.setForFileId(fileId, parsingResult.getFormat());

      final Input input = parsingResult.getInput();
      final Table table = inputToTableAdapter.toTable(input);

      final Map<String, SemanticTableInterpreter> interpreters =
          semanticTableInterpreterFactory.getInterpreters();

      Map<KnowledgeBase, TAnnotation> results = new HashMap<>();
      for (Map.Entry<String, SemanticTableInterpreter> interpreterEntry : interpreters.entrySet()) {
        final Constraints constraints = feedbackToConstraintsAdapter.toConstraints(
            configuration.getFeedback(), new KnowledgeBase(interpreterEntry.getKey()));
        final TAnnotation annotationResult = interpreterEntry.getValue().start(table, constraints);
        results.put(new KnowledgeBase(interpreterEntry.getKey()), annotationResult);
      }
      final Result result = annotationResultAdapter.toResult(results);

      return result;
    };

    final Future<Result> future = executorService.submit(execution);
    tasksToResults.put(task, future);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService#getResultForTaskId(java.lang.String)
   */
  @Override
  public Result getResultForTaskId(String id)
      throws InterruptedException, ExecutionException, CancellationException {
    final Task task = taskService.getById(id);
    final Future<Result> resultFuture = tasksToResults.get(task);

    Preconditions.checkArgument(resultFuture != null);

    return resultFuture.get();
  }

  @Override
  public void cancelForTaskId(String id) {
    final Task task = taskService.getById(id);
    final Future<Result> resultFuture = tasksToResults.get(task);

    Preconditions.checkArgument(resultFuture != null);

    Preconditions.checkState(resultFuture.cancel(true));
  }

  @Override
  public boolean isDoneForTaskId(String id) {
    final Task task = taskService.getById(id);
    final Future<Result> resultFuture = tasksToResults.get(task);

    Preconditions.checkArgument(resultFuture != null);

    return resultFuture.isDone();
  }

  @Override
  public boolean isCanceledForTaskId(String id) {
    final Task task = taskService.getById(id);
    final Future<Result> resultFuture = tasksToResults.get(task);

    Preconditions.checkArgument(resultFuture != null);

    return resultFuture.isCancelled();
  }

  @Override
  public boolean hasBeenScheduledForTaskId(String id) {
    final Task task = taskService.getById(id);
    final Future<Result> resultFuture = tasksToResults.get(task);

    return resultFuture != null;
  }

  @Override
  public boolean isSuccessForTasksId(String id) {
    if (!isDoneForTaskId(id)) {
      return false;
    }

    if (isCanceledForTaskId(id)) {
      return false;
    }

    try {
      final Result result = getResultForTaskId(id);

      return result.getWarnings().isEmpty();
    } catch (final InterruptedException | ExecutionException e) {
      return false;
    }
  }

  @Override
  public boolean isWarnedForTasksId(String id) {
    if (!isDoneForTaskId(id)) {
      return false;
    }

    if (isCanceledForTaskId(id)) {
      return false;
    }

    try {
      final Result result = getResultForTaskId(id);

      return !result.getWarnings().isEmpty();
    } catch (final InterruptedException | ExecutionException e) {
      return false;
    }
  }

  @Override
  public boolean hasFailedForTasksId(String id) {
    if (!isDoneForTaskId(id)) {
      return false;
    }

    if (isCanceledForTaskId(id)) {
      return false;
    }

    try {
      getResultForTaskId(id);

      return false;
    } catch (final InterruptedException | ExecutionException e) {
      return true;
    }
  }
}
