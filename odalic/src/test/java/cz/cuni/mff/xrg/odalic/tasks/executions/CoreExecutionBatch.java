package cz.cuni.mff.xrg.odalic.tasks.executions;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cz.cuni.mff.xrg.odalic.entities.PrefixMappingEntitiesFactory;
import cz.cuni.mff.xrg.odalic.feedbacks.Ambiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.Classification;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnAmbiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnRelation;
import cz.cuni.mff.xrg.odalic.feedbacks.DefaultFeedbackToConstraintsAdapter;
import cz.cuni.mff.xrg.odalic.feedbacks.Disambiguation;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.formats.DefaultApacheCsvFormatAdapter;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.input.DefaultCsvInputParser;
import cz.cuni.mff.xrg.odalic.input.DefaultInputToTableAdapter;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.input.ParsingResult;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.TurtleConfigurablePrefixMappingService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.DefaultAnnotationToResultAdapter;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

public class CoreExecutionBatch {

  private static final Logger log = LoggerFactory.getLogger(CoreExecutionBatch.class);

  private static final Map<String, File> files = new HashMap<>();
  private static final Map<URL, byte[]> data = new HashMap<>();
  private static final Multimap<String, String> utilizingTasks = HashMultimap.create();

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

    // Core settings
    final Task task = testCoreSettings(Paths.get(testInputFilePath));

    if (task == null) {
      log.warn("Task was not set correctly, so execution cannot be launched.");
      return;
    }

