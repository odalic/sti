package cz.cuni.mff.xrg.odalic.tasks.executions;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import cz.cuni.mff.xrg.odalic.files.formats.DefaultApacheCsvFormatAdapter;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.input.ml.*;
import org.apache.commons.lang3.StringUtils;
import org.simmetrics.metrics.StringMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.bases.proxies.KnowledgeBaseProxiesService;
import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;
import uk.ac.shef.dcs.kbproxy.Proxy;
import uk.ac.shef.dcs.kbproxy.solr.CacheProviderService;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.*;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.DefaultMLPreClassifier;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.MLPreClassifier;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.MLPropertiesLoader;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.NoMLPreClassifier;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.classifier.MLClassifier;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.classifier.RandomForestMLClassifier;
import cz.cuni.mff.xrg.odalic.input.ml.TaskMLConfiguration;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing.DefaultMLFeatureDetector;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing.MLFeatureDetector;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.OSPD_nonEmpty;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPClazzScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPEntityScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPRelationScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing.InputValue;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLOntologyDefinition;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLOntologyMapping;
import uk.ac.shef.dcs.sti.core.feature.ConceptBoWCreatorImpl;
import uk.ac.shef.dcs.sti.core.feature.RelationBoWCreatorImpl;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.FileUtils;

/**
 * Implementation of {@link SemanticTableInterpreterFactory} that provides
 * {@link TMPOdalicInterpreter} instances.
 *
 * @author Josef Janoušek
 *
 */
public final class TableMinerPlusFactory implements SemanticTableInterpreterFactory {

  private static final String PROPERTY_HOME = "sti.home";
  private static final String PROPERTY_WEBSEARCH_PROP_FILE = "sti.websearch.properties";
  private static final String PROPERTY_ML_PROP_FILE = "sti.ml.properties";
  private static final String PROPERTY_NLP_RESOURCES = "sti.nlp";

  private static final String PROPERTY_WEBSEARCH_CACHE_CORENAME = "websearch";

  private static final String PROPERTY_TMP_SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH =
      "sti.subjectcolumndetection.ws";
  private static final String PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS =
      "sti.iinf.websearch.stopping.class";
  private static final String PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS_CONSTR_PARAM =
      "sti.iinf.websearch.stopping.class.constructor.params";

  private static final String PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS =
      "sti.tmp.iinf.learning.stopping.class";
  private static final String PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS_CONSTR_PARAM =
      "sti.tmp.iinf.learning.stopping.class.constructor.params";

  private static final String PROPERTY_RELATIONS_USE_ML_CLASSIFIER =
      "sti.learning.relation.ml.classifier";

  private static final Logger logger = LoggerFactory.getLogger(TableMinerPlusFactory.class);

  private final KnowledgeBaseProxiesService knowledgeBaseProxyFactory;
  private final CacheProviderService cacheProviderService;

  private final Properties properties;

  @Autowired
  public TableMinerPlusFactory(final KnowledgeBaseProxiesService knowledgeBaseProxyFactory, final CacheProviderService cacheProviderService,
                               final PropertiesService propertiesService) {
    this(knowledgeBaseProxyFactory, cacheProviderService, propertiesService.get());
  }

  public TableMinerPlusFactory(final KnowledgeBaseProxiesService knowledgeBaseProxyFactory, final CacheProviderService cacheProviderService,
                               final Properties properties) {
    Preconditions.checkNotNull(knowledgeBaseProxyFactory, "The knowledgeBaseProxyFactory cannot be null!");
    Preconditions.checkNotNull(cacheProviderService, "The cacheProviderService cannot be null!");
    Preconditions.checkNotNull(properties, "The properties cannot be null!");

    this.knowledgeBaseProxyFactory = knowledgeBaseProxyFactory;
    this.cacheProviderService = cacheProviderService;
    this.properties = properties;
  }

  private String getAbsolutePath(final String propertyName) {
    return combinePaths(this.properties.getProperty(PROPERTY_HOME),
        this.properties.getProperty(propertyName));
  }

