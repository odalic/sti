package cz.cuni.mff.xrg.odalic.tasks.executions;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.bases.proxies.KnowledgeBaseProxiesService;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.csvexport.CSVExportTest;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.RDFExportTest;
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

    // Core execution
    final CoreSnapshot coreSnapshot = CoreExecutionBatch.testCoreExecution(propertyFilePath, testInputFilePath);

    if (coreSnapshot == null) {
      log.warn("Result of core algorithm is null, so exports cannot be launched.");
      return;
    }

    // settings for export
    final String baseExportPath = FilenameUtils.getFullPath(testInputFilePath)
        + FilenameUtils.getBaseName(testInputFilePath) + "-export";
    final KnowledgeBaseProxiesService kbf = CoreExecutionBatch.getKnowledgeBaseProxyFactory();

    // JSON export
    AnnotatedTable annotatedTable = CSVExportTest.testExportToAnnotatedTable(coreSnapshot.getResult(),
        coreSnapshot.getInput(), coreSnapshot.getConfiguration(), baseExportPath + ".json", kbf);

    // CSV export
    Input extendedInput = CSVExportTest.testExportToCSVFile(coreSnapshot.getResult(),
        coreSnapshot.getInput(), coreSnapshot.getConfiguration(), baseExportPath + ".csv", kbf);

    // RDF export
    if (annotatedTable == null || extendedInput == null) {
      log.warn("Annotated table or extended input is null, so RDF export cannot be launched.");
      return;
    }
    RDFExportTest.testExportToRDFFile(annotatedTable, extendedInput, baseExportPath + ".rdf");
  }

  public static final class CoreSnapshot {
    private final Result result;

    private final Input input;

    private final Configuration configuration;

    public CoreSnapshot(final Result result, final Input input, final Configuration configuration) {
      Preconditions.checkNotNull(result);
      Preconditions.checkNotNull(input);
      Preconditions.checkNotNull(configuration);

      this.input = input;
      this.result = result;
      this.configuration = configuration;
    }

    /**
     * @return the result
     */
    public Result getResult() {
      return result;
    }

    /**
     * @return the input
     */
    public Input getInput() {
      return input;
    }

    /**
     * @return the configuration
     */
    public Configuration getConfiguration() {
      return configuration;
    }

    @Override
    public String toString() {
      return "CoreSnapshot [result=" + result + ", input=" + input + ", configuration=" + configuration + "]";
    }
  }
}
