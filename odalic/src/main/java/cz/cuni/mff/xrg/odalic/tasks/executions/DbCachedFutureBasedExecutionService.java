/**
 *
 */
package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.input.ml.TaskMLConfiguration;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.values.ColumnProcessingTypeValue;
import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.feedbacks.Ambiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.Classification;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnAmbiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnCompulsory;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnRelation;
import cz.cuni.mff.xrg.odalic.feedbacks.DataCubeComponent;
import cz.cuni.mff.xrg.odalic.feedbacks.Disambiguation;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.feedbacks.FeedbackToConstraintsAdapter;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.input.CsvInputParser;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.InputToTableAdapter;
import cz.cuni.mff.xrg.odalic.input.ParsingResult;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnProcessingAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.snapshots.InputSnapshotsService;
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

  /**
   * The shared database instance.
   */
  private final DB db;

  /**
   * Table of cached results where rows are indexed by user IDs and the columns by task IDs
   * (represented as an array of size 2).
   */
  private final BTreeMap<Object[], Result> userTaskIdsToCachedResults;

  @SuppressWarnings("unchecked")
  @Autowired
  public DbCachedFutureBasedExecutionService(final ConfigurationService configurationService,
      final InputSnapshotsService inputSnapshotsService, final FileService fileService,
      final DbService dbService, final BasesService basesService,
      final AnnotationToResultAdapter annotationToResultAdapter,
      final SemanticTableInterpreterFactory semanticTableInterpreterFactory,
      final FeedbackToConstraintsAdapter feedbackToConstraintsAdapter,
      final CsvInputParser csvInputParser, final InputToTableAdapter inputToTableAdapter) {
    Preconditions.checkNotNull(configurationService, "The configurationService cannot be null!");
    Preconditions.checkNotNull(inputSnapshotsService, "The inputSnapshotsService cannot be null!");
    Preconditions.checkNotNull(fileService, "The fileService cannot be null!");
    Preconditions.checkNotNull(basesService, "The basesService cannot be null!");
    Preconditions.checkNotNull(dbService, "The dbService cannot be null!");
    Preconditions.checkNotNull(annotationToResultAdapter,
        "The annotationToResultAdapter cannot be null!");
    Preconditions.checkNotNull(semanticTableInterpreterFactory,
        "The semanticTableInterpreterFactory cannot be null!");
    Preconditions.checkNotNull(feedbackToConstraintsAdapter,
        "The feedbackToConstraintsAdapter cannot be null!");
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

    this.db = dbService.getDb();

    this.userTaskIdsToCachedResults = this.db.treeMap("userTaskIdsToCachedResults")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
  }

  @Override
  public void cancelForTaskId(final String userId, final String taskId) {
    final Future<Result> resultFuture = this.userTaskIdsToResults.get(userId, taskId);

    Preconditions.checkArgument(resultFuture != null, String.format(
        "There is no scheduled execution of task %s registered to user %s", taskId, userId));

    this.userTaskIdsToCachedResults.remove(new Object[] {userId, taskId});
    this.db.commit();
    Preconditions.checkState(resultFuture.cancel(false),
        String.format("The task %s could not be canceled!", taskId));
  }

  @Override
  public Result getResultForTaskId(final String userId, final String taskId)
      throws InterruptedException, ExecutionException, CancellationException {
    final Result cachedResult = this.userTaskIdsToCachedResults.get(new Object[] {userId, taskId});
    if (cachedResult != null) {
      return cachedResult;
    }

    final Future<Result> resultFuture = this.userTaskIdsToResults.get(userId, taskId);

    Preconditions.checkArgument(resultFuture != null, String.format(
        "There is no scheduled execution of task %s registered to user %s", taskId, userId));

    return resultFuture.get();
  }

  @Override
  public boolean hasBeenScheduledForTaskId(final String userId, final String taskId) {
    final Future<Result> resultFuture = this.userTaskIdsToResults.get(userId, taskId);

    return (resultFuture != null)
        || this.userTaskIdsToCachedResults.containsKey(new Object[] {userId, taskId});
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
    Preconditions.checkState((resultFuture == null) || resultFuture.isDone(),
        String.format("The task %s is already scheduled and in progress!", taskId));
  }

  @Override
  public boolean isCanceledForTaskId(final String userId, final String taskId) {
    final Future<Result> resultFuture = this.userTaskIdsToResults.get(userId, taskId);
    if (resultFuture == null) {
      return false;
    }

    return resultFuture.isCancelled();
  }

  @Override
  public boolean isDoneForTaskId(final String userId, final String taskId) {
    if (this.userTaskIdsToCachedResults.containsKey(new Object[] {userId, taskId})) {
      return true;
    }

    final Future<Result> resultFuture = this.userTaskIdsToResults.get(userId, taskId);

    Preconditions.checkArgument(resultFuture != null, String.format(
        "There is no scheduled execution of task %s registered to user %s", taskId, userId));

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

  private TaskMLConfiguration createTaskMLConfiguration(final String userId, final String fileId,
                                                        final Configuration configuration) throws IOException {
    // load ml training dataset file & its format
    TaskMLConfiguration taskMlConfig;
    if (configuration.isUseMLClassifier()) {
      final String data = this.fileService.getDataById(userId, configuration.getMlTrainingDatasetFile().getId());
      final Format format = this.fileService.getFormatForFileId(userId, configuration.getMlTrainingDatasetFile().getId());

      taskMlConfig = new TaskMLConfiguration(configuration.isUseMLClassifier(), format, data);
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

    // load ml training dataset file & its format
    final TaskMLConfiguration taskMlConfig = createTaskMLConfiguration(userId, fileId, configuration);

    this.inputSnapshotsService.setInputSnapshotForTaskid(userId, taskId, input);
    this.db.commit();

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

        final Result result = this.annotationResultAdapter.toResult(results);

        this.userTaskIdsToCachedResults.put(new Object[] {userId, taskId}, result);
        this.db.commit();

        return result;
      } catch (final Exception e) {
        logger.error("Error during task execution!", e);

        throw e;
      }
    };

    this.userTaskIdsToCachedResults.remove(new Object[] {userId, taskId});
    this.db.commit();

    final Future<Result> future = this.executorService.submit(execution);
    this.userTaskIdsToResults.put(userId, taskId, future);
  }

  @Override
  public void unscheduleAll(final String userId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");

    this.userTaskIdsToCachedResults.prefixSubMap(new Object[] {userId}).clear();
    this.db.commit();

    this.userTaskIdsToResults.row(userId).entrySet().stream()
        .forEach(e -> e.getValue().cancel(false));
  }

  @Override
  public void unscheduleForTaskId(final String userId, final String taskId) {
    this.userTaskIdsToCachedResults.remove(new Object[] {userId, taskId});
    this.db.commit();

    final Future<Result> resultFuture = this.userTaskIdsToResults.remove(userId, taskId);
    if (resultFuture == null) {
      return;
    }

    resultFuture.cancel(false);
  }

  @Override
  public void mergeWithResultForTaskId(String userId, String taskId, Feedback feedback) {
    final Object[] userTaskId = new Object[] {userId, taskId};

    final Result previousResult = this.userTaskIdsToCachedResults.get(userTaskId);
    Preconditions.checkState(previousResult != null, "There is no cached result!");

    final List<HeaderAnnotation> headerAnnotations =
        mergeHeaderAnnotations(previousResult, feedback);
    final CellAnnotation[][] cellAnnotations = mergeCellAnnotations(previousResult, feedback);
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> columnRelationAnnotations =
        mergeColumnRelationAnnotations(previousResult, feedback);
    final List<StatisticalAnnotation> statisticalAnnotations =
        mergeStatisticalAnnotations(previousResult, feedback);
    final List<ColumnProcessingAnnotation> columnProcessingAnnotations =
        mergeColumnProcessingAnnotations(previousResult, feedback);

    final Result mergedResult = new Result(previousResult.getSubjectColumnsPositions(),
        headerAnnotations, cellAnnotations, columnRelationAnnotations, statisticalAnnotations,
        columnProcessingAnnotations, previousResult.getWarnings());

    this.userTaskIdsToCachedResults.put(userTaskId, mergedResult);
    this.db.commit();
  }

  private static List<ColumnProcessingAnnotation> mergeColumnProcessingAnnotations(
      Result previousResult, Feedback feedback) {
    final List<ColumnProcessingAnnotation> original =
        previousResult.getColumnProcessingAnnotations();
    final Set<ColumnCompulsory> feedbackSet = feedback.getColumnCompulsory();
    final Set<ColumnIgnore> feedbackIgnores = feedback.getColumnIgnores();
    final Map<ColumnPosition, ColumnCompulsory> indicesToCompulsoryFeedback =
        feedbackSet.stream().collect(Collectors.toMap(e -> e.getPosition(), Function.identity()));
    final Map<ColumnPosition, ColumnIgnore> indicesToIgnores = feedbackIgnores.stream()
        .collect(Collectors.toMap(e -> e.getPosition(), Function.identity()));

    return IntStream.range(0, original.size()).mapToObj(index -> {
      final ColumnProcessingAnnotation originalAnnotation = original.get(index);
      final ColumnCompulsory compulsory =
          indicesToCompulsoryFeedback.get(new ColumnPosition(index));
      final ColumnIgnore feedbackIgnore = indicesToIgnores.get(new ColumnPosition(index));

      if (compulsory == null) {
        if (feedbackIgnore == null) {
          return originalAnnotation;
        } else {
          return new ColumnProcessingAnnotation(
              originalAnnotation.getProcessingType().entrySet().stream().collect(ImmutableMap
                  .toImmutableMap(e -> e.getKey(), e -> ColumnProcessingTypeValue.IGNORED)));
        }
      } else {
        Preconditions.checkArgument(feedbackIgnore == null,
            String.format(
                "Invalid feedback for column %d: the compulsory and ignore feedback is mutually exclusive!",
                index));

        return new ColumnProcessingAnnotation(
            originalAnnotation.getProcessingType().entrySet().stream().collect(ImmutableMap
                .toImmutableMap(e -> e.getKey(), e -> ColumnProcessingTypeValue.COMPULSORY)));
      }
    }).collect(ImmutableList.toImmutableList());
  }

  private static List<StatisticalAnnotation> mergeStatisticalAnnotations(Result previousResult,
      Feedback feedback) {
    final List<StatisticalAnnotation> original = previousResult.getStatisticalAnnotations();
    final Set<DataCubeComponent> feedbackSet = feedback.getDataCubeComponents();
    final Map<ColumnPosition, StatisticalAnnotation> indicesToFeedbackAnnotations = feedbackSet
        .stream().collect(Collectors.toMap(e -> e.getPosition(), e -> e.getAnnotation()));

    return IntStream.range(0, original.size()).mapToObj(index -> {
      final StatisticalAnnotation feedbackAnnotation =
          indicesToFeedbackAnnotations.get(new ColumnPosition(index));
      if (feedbackAnnotation == null) {
        return original.get(index);
      } else {
        return feedbackAnnotation;
      }
    }).collect(ImmutableList.toImmutableList());
  }

  private static Map<ColumnRelationPosition, ColumnRelationAnnotation> mergeColumnRelationAnnotations(
      Result previousResult, Feedback feedback) {
    final Map<ColumnRelationPosition, ColumnRelationAnnotation> original =
        previousResult.getColumnRelationAnnotations();

    final Map<ColumnRelationPosition, ColumnRelationAnnotation> result = new HashMap<>(original);

    for (final ColumnRelation relation : feedback.getColumnRelations()) {
      final ColumnRelationPosition position = relation.getPosition();

      result.put(position, relation.getAnnotation());
    }

    return ImmutableMap.copyOf(result);
  }

  private static CellAnnotation[][] mergeCellAnnotations(Result previousResult, Feedback feedback) {
    final CellAnnotation[][] original = previousResult.getCellAnnotations();
    final CellAnnotation[][] result =
        cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(CellAnnotation.class, original);

    for (final Disambiguation disambiguation : feedback.getDisambiguations()) {
      final CellPosition position = disambiguation.getPosition();

      result[position.getRowIndex()][position.getColumnIndex()] = disambiguation.getAnnotation();
    }

    for (final Ambiguity ambiguity : feedback.getAmbiguities()) {
      final CellPosition position = ambiguity.getPosition();

      final CellAnnotation originalAnnotation =
          result[position.getRowIndex()][position.getColumnIndex()];

      result[position.getRowIndex()][position
          .getColumnIndex()] =
              new CellAnnotation(
                  originalAnnotation.getCandidates().entrySet().stream()
                      .collect(ImmutableMap.toImmutableMap(e -> e.getKey(),
                          e -> ImmutableSortedSet.of())),
                  originalAnnotation.getChosen().entrySet().stream().collect(
                      ImmutableMap.toImmutableMap(e -> e.getKey(), e -> ImmutableSet.of())));
    }

    for (final ColumnAmbiguity ambiguity : feedback.getColumnAmbiguities()) {
      final ColumnPosition position = ambiguity.getPosition();
      final int columnIndex = position.getIndex();

      for (int rowIndex = 0; rowIndex < result.length; rowIndex++) {
        final CellAnnotation originalAnnotation = result[rowIndex][columnIndex];

        result[rowIndex][columnIndex] = new CellAnnotation(
            originalAnnotation.getCandidates().entrySet().stream().collect(
                ImmutableMap.toImmutableMap(e -> e.getKey(), e -> ImmutableSortedSet.of())),
            originalAnnotation.getChosen().entrySet().stream()
                .collect(ImmutableMap.toImmutableMap(e -> e.getKey(), e -> ImmutableSet.of())));
      }
    }

    return result;
  }

  private static List<HeaderAnnotation> mergeHeaderAnnotations(Result previousResult,
      Feedback feedback) {
    final List<HeaderAnnotation> original = previousResult.getHeaderAnnotations();
    final Set<Classification> feedbackSet = feedback.getClassifications();
    final Set<ColumnIgnore> feedbackIgnores = feedback.getColumnIgnores();
    final Map<ColumnPosition, HeaderAnnotation> indicesToFeedbackAnnotations = feedbackSet.stream()
        .collect(Collectors.toMap(e -> e.getPosition(), e -> e.getAnnotation()));
    final Map<ColumnPosition, ColumnIgnore> indicesToIgnores = feedbackIgnores.stream()
        .collect(Collectors.toMap(e -> e.getPosition(), Function.identity()));

    return IntStream.range(0, original.size()).mapToObj(index -> {
      final HeaderAnnotation feedbackAnnotation =
          indicesToFeedbackAnnotations.get(new ColumnPosition(index));
      final ColumnIgnore feedbackIgnore = indicesToIgnores.get(new ColumnPosition(index));

      final HeaderAnnotation originalAnnotation = original.get(index);

      if (feedbackAnnotation == null) {
        if (feedbackIgnore == null) {
          return originalAnnotation;
        } else {
          return new HeaderAnnotation(
              originalAnnotation.getCandidates().entrySet().stream().collect(
                  ImmutableMap.toImmutableMap(e -> e.getKey(), e -> ImmutableSortedSet.of())),
              originalAnnotation.getChosen().entrySet().stream()
                  .collect(ImmutableMap.toImmutableMap(e -> e.getKey(), e -> ImmutableSet.of())));
        }
      } else {
        if (feedbackIgnore == null) {
          return feedbackAnnotation;
        } else {
          return new HeaderAnnotation(
              feedbackAnnotation.getCandidates().entrySet().stream().collect(
                  ImmutableMap.toImmutableMap(e -> e.getKey(), e -> ImmutableSortedSet.of())),
              feedbackAnnotation.getChosen().entrySet().stream()
                  .collect(ImmutableMap.toImmutableMap(e -> e.getKey(), e -> ImmutableSet.of())));
        }
      }
    }).collect(ImmutableList.toImmutableList());
  }
}