  @Override
  public Map<String, SemanticTableInterpreter> getInterpreters(final String userId,
                                                               final Set<? extends KnowledgeBase> bases,
                                                               final TaskMLConfiguration mlConfig) throws STIException, IOException {
    return initializeInterpreters(userId, bases, mlConfig);
  }

  private String getNLPResourcesDir() throws STIException {
    return getAndValidatePath(PROPERTY_NLP_RESOURCES, "nlp resources folder");
  }

  private String getAndValidatePath(String pathPropertyName, String propertyDescription) throws STIException {
    final String prop = getAbsolutePath(pathPropertyName);
    if ((prop == null) || !new File(prop).exists()) {
      final String error = "Cannot proceed: " + propertyDescription + " is not set or does not exist. "
              + pathPropertyName + "=" + prop;
      logger.error(error);
      throw new STIException(error);
    }
    return prop;
  }

  /*private boolean useMlClassifier() throws STIException {
    final String prop = this.properties.getProperty(PROPERTY_RELATIONS_USE_ML_CLASSIFIER);

    if (prop != null) {
      return Boolean.valueOf(prop);
    } else {
      final String error = "Cannot proceed: '" + PROPERTY_RELATIONS_USE_ML_CLASSIFIER + "' is not set or is invalid. "
              + PROPERTY_RELATIONS_USE_ML_CLASSIFIER + "=" + prop;
      logger.error(error);
      throw new STIException(error);
    }
  }*/

  private List<String> getStopwords() throws STIException, IOException {
    return FileUtils.readList(getNLPResourcesDir() + File.separator + "stoplist.txt", true);
  }

  private TColumnClassifier initClassifier() throws STIException {
    try {
      ClazzScorer clazzScorer = initClazzScorer();
      return new TColumnClassifier(clazzScorer); // header, column, out trivial, out important
    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing LEARNING components.", e);
    }
  }

  private TMPClazzScorer initClazzScorer() throws STIException {
    try {
      return new TMPClazzScorer(getNLPResourcesDir(), new ConceptBoWCreatorImpl(), getStopwords(),
              STIConstantProperty.SCORER_CLAZZ_CONTEXT_WEIGHT); // all 1.0
    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing Clazz Scorer components.", e);
    }
  }

  // Initialize kbsearcher, websearcher
  private synchronized Map<String, SemanticTableInterpreter> initializeInterpreters(final String userId,
                                          final Set<? extends KnowledgeBase> bases,
                                          final TaskMLConfiguration mlConfig) throws STIException, IOException {

      // object to fetch things from KB
      final Table<String, String, Proxy> kbProxyInstances = this.knowledgeBaseProxyFactory.toProxies(bases);

      final Map<String, SemanticTableInterpreter> interpreters = new HashMap<>();
      for (final Map.Entry<String, Proxy> kbProxyEntry : kbProxyInstances.row(userId).entrySet()) {
        final Proxy kbProxy = kbProxyEntry.getValue();

        final MLPreClassifier mlPreClassifier = initMLPreClassifier(mlConfig);

        final SubjectColumnDetector subcolDetector = initSubColDetector(kbProxy);

        final TCellDisambiguator disambiguator = initDisambiguator(kbProxy);
        final TColumnClassifier classifier = initClassifier();
        final TContentCellRanker selector = new OSPD_nonEmpty();

        final LEARNING learning = initLearning(kbProxy, selector, disambiguator, classifier);

        final UPDATE update = initUpdate(kbProxy, selector, disambiguator, classifier);

        final TColumnColumnRelationEnumerator relationEnumerator = initRelationEnumerator(mlPreClassifier.getMlOntologyDefinition());

        // object to consolidate previous output, further computeElementScores columns
        // and disambiguate entities
        final LiteralColumnTagger literalColumnTagger = new LiteralColumnTaggerImpl();

        final SemanticTableInterpreter interpreter = new TMPOdalicInterpreter(mlPreClassifier, subcolDetector,
            learning, update, relationEnumerator, literalColumnTagger);

        interpreters.put(kbProxyEntry.getKey(), interpreter);
      }
      
      return interpreters;
  }

