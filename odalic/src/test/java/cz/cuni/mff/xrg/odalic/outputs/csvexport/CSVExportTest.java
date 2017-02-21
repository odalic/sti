package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.files.formats.DefaultApacheCsvFormatAdapter;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.input.DefaultCsvInputParser;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.DefaultResultToAnnotatedTableAdapter;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.executions.DefaultKnowledgeBaseProxyFactory;
import cz.cuni.mff.xrg.odalic.tasks.executions.KnowledgeBaseProxyFactory;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.User;
import uk.ac.shef.dcs.sti.STIException;

/**
 * JUnit test for CSV export
 * 
 * @author Josef Janoušek
 *
 */
public class CSVExportTest {

  private static final Logger log = LoggerFactory.getLogger(CSVExportTest.class);

  static File inputResultFile;
  static File inputFile;

  @BeforeClass
  public static void beforeClass() throws URISyntaxException, IOException {

    inputResultFile = new File(CSVExportTest.class.getClassLoader().getResource("book-result.json").toURI());
    inputFile = new File(CSVExportTest.class.getClassLoader().getResource("book-input.csv").toURI());
  }

  @Test
  public void TestConversionToCSV() {

    KnowledgeBaseProxyFactory kbf;
    try {
      System.setProperty("cz.cuni.mff.xrg.odalic.sti", Paths.get("").toAbsolutePath()
          .resolveSibling("config").resolve("sti.properties").toString());
      kbf = new DefaultKnowledgeBaseProxyFactory(null);
    } catch (STIException | IOException e) {
      log.info("KnowledgeBaseProxyFactory is not available, so test was stopped: " + e.getMessage());
      return;
    }

    Configuration config;
    try {
      config = new Configuration(new cz.cuni.mff.xrg.odalic.files.File(
          new User("test@odalic.eu", "passwordHash", Role.USER), inputFile.getName(),
          inputFile.toURI().toURL(), new Format(), true), ImmutableSet.of(new KnowledgeBase("DBpedia"),
          new KnowledgeBase("DBpedia Clone"), new KnowledgeBase("German DBpedia")),
          new KnowledgeBase("DBpedia"), new Feedback(), null, false);
    } catch (MalformedURLException e) {
      log.error("Error - configuration settings:", e);
      return;
    }

    // Convert JSON file to Java Object Result
    Result result;
    try (final FileInputStream inputFileStream = new FileInputStream(inputResultFile)) {
      result = new ObjectMapper().setAnnotationIntrospector(AnnotationIntrospector.pair(
          new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector(
              TypeFactory.defaultInstance()))).readValue(inputFileStream, Result.class);
      log.info("Input JSON file loaded.");
    } catch (IOException e) {
      log.error("Error - loading input JSON file:", e);
      return;
    }

    // Convert CSV file to Java Object Input
    Input input;
    try (final FileInputStream inputFileStream = new FileInputStream(inputFile)) {
      input = new DefaultCsvInputParser(new ListsBackedInputBuilder(),
          new DefaultApacheCsvFormatAdapter()).parse(IOUtils.toString(inputFileStream,
              config.getInput().getFormat().getCharset()), config.getInput().getId(),
              config.getInput().getFormat(), config.getRowsLimit()).getInput();
      log.info("Input CSV file loaded.");
    } catch (IOException e) {
      log.error("Error - loading input CSV file:", e);
      return;
    }

    // Export from result (and input) to CSV file
    testExportToCSVFile(result, input, config, inputFile.getParent() + File.separator
        + FilenameUtils.getBaseName(inputFile.getName()) + "-export.csv", kbf);

    // Export from result (and input) to JSON annotated table
    testExportToAnnotatedTable(result, input, config, inputFile.getParent() + File.separator
        + FilenameUtils.getBaseName(inputFile.getName()) + "-export.json", kbf);
  }

  public static Input testExportToCSVFile(Result result, Input input, Configuration config,
      String filePath, KnowledgeBaseProxyFactory kbf) {

    // Conversion from result to CSV extended input
    Input extendedInput = new DefaultResultToCSVExportAdapter(kbf).toCSVExport(result, input, config);

    // Export CSV extended Input to CSV String
    String csv;
    try {
      csv = new DefaultCSVExporter(new DefaultApacheCsvFormatAdapter()).export(extendedInput,
          config.getInput().getFormat());
    } catch (IOException e) {
      log.error("Error - exporting extended Input to CSV:", e);
      return null;
    }
    log.info("Resulting CSV is: " + csv);

    // Write CSV String to file
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write(csv);
      log.info("CSV export saved to file " + filePath);
      return extendedInput;
    } catch (IOException e) {
      log.error("Error - saving CSV export file:", e);
      return null;
    }
  }

  public static AnnotatedTable testExportToAnnotatedTable(Result result, Input input,
      Configuration config, String filePath, KnowledgeBaseProxyFactory kbf) {

    // Conversion from result to annotated table
    AnnotatedTable annotatedTable =
        new DefaultResultToAnnotatedTableAdapter(kbf).toAnnotatedTable(result, input, config);

    // Export Annotated Table to JSON String
    String json;
    try {
      json = new ObjectMapper().setAnnotationIntrospector(AnnotationIntrospector.pair(
          new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector(
              TypeFactory.defaultInstance()))).writerWithDefaultPrettyPrinter()
          .writeValueAsString(annotatedTable);
    } catch (JsonProcessingException e) {
      log.error("Error - exporting Annotated Table to JSON String:", e);
      return null;
    }
    log.info("Resulting JSON is: " + json);

    // Write JSON String to file
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write(json);
      log.info("JSON export saved to file " + filePath);
      return annotatedTable;
    } catch (IOException e) {
      log.error("Error - saving JSON export file:", e);
      return null;
    }
  }
}
