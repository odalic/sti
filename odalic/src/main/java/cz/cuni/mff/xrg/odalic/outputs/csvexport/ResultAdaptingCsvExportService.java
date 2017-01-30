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

import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.files.formats.FormatService;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.tasks.feedbacks.FeedbackService;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Implementation of {@link RdfExportService} that gets the extended CSV data by adapting present
 * {@link Result}, {@link Input} and {@link Format} instances.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public class ResultAdaptingCsvExportService implements CsvExportService {

  private final ExecutionService executionService;

  private final FeedbackService feedbackService;

  private final ConfigurationService configurationService;

  private final FormatService formatService;

  private final ResultToCSVExportAdapter resultToCsvExportAdapter;

  private final CSVExporter csvExporter;

  @Autowired
  public ResultAdaptingCsvExportService(final ExecutionService executionService,
      final FeedbackService feedbackService, final ConfigurationService configurationService,
      final FormatService formatService, final ResultToCSVExportAdapter resultToCsvExportAdapter,
      final CSVExporter csvExporter) {
    Preconditions.checkNotNull(feedbackService);
    Preconditions.checkNotNull(executionService);
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(formatService);
    Preconditions.checkNotNull(resultToCsvExportAdapter);
    Preconditions.checkNotNull(csvExporter);

    this.executionService = executionService;
    this.feedbackService = feedbackService;
    this.configurationService = configurationService;
    this.formatService = formatService;
    this.resultToCsvExportAdapter = resultToCsvExportAdapter;
    this.csvExporter = csvExporter;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.outputs.csvexport.CsvExportService#getExtendedCsvForTaskId(java.lang.
   * String)
   */
  @Override
  public String getExtendedCsvForTaskId(String userId, String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    final Input output = getExtendedInputForTaskId(userId, taskId);
    final Format originalFormat = getOriginalFormat(userId, taskId);

    final String data = csvExporter.export(output, originalFormat);

    return data;
  }

  private Format getOriginalFormat(String userId, String taskId) {
    final Configuration configuration = configurationService.getForTaskId(userId, taskId);
    final File file = configuration.getInput();
    final Format format = formatService.getForFileId(userId, file.getId());
    return format;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.outputs.csvexport.CsvExportService#getExtendedInputForTaskId(java.lang.
   * String)
   */
  @Override
  public Input getExtendedInputForTaskId(String userId, String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException {
    final Result result = executionService.getResultForTaskId(userId, taskId);
    final Input input = feedbackService.getInputForTaskId(userId, taskId);
    final Configuration configuration = configurationService.getForTaskId(userId, taskId);

    final Input output = resultToCsvExportAdapter.toCSVExport(result, input, configuration);

    return output;
  }
}