  private MLPreClassifier initMLPreClassifier(final TaskMLConfiguration mlConfig) throws STIException {
    if (mlConfig.isUseMlClassifier()) {
      // ML should be used
      String mlPropsFilePath = getAbsolutePath(PROPERTY_ML_PROP_FILE);

      try {
        final String homePath = this.properties.getProperty(PROPERTY_HOME);
        final MLFeatureDetector mlFeatureDetector = new DefaultMLFeatureDetector();

        MLPropertiesLoader mlPropertiesLoader = new MLPropertiesLoader(homePath, mlPropsFilePath);

        // parse input dataset
        URL trainingDatasetPath = mlConfig.getTrainingDatasetFile().getLocation();
        Format trainingDatasetConfiguration = mlConfig.getTrainingDatasetFile().getFormat();
        DatasetFileReader datasetFileReader = new CsvDatasetFileReader(new DefaultApacheCsvFormatAdapter());
        InputValue[] trainingDatasetInputValues = datasetFileReader.readDatasetFile(trainingDatasetPath, trainingDatasetConfiguration);

        // parse ontology mapping
        OntologyMappingReader ontologyMappingReader = new JsonOntologyMappingReader();
        MLOntologyMapping ontologyMapping =
                ontologyMappingReader.readOntologyMapping(mlPropertiesLoader.getMLClassifierOntologyMappingFilePath());

        // load ontology definitions
        OntologyDefinitionReader ontologyDefinitionReader = new Rdf4jOntologyDefinitionReader();
        MLOntologyDefinition ontologyDefinition = ontologyDefinitionReader.readOntologyDefinitions(
                mlPropertiesLoader.getMLClassifierOntologyDefinitionFilePaths()
        );


        MLClassifier classifier = new RandomForestMLClassifier(
                homePath, mlPropertiesLoader.getProperties(), mlFeatureDetector, trainingDatasetInputValues
        );
        classifier.trainClassifier();
        return new DefaultMLPreClassifier(classifier, ontologyMapping, ontologyDefinition);

      } catch (final Exception e) {
        logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
        throw new STIException("Failed initializing Machine Learning Classifier components.", e);
      }
    } else {
      // ML is disabled
      return new NoMLPreClassifier();
    }
  }

  /*private MLClassifier initMLClassifier(String mlPropsFilePath) throws STIException {
    try {
      final String homePath = this.properties.getProperty(PROPERTY_HOME);
      final MLFeatureDetector mlFeatureDetector = new DefaultMLFeatureDetector();

      MLPropertiesLoader mlPropertiesLoader = new MLPropertiesLoader(homePath, mlPropsFilePath);

      // parse input dataset
      String trainingDatasetPath = mlPropertiesLoader.getMLClassifierTrainingDatasetFilePath();
      // TODO pass training set path and format from task submission
      Format trainingDatasetConfiguration = new Format(Charset.forName("UTF8"), '|', true, null, null, null, "\n");
      DatasetFileReader datasetFileReader = new CsvDatasetFileReader( new DefaultApacheCsvFormatAdapter());
      InputValue[] trainingDatasetInputValues = datasetFileReader.readDatasetFile(trainingDatasetPath, trainingDatasetConfiguration);

      // parse ontology mapping
      OntologyMappingReader ontologyMappingReader = new JsonOntologyMappingReader();
      MLOntologyMapping ontologyMapping =
              ontologyMappingReader.readOntologyMapping(mlPropertiesLoader.getMLClassifierOntologyMappingFilePath());

      // load ontology definitions
      OntologyDefinitionReader ontologyDefinitionReader = new Rdf4jOntologyDefinitionReader();
      MLOntologyDefinition ontologyDefinition = ontologyDefinitionReader.readOntologyDefinitions(
              mlPropertiesLoader.getMLClassifierOntologyDefinitionFilePaths()
      );


      MLClassifier classifier = new RandomForestMLClassifier(
          homePath, mlPropertiesLoader.getProperties(), mlFeatureDetector, trainingDatasetInputValues, ontologyMapping
      );
      classifier.trainClassifier();
      return classifier;
    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing Machine Learning Classifier components.", e);
    }
  }*/

