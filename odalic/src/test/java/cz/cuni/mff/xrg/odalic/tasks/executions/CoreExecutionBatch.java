package cz.cuni.mff.xrg.odalic.tasks.executions;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBaseBuilder;
import cz.cuni.mff.xrg.odalic.bases.proxies.KnowledgeBaseProxiesService;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.entities.PrefixMappingEntitiesFactory;
import cz.cuni.mff.xrg.odalic.feedbacks.Ambiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.Classification;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnAmbiguity;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnIgnore;
import cz.cuni.mff.xrg.odalic.feedbacks.ColumnRelation;
import cz.cuni.mff.xrg.odalic.feedbacks.DataCubeComponent;
import cz.cuni.mff.xrg.odalic.feedbacks.DefaultFeedbackToConstraintsAdapter;
import cz.cuni.mff.xrg.odalic.feedbacks.Disambiguation;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.formats.DefaultApacheCsvFormatAdapter;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.groups.DefaultGroupBuilder;
import cz.cuni.mff.xrg.odalic.groups.GroupBuilder;
import cz.cuni.mff.xrg.odalic.input.DefaultCsvInputParser;
import cz.cuni.mff.xrg.odalic.input.DefaultInputToTableAdapter;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.input.ParsingResult;
import cz.cuni.mff.xrg.odalic.positions.CellPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnPosition;
import cz.cuni.mff.xrg.odalic.positions.ColumnRelationPosition;
import cz.cuni.mff.xrg.odalic.tasks.annotations.CellAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.ColumnRelationAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.HeaderAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;
import cz.cuni.mff.xrg.odalic.tasks.annotations.StatisticalAnnotation;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.TurtleConfigurablePrefixMappingService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.executions.InterpreterExecutionBatch.CoreSnapshot;
import cz.cuni.mff.xrg.odalic.tasks.results.DefaultAnnotationToResultAdapter;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.util.configuration.DefaultPropertiesService;

public class CoreExecutionBatch {

  private static final Logger log = LoggerFactory.getLogger(CoreExecutionBatch.class);

  private static KnowledgeBaseProxiesService kbf;

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

