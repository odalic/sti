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

  private final ResultToAnnotatedTableAdapter resultToAnnotatedTableAdapter;

  @Autowired
  public ResultAdaptingAnnotatedTableService(final ExecutionService executionService,
      final FeedbackService feedbackService, final ConfigurationService configurationService,
      final ResultToAnnotatedTableAdapter resultToAnnotatedTableAdapter) {
    Preconditions.checkNotNull(feedbackService);
    Preconditions.checkNotNull(executionService);
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(resultToAnnotatedTableAdapter);

    this.executionService = executionService;
    this.feedbackService = feedbackService;
    this.configurationService = configurationService;
    this.resultToAnnotatedTableAdapter = resultToAnnotatedTableAdapter;
  }

  @Override
  public AnnotatedTable getAnnotatedTableForTaskId(final String userId, final String taskId)
      throws IllegalArgumentException, CancellationException, InterruptedException,
      ExecutionException, IOException {
    final Result result = this.executionService.getResultForTaskId(userId, taskId);
    final Input input = this.feedbackService.getInputSnapshotForTaskId(userId, taskId);
    final Configuration configuration = this.configurationService.getForTaskId(userId, taskId);

    return this.resultToAnnotatedTableAdapter.toAnnotatedTable(result, input, configuration);
  }

}
