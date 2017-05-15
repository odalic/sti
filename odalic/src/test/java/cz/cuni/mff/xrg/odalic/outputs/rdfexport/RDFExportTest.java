package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.input.CsvInputParser;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;

/**
 * JUnit test for RDF export
 * 
 * @author Josef Janoušek
 * @author Tomáš Knap
 *
 */
@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml"})
public class RDFExportTest {

  private static final Logger log = LoggerFactory.getLogger(RDFExportTest.class);

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  @Lazy
  private CsvInputParser csvInputParser;

  @Autowired
  @Lazy
  private AnnotatedTableToRDFExportAdapter annotatedTableToRdfExportAdapter;

  @Autowired
  @Lazy
  private RDFExporter rdfExporter;

  private static File inputJsonFile;
  private static File inputFile;

  @BeforeClass
  public static void beforeClass() throws URISyntaxException, IOException {

    inputJsonFile = new File(RDFExportTest.class.getClassLoader().getResource("book.json").toURI());
    inputFile = new File(RDFExportTest.class.getClassLoader().getResource("book.csv").toURI());
  }

  @Test
  public void TestConversionToTurtle() {

    // Convert JSON file to Java Object AnnotatedTable
    AnnotatedTable annotatedTable;
    try (final FileInputStream inputFileStream = new FileInputStream(inputJsonFile)) {
      annotatedTable = new ObjectMapper().setAnnotationIntrospector(AnnotationIntrospector.pair(
          new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector(
              TypeFactory.defaultInstance()))).readValue(inputFileStream, AnnotatedTable.class);
      log.info("Input JSON file loaded.");
    } catch (IOException e) {
      log.error("Error - loading input JSON file:", e);
      return;
    }

    // Convert CSV file to Java Object Input
    Format format = new Format(StandardCharsets.UTF_8, ';', true, '"', null, null);
    Input extendedInput;
    try (final FileInputStream inputFileStream = new FileInputStream(inputFile)) {
      extendedInput = csvInputParser.parse(
              IOUtils.toString(inputFileStream, format.getCharset()),
              inputFile.getName(), format, Integer.MAX_VALUE).getInput();
      log.info("Input CSV file loaded.");
    } catch (IOException e) {
      log.error("Error - loading input CSV file:", e);
      return;
    }

    // Export from annotated table (and extended input) to RDF file
    testExportToRDFFile(annotatedTable, extendedInput, inputFile.getParent() + File.separator
        + FilenameUtils.getBaseName(inputFile.getName()) + ".rdf", annotatedTableToRdfExportAdapter, rdfExporter);
  }

  public static void testExportToRDFFile(AnnotatedTable annotatedTable, Input extendedInput,
      String filePath, AnnotatedTableToRDFExportAdapter adapter, RDFExporter exporter) {

    // Conversion from annotated table to RDF model
    Model rdfModel =
        adapter.toRDFExport(annotatedTable, extendedInput);

    // Export RDF Model to RDF String (in turtle format)
    String rdf = exporter.export(rdfModel, RDFFormat.TURTLE);
    log.info("Resulting RDF is: " + rdf);

    // Write RDF String to file
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write(rdf);
      log.info("RDF export saved to file " + filePath);
    } catch (IOException e) {
      log.error("Error - saving RDF export file:", e);
    }
  }
}
