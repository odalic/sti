package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.input.CsvConfiguration;
import cz.cuni.mff.xrg.odalic.input.DefaultCsvInputParser;
import cz.cuni.mff.xrg.odalic.input.DefaultInputToTableAdapter;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.csvexport.CSVExportTest;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.RDFExportTest;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.DefaultAnnotationToResultAdapter;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

public class InterpreterExecutionBatch {

  private static final Logger log = LoggerFactory.getLogger(InterpreterExecutionBatch.class);
  
  /**
   * Expects sti.properties file path as the first and test input CSV file path as the second
   * command line argument
   * 
   * @param args command line arguments
   * 
   * @author Josef Janoušek
   * @author Jan Váňa
   * 
   */
  public static void main(String[] args) {
    
    final String propertyFilePath = args[0];
    final String testInputFilePath = args[1];
    
    final File inputFile = new File(testInputFilePath);
    
    // TableMinerPlus initialization
    final SemanticTableInterpreterFactory factory = new TableMinerPlusFactory(propertyFilePath);
    final Map<String, SemanticTableInterpreter> semanticTableInterpreters = factory.getInterpreters();
    Preconditions.checkNotNull(semanticTableInterpreters);
    
    // set ignore columns
    factory.setColumnIgnoresForInterpreter(ImmutableSet.of());
    
    // Code for extraction from CSV
    final Input input;
    try (final FileInputStream inputFileStream = new FileInputStream(inputFile)) {
      input = new DefaultCsvInputParser(new ListsBackedInputBuilder())
          .parse(inputFileStream, inputFile.getName(), new CsvConfiguration());
      log.info("Input CSV file loaded.");
    } catch (IOException e) {
      log.error("Error - loading input CSV file:");
      e.printStackTrace();
      return;
    }
    
    // input Table creation
    final Table table = new DefaultInputToTableAdapter().toTable(input);
    
    // TableMinerPlus algorithm run
    Map<KnowledgeBase, TAnnotation> results = new HashMap<>();
    try {
      for(Map.Entry<String, SemanticTableInterpreter> interpreterEntry : semanticTableInterpreters.entrySet()) {
        TAnnotation annotationResult = interpreterEntry.getValue().start(table, true);
        
        results.put(new KnowledgeBase(interpreterEntry.getKey()), annotationResult);
      }
    } catch (STIException e) {
      log.error("Error - running TableMinerPlus algorithm:");
      e.printStackTrace();
      return;
    }
    
    // settings for export
    Configuration config;
    try {
      config = new Configuration(new cz.cuni.mff.xrg.odalic.files.File(inputFile.getName(), "x",
          inputFile.toURI().toURL()), new KnowledgeBase("DBPedia"));
    } catch (MalformedURLException e) {
      log.error("Error - configuration settings for export:");
      e.printStackTrace();
      return;
    }
    
    // Odalic Result creation
    Result odalicResult = new DefaultAnnotationToResultAdapter().toResult(results);
    log.info("Odalic Result is: " + odalicResult);
    
    String baseExportPath = inputFile.getParent() + File.separator + FilenameUtils.getBaseName(inputFile.getName()) + "-export";
    
    // JSON export
    AnnotatedTable annotatedTable = CSVExportTest.testExportToAnnotatedTable(odalicResult, input, config, baseExportPath + ".json");
    
    // CSV export
    Input extendedInput = CSVExportTest.testExportToCSVFile(odalicResult, input, config, baseExportPath + ".csv");
    
    // RDF export
    if (annotatedTable == null || extendedInput == null) {
      log.warn("Annotated table or extended input is null, so RDF export cannot be launched.");
      return;
    }
    RDFExportTest.testExportToRDFFile(annotatedTable, extendedInput, baseExportPath + ".rdf");
  }
}