    // Core execution
    testCoreExecution(propertyFilePath, task);
  }

  public static Task testCoreSettings(Path path) {
    final String fileId = path.getFileName().toString();

    // File settings
    try {
      final File file = new File(fileId, "", path.toUri().toURL(), new Format(), true);
      files.put(file.getId(), file);
      data.put(file.getLocation(),
          IOUtils.toByteArray(new FileInputStream(file.getLocation().getFile())));
    } catch (IOException e) {
      log.error("Error - File settings:", e);
      return null;
    }

    // Format settings
    files.get(fileId).setFormat(new Format(StandardCharsets.UTF_8, ';', true, '"', null, null));

    // Task settings
    Task task = new Task("simple_task", "task description",
        new Configuration(files.get(fileId), ImmutableSet.of(new KnowledgeBase("DBpedia"),
            new KnowledgeBase("DBpedia Clone"), new KnowledgeBase("German DBpedia")),
            new KnowledgeBase("DBpedia"), createFeedback(true), null));
    utilizingTasks.put(task.getConfiguration().getInput().getId(), task.getId());

    return task;
  }

  public static Result testCoreExecution(String propertyFilePath, Task task) {
    System.setProperty("cz.cuni.mff.xrg.odalic.sti", propertyFilePath);

    final File file = task.getConfiguration().getInput();

    // Code for extraction from CSV
    final ParsingResult parsingResult;
    try {
      parsingResult = new DefaultCsvInputParser(new ListsBackedInputBuilder(),
          new DefaultApacheCsvFormatAdapter()).parse(
              new String(data.get(file.getLocation()), file.getFormat().getCharset()), file.getId(),
              file.getFormat(), task.getConfiguration().getRowsLimit());
      log.info("Input CSV file loaded.");
    } catch (IOException e) {
      log.error("Error - loading input CSV file:", e);
      return null;
    }

    // Parsed format and input settings
    file.setFormat(parsingResult.getFormat());
    task.setInputSnapshot(parsingResult.getInput());

    // input Table creation
    final Table table = new DefaultInputToTableAdapter().toTable(parsingResult.getInput());

    // TableMinerPlus initialization
    final Map<String, SemanticTableInterpreter> semanticTableInterpreters;
    try {
      semanticTableInterpreters = new TableMinerPlusFactory(
          new DefaultKnowledgeBaseProxyFactory()).getInterpreters();
    } catch (IOException e) {
      log.error("Error - TMP initialization process fails to load its configuration:", e);
      return null;
    } catch (STIException e) {
      log.error("Error - TMP interpreters fail to initialize:", e);
      return null;
    }
    Preconditions.checkNotNull(semanticTableInterpreters);

    // TableMinerPlus algorithm run
    Map<KnowledgeBase, TAnnotation> results = new HashMap<>();
    try {
      for (Map.Entry<String, SemanticTableInterpreter> interpreterEntry : semanticTableInterpreters
          .entrySet()) {
        final KnowledgeBase base = new KnowledgeBase(interpreterEntry.getKey());
        if (!task.getConfiguration().getUsedBases().contains(base)) {
          continue;
        }

        Constraints constraints = new DefaultFeedbackToConstraintsAdapter().toConstraints(
            task.getConfiguration().getFeedback(), base);

        TAnnotation annotationResult = interpreterEntry.getValue().start(table, constraints);

        results.put(base, annotationResult);
      }
    } catch (STIException e) {
      log.error("Error - running TableMinerPlus algorithm:", e);
      return null;
    }

    // PrefixMappingService
    TurtleConfigurablePrefixMappingService pms;
    try {
      pms = new TurtleConfigurablePrefixMappingService();
    } catch (IOException e) {
      log.error("Error - prefix mapping service loading:", e);
      return null;
    }

    // Odalic Result creation
    final Result odalicResult = new DefaultAnnotationToResultAdapter(
        new PrefixMappingEntitiesFactory(pms)).toResult(results);
    log.info("Odalic Result is: " + odalicResult);

    return odalicResult;
  }

  private static Feedback createFeedback(boolean emptyFeedback) {
    if (emptyFeedback) {
      return new Feedback();
    }
    else {
      // subject columns example
      HashMap<KnowledgeBase, ColumnPosition> subjectColumns = new HashMap<>();
      subjectColumns.put(new KnowledgeBase("DBpedia Clone"), new ColumnPosition(0));

      // classifications example
      HashSet<EntityCandidate> candidatesClassification = new HashSet<>();
      candidatesClassification.add(
          new EntityCandidate(Entity.of("http://schema.org/Bookxyz", "Booooook"), new Score(1.0)));
      candidatesClassification
          .add(new EntityCandidate(Entity.of("http://schema.org/Book", "Book"), new Score(1.0)));
      HashMap<KnowledgeBase, HashSet<EntityCandidate>> headerAnnotation = new HashMap<>();
      headerAnnotation.put(new KnowledgeBase("DBpedia Clone"), candidatesClassification);
      HashSet<Classification> classifications = new HashSet<>();
      classifications.add(new Classification(new ColumnPosition(0),
          new HeaderAnnotation(headerAnnotation, headerAnnotation)));

      // disambiguations example
      HashSet<EntityCandidate> candidatesDisambiguation = new HashSet<>();
      candidatesDisambiguation.add(new EntityCandidate(
          Entity.of("http://dbpedia.org/resource/Gardens_of_the_Moonxyz", "Gars of Moooooon"),
          new Score(1.0)));
      candidatesDisambiguation.add(new EntityCandidate(
          Entity.of("http://dbpedia.org/resource/Gardens_of_the_Moon", "Gardens of the Moon"),
          new Score(1.0)));
      HashMap<KnowledgeBase, HashSet<EntityCandidate>> cellAnnotation = new HashMap<>();
      cellAnnotation.put(new KnowledgeBase("DBpedia Clone"), candidatesDisambiguation);
      HashSet<Disambiguation> disambiguations = new HashSet<>();
      disambiguations.add(new Disambiguation(new CellPosition(0, 0),
          new CellAnnotation(cellAnnotation, cellAnnotation)));

      // relations example
      HashSet<EntityCandidate> candidatesRelation = new HashSet<>();
      candidatesRelation.add(new EntityCandidate(
          Entity.of("http://dbpedia.org/property/authorxyz", ""), new Score(1.0)));
      candidatesRelation.add(
          new EntityCandidate(Entity.of("http://dbpedia.org/property/author", ""), new Score(1.0)));
      HashMap<KnowledgeBase, HashSet<EntityCandidate>> columnRelationAnnotation = new HashMap<>();
      columnRelationAnnotation.put(new KnowledgeBase("DBpedia Clone"), candidatesRelation);
      HashSet<ColumnRelation> relations = new HashSet<>();
      relations.add(new ColumnRelation(new ColumnRelationPosition(0, 1),
          new ColumnRelationAnnotation(columnRelationAnnotation, columnRelationAnnotation)));

      // ignore columns example
      HashSet<ColumnIgnore> columnIgnores = new HashSet<>();
      columnIgnores.add(new ColumnIgnore(new ColumnPosition(3)));

      // column ambiguities example
      HashSet<ColumnAmbiguity> columnAmbiguities = new HashSet<>();
      columnAmbiguities.add(new ColumnAmbiguity(new ColumnPosition(4)));

      // ambiguities example
      HashSet<Ambiguity> ambiguities = new HashSet<>();
      ambiguities.add(new Ambiguity(new CellPosition(5, 5)));

      // construction example
      return new Feedback(subjectColumns, columnIgnores, columnAmbiguities, classifications,
          relations, disambiguations, ambiguities);
    }
  }
}
