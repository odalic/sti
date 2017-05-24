package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.files.formats.DefaultApacheCsvFormatAdapter;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.DefaultResultToAnnotatedTableAdapter;
import cz.cuni.mff.xrg.odalic.outputs.csvexport.CSVExportTest;
import cz.cuni.mff.xrg.odalic.outputs.csvexport.DefaultCSVExporter;
import cz.cuni.mff.xrg.odalic.outputs.csvexport.DefaultResultToCSVExportAdapter;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.DefaultAnnotatedTableToRDFExportAdapter;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.DefaultRDFExporter;
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
    // Result export
    resultExport(coreSnapshot.getResult(), baseExportPath + "-result.json");

    final KnowledgeBase primaryBase = CoreExecutionBatch.getKnowledgeBaseService().getByName("test@odalic.eu", "DBpedia");
    
    // JSON export
    AnnotatedTable annotatedTable = CSVExportTest.testExportToAnnotatedTable(coreSnapshot.getResult(),
        coreSnapshot.getInput(), coreSnapshot.getConfiguration(), primaryBase, baseExportPath + ".json",
        new DefaultResultToAnnotatedTableAdapter(CoreExecutionBatch.getKnowledgeBaseProxyFactory()));

    // CSV export
    Input extendedInput = CSVExportTest.testExportToCSVFile(coreSnapshot.getResult(),
        coreSnapshot.getInput(), coreSnapshot.getConfiguration(), primaryBase, baseExportPath + ".csv",
        new DefaultResultToCSVExportAdapter(), new DefaultCSVExporter(new DefaultApacheCsvFormatAdapter()));

    // RDF export
    if (annotatedTable == null || extendedInput == null) {
      log.warn("Annotated table or extended input is null, so RDF export cannot be launched.");
      return;
    }
    RDFExportTest.testExportToRDFFile(annotatedTable, extendedInput, baseExportPath + ".rdf",
        new DefaultAnnotatedTableToRDFExportAdapter(), new DefaultRDFExporter());
  }

  public static void resultExport(Result result, String filePath) {
    // Export Result to JSON String
    String json;
    try {
      json = new ObjectMapper().setAnnotationIntrospector(AnnotationIntrospector.pair(
          new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector(
              TypeFactory.defaultInstance()))).writerWithDefaultPrettyPrinter()
          .writeValueAsString(result);
    } catch (JsonProcessingException e) {
      log.error("Error - exporting Result to JSON String:", e);
      return;
    }
    log.info("Resulting JSON is: " + json);

    // Write JSON String to file
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write(json);
      log.info("JSON export saved to file " + filePath);
    } catch (IOException e) {
      log.error("Error - saving JSON export file:", e);
    }
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
