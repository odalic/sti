/**
 *
 */
package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.Immutable;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.FeedbackService;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Implementation of {@link CsvExportService} that produces the extended CSV data by adapting
 * present {@link Result}, {@link Input} and {@link Format} instances.
 *
 * @author Václav Brodec
 *
 */
@Immutable
public class ResultAdaptingCsvExportService implements CsvExportService {

  private final ExecutionService executionService;

  private final FeedbackService feedbackService;

  private final ConfigurationService configurationService;

  private final FileService fileService;

  private final BasesService basesService;

  private final ResultToCSVExportAdapter resultToCsvExportAdapter;

  private final CSVExporter csvExporter;

  @Autowired
  public ResultAdaptingCsvExportService(final ExecutionService executionService,
      final FeedbackService feedbackService, final ConfigurationService configurationService,
      final FileService fileService, final BasesService basesService,
      final ResultToCSVExportAdapter resultToCsvExportAdapter, final CSVExporter csvExporter) {
    Preconditions.checkNotNull(feedbackService);
    Preconditions.checkNotNull(executionService);
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(basesService);
    Preconditions.checkNotNull(resultToCsvExportAdapter);
    Preconditions.checkNotNull(csvExporter);

    this.executionService = executionService;
    this.feedbackService = feedbackService;
    this.configurationService = configurationService;
    this.fileService = fileService;
    this.basesService = basesService;
    this.resultToCsvExportAdapter = resultToCsvExportAdapter;
    this.csvExporter = csvExporter;
  }

  @Override
  public String getExtendedCsvForTaskId(final String userId, final String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    final Input output = getExtendedInputForTaskId(userId, taskId);
    final Format originalFormat = getOriginalFormat(userId, taskId);

    final String data = this.csvExporter.export(output, originalFormat);

    return data;
  }

  @Override
  public Input getExtendedInputForTaskId(final String userId, final String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    final Result result = this.executionService.getResultForTaskId(userId, taskId);
    final Input input = this.feedbackService.getInputSnapshotForTaskId(userId, taskId);
    final Configuration configuration = this.configurationService.getForTaskId(userId, taskId);
    final KnowledgeBase primaryBase =
        this.basesService.getByName(userId, configuration.getPrimaryBase());

    final Input output = this.resultToCsvExportAdapter.toCSVExport(result, input,
        configuration.isStatistical(), primaryBase);

    return output;
  }

  private Format getOriginalFormat(final String userId, final String taskId) {
    final Configuration configuration = this.configurationService.getForTaskId(userId, taskId);
    final File file = configuration.getInput();
    final Format format = this.fileService.getFormatForFileId(userId, file.getId());
    return format;
  }
}