  private TCellDisambiguator initDisambiguator(final Proxy kbProxy) throws STIException {
    try {
      return new TCellDisambiguator(kbProxy,
          new TMPEntityScorer(getStopwords(), STIConstantProperty.SCORER_ENTITY_CONTEXT_WEIGHT,
              // row, column, column header, table context all
              getNLPResourcesDir()));
    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing LEARNING components.", e);
    }
  }

  private LEARNING initLearning(final Proxy kbProxy, final TContentCellRanker selector,
      final TCellDisambiguator disambiguator, final TColumnClassifier classifier)
      throws STIException {
    logger.info("Initializing LEARNING components ...");
    try {
      final LEARNINGPreliminaryColumnClassifier preliminaryClassify =
          new LEARNINGPreliminaryColumnClassifier(selector,
              this.properties.getProperty(PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS),
              StringUtils.split(this.properties
                  .getProperty(PROPERTY_TMP_IINF_LEARNING_STOPPING_CLASS_CONSTR_PARAM), ','),
              kbProxy, disambiguator, classifier);
      final LEARNINGPreliminaryDisamb preliminaryDisamb =
          new LEARNINGPreliminaryDisamb(kbProxy, disambiguator, classifier);

      return new LEARNING(preliminaryClassify, preliminaryDisamb);
    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing LEARNING components.", e);
    }
  }

  private TColumnColumnRelationEnumerator initRelationEnumerator(MLOntologyDefinition mlOntologyDefinition) throws STIException {
    logger.info("Initializing RELATIONLEARNING components ...");
    try {
      // object to computeElementScores relations between columns
      final RelationScorer relationScorer =
          new TMPRelationScorer(getNLPResourcesDir(), new RelationBoWCreatorImpl(),
              getStopwords(), STIConstantProperty.SCORER_RELATION_CONTEXT_WEIGHT
          // new double[]{1.0, 1.0, 0.0, 0.0, 1.0}
          );


      /*if (useMlClassifier()) {
        // append ML Classifier to existing components
        final MLClassifier mlClassifier = initMLClassifier(getAbsolutePath(PROPERTY_ML_PROP_FILE));
        return new TMLColumnColumnRelationEnumerator(
                new AttributeValueMatcher(STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE, getStopwords(),
                        StringMetrics.levenshtein()),
                relationScorer,
                mlClassifier);

      } else {
        // dont use ML Classifier
        return new TColumnColumnRelationEnumerator(
                new AttributeValueMatcher(STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE, getStopwords(),
                        StringMetrics.levenshtein()),
                relationScorer);
      }*/

      return new TMLColumnColumnRelationEnumerator(
              new AttributeValueMatcher(STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE, getStopwords(),
                      StringMetrics.levenshtein()), relationScorer, mlOntologyDefinition);

    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing RELATIONLEARNING components.", e);
    }
  }

  private SubjectColumnDetector initSubColDetector(final Proxy kbProxy) throws STIException {
    logger.info("Initializing SUBJECT COLUMN DETECTION components ...");
    try {
      return new SubjectColumnDetector(new TContentTContentRowRankerImpl(),
          this.properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS),
          StringUtils.split(
              this.properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS_CONSTR_PARAM),
              ','),
          // new String[]{"0.0", "1", "0.01"},
          this.cacheProviderService.getCache(kbProxy.getName(), PROPERTY_WEBSEARCH_CACHE_CORENAME), getNLPResourcesDir(),
          Boolean.valueOf(
              this.properties.getProperty(PROPERTY_TMP_SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH)),
          getStopwords(), getAbsolutePath(PROPERTY_WEBSEARCH_PROP_FILE));
    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing SUBJECT COLUMN DETECTION components: "
          + this.properties.getProperty(PROPERTY_WEBSEARCH_PROP_FILE), e);
    }
  }

  private UPDATE initUpdate(final Proxy kbProxy, final TContentCellRanker selector,
      final TCellDisambiguator disambiguator, final TColumnClassifier classifier)
      throws STIException {
    logger.info("Initializing UPDATE components ...");
    try {
      return new UPDATE(selector, kbProxy, disambiguator, classifier, getStopwords(),
          getNLPResourcesDir());
    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing UPDATE components.", e);
    }
  }
}
