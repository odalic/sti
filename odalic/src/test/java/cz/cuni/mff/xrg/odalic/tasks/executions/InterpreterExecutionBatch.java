package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.csvexport.CSVExportTest;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.RDFExportTest;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

public class InterpreterExecutionBatch {

  private static final Logger log = LoggerFactory.getLogger(InterpreterExecutionBatch.class);

  /**
   * Expects sti.properties file path as the first and test input CSV file path as the second
   * command line argument
   * 
   * @param args command line arguments
   * 
   * @author Josef Janoušek
   * 
   */
  public static void main(String[] args) {

    final String propertyFilePath = args[0];
    final String testInputFilePath = args[1];

    // Core settings
    final Task task = CoreExecutionBatch.testCoreSettings(Paths.get(testInputFilePath));

    if (task == null) {
      log.warn("Task was not set correctly, so execution cannot be launched.");
      return;
    }

    // Core execution
    final Result odalicResult = CoreExecutionBatch.testCoreExecution(propertyFilePath, task);

    if (odalicResult == null) {
      log.warn("Result of core algorithm is null, so exports cannot be launched.");
      return;
    }

    // settings for export
    final Input input = CoreExecutionBatch.getInput();
    final Configuration config = task.getConfiguration();
    final String baseExportPath = FilenameUtils.getFullPath(testInputFilePath)
        + FilenameUtils.getBaseName(testInputFilePath) + "-export";

    // JSON export
    AnnotatedTable annotatedTable = CSVExportTest.testExportToAnnotatedTable(odalicResult, input,
        config, baseExportPath + ".json");

    // CSV export
    Input extendedInput =
        CSVExportTest.testExportToCSVFile(odalicResult, input, config, baseExportPath + ".csv");

    // RDF export
    if (annotatedTable == null || extendedInput == null) {
      log.warn("Annotated table or extended input is null, so RDF export cannot be launched.");
      return;
    }
    RDFExportTest.testExportToRDFFile(annotatedTable, extendedInput, baseExportPath + ".rdf");
  }
}
