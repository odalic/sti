package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

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

import cz.cuni.mff.xrg.odalic.files.formats.DefaultApacheCsvFormatAdapter;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.DefaultResultToAnnotatedTableAdapter;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.executions.KnowledgeBaseProxyFactory;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * JUnit test for CSV export
 * 
 * @author Josef Janoušek
 *
 */
public class CSVExportTest {

  private static final Logger log = LoggerFactory.getLogger(CSVExportTest.class);

  @BeforeClass
  public static void beforeClass() throws URISyntaxException, IOException {

  }

  @Test
  public void TestConversionToCSV() {

    // Now this test cannot be launched separately,
    // because there are problems with deserializing Result (ResultValue) from external JSON file
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
