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

import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.feedbacks.FeedbackToConstraintsAdapter;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.input.CsvInputParser;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.InputToTableAdapter;
import cz.cuni.mff.xrg.odalic.input.ParsingResult;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.FeedbackService;
import cz.cuni.mff.xrg.odalic.tasks.results.AnnotationToResultAdapter;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import cz.cuni.mff.xrg.odalic.util.storage.DbService;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * Implementation of {@link ExecutionService} based on {@link Future} and {@link ExecutorService}
 * implementations. Stores the latest computed results using {@link DB}-backed map.
 * 
 * @author Václav Brodec
 *
 */
public final class DbCachedFutureBasedExecutionService implements ExecutionService {

  private static final Logger logger =
      LoggerFactory.getLogger(DbCachedFutureBasedExecutionService.class);

  private final ConfigurationService configurationService;
  private final FeedbackService feedbackService;
  private final FileService fileService;
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
  
  /**
   * The shared database instance.
   */
  private final DB db;

  /**
   * Table of cached results where rows are indexed by user IDs and the columns by task IDs
   * (represented as an array of size 2).
   */
  private final Map<Object[], Result> userTaskIdsToCachedResults;

  @SuppressWarnings("unchecked")
  @Autowired
  public DbCachedFutureBasedExecutionService(final ConfigurationService configurationService,
      final FeedbackService feedbackService, final FileService fileService,
      final DbService dbService, final AnnotationToResultAdapter annotationToResultAdapter,
      final SemanticTableInterpreterFactory semanticTableInterpreterFactory,
      final FeedbackToConstraintsAdapter feedbackToConstraintsAdapter,
      final CsvInputParser csvInputParser, final InputToTableAdapter inputToTableAdapter) {
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(feedbackService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(dbService);
    Preconditions.checkNotNull(annotationToResultAdapter);
    Preconditions.checkNotNull(semanticTableInterpreterFactory);
    Preconditions.checkNotNull(feedbackToConstraintsAdapter);
    Preconditions.checkNotNull(csvInputParser);
    Preconditions.checkNotNull(inputToTableAdapter);

    this.configurationService = configurationService;
    this.feedbackService = feedbackService;
    this.fileService = fileService;
    this.annotationResultAdapter = annotationToResultAdapter;
    this.semanticTableInterpreterFactory = semanticTableInterpreterFactory;
    this.feedbackToConstraintsAdapter = feedbackToConstraintsAdapter;
    this.csvInputParser = csvInputParser;
    this.inputToTableAdapter = inputToTableAdapter;

    this.userTaskIdsToResults = HashBasedTable.create();

    this.db = dbService.getDb();

    this.userTaskIdsToCachedResults = db.treeMap("userTaskIdsToCachedResults")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
  }

  @Override
  public void submitForTaskId(String userId, String taskId)
      throws IllegalStateException, IOException {
    checkNotAlreadyScheduled(userId, taskId);

    final Configuration configuration = configurationService.getForTaskId(userId, taskId);

    final String fileId = configuration.getInput().getId();
    final Feedback feedback = configuration.getFeedback();
    final Set<KnowledgeBase> usedBases = configuration.getUsedBases();
    final int rowsLimit = configuration.getRowsLimit();

    final ParsingResult parsingResult = parse(userId, fileId, rowsLimit);
    final Input input = parsingResult.getInput();

    feedbackService.setInputSnapshotForTaskid(userId, taskId, input);
    db.commit();

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

        final Result result = annotationResultAdapter.toResult(results);
        
        userTaskIdsToCachedResults.put(new Object[] { userId, taskId }, result);
        db.commit();
        
        return result;
      } catch (final Exception e) {
        logger.error("Error during task execution!", e);

        throw e;
      }
    };

    userTaskIdsToCachedResults.remove(new Object[] { userId, taskId });
    db.commit();
    
    final Future<Result> future = executorService.submit(execution);
    userTaskIdsToResults.put(userId, taskId, future);
  }

  private ParsingResult parse(final String userId, final String fileId, final int rowsLimit)
      throws IOException {
    final String data = fileService.getDataById(userId, fileId);
    final Format format = fileService.getFormatForFileId(userId, fileId);

    final ParsingResult result = csvInputParser.parse(data, fileId, format, rowsLimit);
    fileService.setFormatForFileId(userId, fileId, result.getFormat());

    return result;
  }

  private void checkNotAlreadyScheduled(final String userId, final String taskId) {
    final Future<Result> resultFuture = userTaskIdsToResults.get(userId, taskId);
    Preconditions.checkState(resultFuture == null || resultFuture.isDone());
  }


  @Override
  public void unscheduleForTaskId(String userId, String taskId) {
    userTaskIdsToCachedResults.remove(new Object[] { userId, taskId });
    db.commit();
    
    final Future<Result> resultFuture = userTaskIdsToResults.remove(userId, taskId);
    if (resultFuture == null) {
      return;
    }

    resultFuture.cancel(false);
  }

  @Override
  public Result getResultForTaskId(String userId, String taskId)
      throws InterruptedException, ExecutionException, CancellationException {
    final Result cachedResult = this.userTaskIdsToCachedResults.get(new Object[] { userId, taskId });
    if (cachedResult != null) {
      return cachedResult;
    }
    
    final Future<Result> resultFuture = userTaskIdsToResults.get(userId, taskId);

    Preconditions.checkArgument(resultFuture != null);

    return resultFuture.get();
  }

  @Override
  public void cancelForTaskId(String userId, String taskId) {
    final Future<Result> resultFuture = userTaskIdsToResults.get(userId, taskId);

    Preconditions.checkArgument(resultFuture != null);

    userTaskIdsToCachedResults.remove(new Object[] { userId, taskId });
    db.commit();
    Preconditions.checkState(resultFuture.cancel(false));
  }

  @Override
  public boolean isDoneForTaskId(String userId, String taskId) {
    if (this.userTaskIdsToCachedResults.containsKey(new Object[] { userId, taskId })) {
      return true;
    }
    
    final Future<Result> resultFuture = userTaskIdsToResults.get(userId, taskId);

    Preconditions.checkArgument(resultFuture != null);

    return resultFuture.isDone();
  }

  @Override
  public boolean isCanceledForTaskId(String userId, String taskId) {
    final Future<Result> resultFuture = userTaskIdsToResults.get(userId, taskId);
    if (resultFuture == null) {
      return false;
    }

    return resultFuture.isCancelled();
  }

  @Override
  public boolean hasBeenScheduledForTaskId(String userId, String taskId) {
    final Future<Result> resultFuture = userTaskIdsToResults.get(userId, taskId);

    return resultFuture != null || this.userTaskIdsToCachedResults.containsKey(new Object[] { userId, taskId });
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
  public boolean hasBeenWarnedForTaskId(String userId, String taskId) {
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
  public boolean hasFailedForTaskId(String userId, String taskId) {
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