    // Core execution
    testCoreExecution(propertyFilePath, testInputFilePath);
  }

  public static CoreSnapshot testCoreExecution(String propertyFilePath, String testInputFilePath) {
    System.setProperty("cz.cuni.mff.xrg.odalic.sti", propertyFilePath);

    // File settings
    final Path path = Paths.get(testInputFilePath);
    File file;
    try {
      file = new File(new User("test@odalic.eu", "passwordHash", Role.USER),
          path.getFileName().toString(), path.toUri().toURL(), new Format(
              StandardCharsets.ISO_8859_1, ';', true, '"', null, null), true);
    } catch (IOException e) {
      log.error("Error - File settings:", e);
      return null;
    }
    int rowsLimit = Configuration.MAXIMUM_ROWS_LIMIT;

    // Code for extraction from CSV
    final ParsingResult parsingResult;
    try {
      parsingResult = new DefaultCsvInputParser(new ListsBackedInputBuilder(),
          new DefaultApacheCsvFormatAdapter()).parse(
              new String(IOUtils.toByteArray(new FileInputStream(file.getLocation().getFile())),
                  file.getFormat().getCharset()), file.getId(), file.getFormat(), rowsLimit);
      log.info("Input CSV file loaded: " + file.getLocation());
    } catch (IOException e) {
      log.error("Error - loading input CSV file:", e);
      return null;
    }

    // Parsed format settings
    file = new File(file.getOwner(), file.getId(), file.getUploaded(), file.getLocation(),
        parsingResult.getFormat(), file.isCached());

    // Configuration settings
    Configuration configuration = new Configuration(file, ImmutableSet.of(getDummyBase("DBpedia"),
        getDummyBase("DBpedia Clone"), getDummyBase("German DBpedia")),
        getDummyBase("DBpedia"), createFeedback(true), rowsLimit, false);

    // input Table creation
    final Table table = new DefaultInputToTableAdapter().toTable(parsingResult.getInput());

    // PrefixMappingService
    TurtleConfigurablePrefixMappingService pms;
    try {
      pms = new TurtleConfigurablePrefixMappingService(new DefaultPropertiesService());
    } catch (IOException e) {
      log.error("Error - prefix mapping service loading:", e);
      return null;
    }

    // TableMinerPlus initialization
    final Map<String, SemanticTableInterpreter> semanticTableInterpreters;
    try {
      kbf = null; // TODO: Instantiate with pms!
      semanticTableInterpreters = null; // TODO: Instantiate factory first, with cache service.
    } catch (final Exception e) {
      log.error("Error - TMP interpreters failed to initialize:", e);
      return null;
    }
    Preconditions.checkNotNull(semanticTableInterpreters);

    // TableMinerPlus algorithm run
    Map<KnowledgeBase, TAnnotation> results = new HashMap<>();
    try {
      for (Map.Entry<String, SemanticTableInterpreter> interpreterEntry : semanticTableInterpreters
          .entrySet()) {
        final KnowledgeBase base = getDummyBase(interpreterEntry.getKey());
        if (!configuration.getUsedBases().contains(base)) {
          continue;
        }

        Constraints constraints = new DefaultFeedbackToConstraintsAdapter().toConstraints(
            configuration.getFeedback(), base);

        TAnnotation annotationResult = interpreterEntry.getValue().start(table,
            configuration.isStatistical(), constraints);

        results.put(base, annotationResult);
      }
    } catch (STIException e) {
      log.error("Error - running TableMinerPlus algorithm:", e);
      return null;
    }

    // Odalic Result creation
    final Result odalicResult = new DefaultAnnotationToResultAdapter(
        new PrefixMappingEntitiesFactory(pms)).toResult(results);
    log.info("Odalic Result is: " + odalicResult);

    return new CoreSnapshot(odalicResult, parsingResult.getInput(), configuration);
  }

  public static KnowledgeBaseProxiesService getKnowledgeBaseProxyFactory() {
    return kbf;
  }

  public static Feedback createFeedback(boolean emptyFeedback) {
    if (emptyFeedback) {
      return new Feedback();
    }
    else {
      // subject columns example
      HashMap<String, ColumnPosition> subjectColumns = new HashMap<>();
      subjectColumns.put("DBpedia Clone", new ColumnPosition(0));

      // classifications example
      HashSet<EntityCandidate> candidatesClassification = new HashSet<>();
      candidatesClassification.add(
          new EntityCandidate(Entity.of("http://schema.org/Bookxyz", "Booooook"), new Score(1.0)));
      candidatesClassification
          .add(new EntityCandidate(Entity.of("http://schema.org/Book", "Book"), new Score(1.0)));
      HashMap<String, HashSet<EntityCandidate>> headerAnnotation = new HashMap<>();
      headerAnnotation.put("DBpedia Clone", candidatesClassification);
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
      HashMap<String, HashSet<EntityCandidate>> cellAnnotation = new HashMap<>();
      cellAnnotation.put("DBpedia Clone", candidatesDisambiguation);
      HashSet<Disambiguation> disambiguations = new HashSet<>();
      disambiguations.add(new Disambiguation(new CellPosition(0, 0),
          new CellAnnotation(cellAnnotation, cellAnnotation)));

      // relations example
      HashSet<EntityCandidate> candidatesRelation = new HashSet<>();
      candidatesRelation.add(new EntityCandidate(
          Entity.of("http://dbpedia.org/property/authorxyz", ""), new Score(1.0)));
      candidatesRelation.add(
          new EntityCandidate(Entity.of("http://dbpedia.org/property/author", ""), new Score(1.0)));
      HashMap<String, HashSet<EntityCandidate>> columnRelationAnnotation = new HashMap<>();
      columnRelationAnnotation.put("DBpedia Clone", candidatesRelation);
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

      // dataCubeComponents example
      HashSet<DataCubeComponent> dataCubeComponents = new HashSet<>();
      dataCubeComponents.add(createDCC(4, ComponentTypeValue.DIMENSION,
          "http://odalic.eu/schema/districtName", "District name"));
      dataCubeComponents.add(createDCC(6, ComponentTypeValue.DIMENSION,
          "http://odalic.eu/schema/lauName", "Lau name"));
      dataCubeComponents.add(createDCC(2, ComponentTypeValue.DIMENSION,
          "http://odalic.eu/schema/nuts3", "NUTS 3"));
      dataCubeComponents.add(createDCC(7, ComponentTypeValue.MEASURE,
          "http://odalic.eu/schema/livebirths", "Livebirths"));
      dataCubeComponents.add(createDCC(8, ComponentTypeValue.MEASURE,
          "http://dbpedia.org/ontology/year", "Year"));

      /**/
      // statistical data feedback example
      return new Feedback(ImmutableMap.of(), ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of(),
          ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of(), dataCubeComponents);
      /*/

      // construction example
      return new Feedback(subjectColumns, columnIgnores, columnAmbiguities, classifications,
          relations, disambiguations, ambiguities, ImmutableSet.of());
      /**/
    }
  }

  private static DataCubeComponent createDCC(int col, ComponentTypeValue comp, String uri, String label) {
    HashMap<String, ComponentTypeValue> compMap = new HashMap<>();
    compMap.put("DBpedia", comp);
    HashSet<EntityCandidate> predicateSet = new HashSet<>();
    predicateSet.add(new EntityCandidate(Entity.of(uri, label), new Score(1.0)));
    HashMap<String, HashSet<EntityCandidate>> predicateMap = new HashMap<>();
    predicateMap.put("DBpedia", predicateSet);
    return new DataCubeComponent(new ColumnPosition(col), new StatisticalAnnotation(compMap, predicateMap));
  }
  
  private static KnowledgeBase getDummyBase(final String name) {    
    final User owner = new User("dummy@dummy.com", "dummyHash", Role.ADMINISTRATOR);
    
    final GroupBuilder groupBuilder = new DefaultGroupBuilder();
    groupBuilder.setId("DummyGroup");
    groupBuilder.setOwner(owner);
    
    final KnowledgeBaseBuilder builder = new KnowledgeBaseBuilder();
    
    builder.setName(name);
    builder.setOwner(owner);
    
    try {
      builder.setEndpoint(new URL("http://dummy.com"));
    } catch (final MalformedURLException e) {
      throw new RuntimeException(e);
    }
    
    builder.addSelectedGroup(groupBuilder.build());
        
    return builder.build();
  }
}
