/**
 *
 */
package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.Immutable;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.FeedbackService;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Implementation of {@link AnnotatedTableService} that gets the {@link AnnotatedTable} by adapting
 * present {@link Result}, {@link Input} and {@link Configuration} instances.
 *
 * @author Václav Brodec
 *
 */
@Immutable
public final class ResultAdaptingAnnotatedTableService implements AnnotatedTableService {

  private final ExecutionService executionService;

  private final FeedbackService feedbackService;

  private final ConfigurationService configurationService;

  private final BasesService basesService;

  private final ResultToAnnotatedTableAdapter resultToAnnotatedTableAdapter;


  @Autowired
  public ResultAdaptingAnnotatedTableService(final ExecutionService executionService,
      final FeedbackService feedbackService, final ConfigurationService configurationService,
      final BasesService basesService,
      final ResultToAnnotatedTableAdapter resultToAnnotatedTableAdapter) {
    Preconditions.checkNotNull(feedbackService, "The feedbackService cannot be null!");
    Preconditions.checkNotNull(executionService, "The executionService cannot be null!");
    Preconditions.checkNotNull(configurationService, "The configurationService cannot be null!");
    Preconditions.checkNotNull(basesService, "The basesService cannot be null!");
    Preconditions.checkNotNull(resultToAnnotatedTableAdapter, "The resultToAnnotatedTableAdapter cannot be null!");

    this.executionService = executionService;
    this.feedbackService = feedbackService;
    this.configurationService = configurationService;
    this.basesService = basesService;
    this.resultToAnnotatedTableAdapter = resultToAnnotatedTableAdapter;
  }

  @Override
  public AnnotatedTable getAnnotatedTableForTaskId(final String userId, final String taskId)
      throws IllegalArgumentException, CancellationException, InterruptedException,
      ExecutionException, IOException {
    final Result result = this.executionService.getResultForTaskId(userId, taskId);
    final Input input = this.feedbackService.getInputSnapshotForTaskId(userId, taskId);
    final Configuration configuration = this.configurationService.getForTaskId(userId, taskId);
    final KnowledgeBase primaryBase =
        this.basesService.getByName(userId, configuration.getPrimaryBase());

    return this.resultToAnnotatedTableAdapter.toAnnotatedTable(result, input,
        configuration.isStatistical(), primaryBase);
  }

}
