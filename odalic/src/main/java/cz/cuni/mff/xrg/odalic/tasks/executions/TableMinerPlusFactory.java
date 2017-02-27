package cz.cuni.mff.xrg.odalic.tasks.executions;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.simmetrics.metrics.StringMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LEARNING;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LEARNINGPreliminaryColumnClassifier;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LEARNINGPreliminaryDisamb;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTagger;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.LiteralColumnTaggerImpl;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.TCellDisambiguator;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.TColumnClassifier;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.TColumnColumnRelationEnumerator;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.TMPOdalicInterpreter;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.UPDATE;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.OSPD_nonEmpty;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentCellRanker;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler.TContentTContentRowRankerImpl;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPClazzScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPEntityScorer;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.TMPRelationScorer;
import uk.ac.shef.dcs.sti.core.feature.ConceptBoWCreatorImpl;
import uk.ac.shef.dcs.sti.core.feature.RelationBoWCreatorImpl;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
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

  private static final Logger logger = LoggerFactory.getLogger(TableMinerPlusFactory.class);

  private final String propertyFilePath;

  private final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory;

  private Map<String, SemanticTableInterpreter> interpreters;
  private Properties properties;
  private final Lock initLock = new ReentrantLock();
  private boolean isInitialized = false;

  @Autowired
  public TableMinerPlusFactory(final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory) {
    this(knowledgeBaseProxyFactory, System.getProperty("cz.cuni.mff.xrg.odalic.sti"));
  }

  public TableMinerPlusFactory(final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory,
      final String propertyFilePath) {
    Preconditions.checkNotNull(knowledgeBaseProxyFactory);
    Preconditions.checkNotNull(propertyFilePath);

    this.knowledgeBaseProxyFactory = knowledgeBaseProxyFactory;
    this.propertyFilePath = propertyFilePath;
  }

  private String getAbsolutePath(final String propertyName) {
    return combinePaths(this.properties.getProperty(PROPERTY_HOME),
        this.properties.getProperty(propertyName));
  }

  @Override
  public Map<String, SemanticTableInterpreter> getInterpreters() throws STIException, IOException {
    if (this.interpreters == null) {
      initComponents();
    }
    return this.interpreters;
  }

  private String getNLPResourcesDir() throws STIException {
    final String prop = getAbsolutePath(PROPERTY_NLP_RESOURCES);
    if ((prop == null) || !new File(prop).exists()) {
      final String error = "Cannot proceed: nlp resources folder is not set or does not exist. "
          + PROPERTY_NLP_RESOURCES + "=" + prop;
      logger.error(error);
      throw new STIException(error);
    }
    return prop;
  }

  private List<String> getStopwords() throws STIException, IOException {
    return FileUtils.readList(getNLPResourcesDir() + File.separator + "stoplist.txt", true);
  }

  private TColumnClassifier initClassifier() throws STIException {
    try {
      return new TColumnClassifier(
          new TMPClazzScorer(getNLPResourcesDir(), new ConceptBoWCreatorImpl(), getStopwords(),
              STIConstantProperty.SCORER_CLAZZ_CONTEXT_WEIGHT) // all 1.0
      ); // header, column, out trivial, out important
    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing LEARNING components.", e);
    }
  }

  // Initialize kbsearcher, websearcher
  private void initComponents() throws STIException, IOException {
    this.initLock.lock();
    try {
      if (this.isInitialized) {
        return;
      }

      this.properties = new Properties();
      this.properties.load(new FileInputStream(this.propertyFilePath));

      // object to fetch things from KB
      final Map<String, KBProxy> kbProxyInstances = this.knowledgeBaseProxyFactory.getKBProxies();

      this.interpreters = new HashMap<>();
      for (final KBProxy kbProxy : kbProxyInstances.values()) {
        final SubjectColumnDetector subcolDetector = initSubColDetector(kbProxy);

        final TCellDisambiguator disambiguator = initDisambiguator(kbProxy);
        final TColumnClassifier classifier = initClassifier();
        final TContentCellRanker selector = new OSPD_nonEmpty();

        final LEARNING learning = initLearning(kbProxy, selector, disambiguator, classifier);

        final UPDATE update = initUpdate(kbProxy, selector, disambiguator, classifier);

        final TColumnColumnRelationEnumerator relationEnumerator = initRelationEnumerator();

        // object to consolidate previous output, further computeElementScores columns
        // and disambiguate entities
        final LiteralColumnTagger literalColumnTagger = new LiteralColumnTaggerImpl();

        final SemanticTableInterpreter interpreter = new TMPOdalicInterpreter(subcolDetector,
            learning, update, relationEnumerator, literalColumnTagger);

        this.interpreters.put(kbProxy.getName(), interpreter);

        this.isInitialized = true;
      }
    } finally {
      this.initLock.unlock();
    }
  }

  private TCellDisambiguator initDisambiguator(final KBProxy kbProxy) throws STIException {
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

  private LEARNING initLearning(final KBProxy kbProxy, final TContentCellRanker selector,
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

  private TColumnColumnRelationEnumerator initRelationEnumerator() throws STIException {
    logger.info("Initializing RELATIONLEARNING components ...");
    try {
      // object to computeElementScores relations between columns
      final RelationScorer relationScorer =
          new TMPRelationScorer(getNLPResourcesDir(), new RelationBoWCreatorImpl(),
              getStopwords(), STIConstantProperty.SCORER_RELATION_CONTEXT_WEIGHT
          // new double[]{1.0, 1.0, 0.0, 0.0, 1.0}
          );
      return new TColumnColumnRelationEnumerator(
          new AttributeValueMatcher(STIConstantProperty.ATTRIBUTE_MATCHER_MIN_SCORE, getStopwords(),
              StringMetrics.levenshtein()),
          relationScorer);
    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing RELATIONLEARNING components.", e);
    }
  }

  private SubjectColumnDetector initSubColDetector(final KBProxy kbProxy) throws STIException {
    logger.info("Initializing SUBJECT COLUMN DETECTION components ...");
    try {
      return new SubjectColumnDetector(new TContentTContentRowRankerImpl(),
          this.properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS),
          StringUtils.split(
              this.properties.getProperty(PROPERTY_TMP_IINF_WEBSEARCH_STOPPING_CLASS_CONSTR_PARAM),
              ','),
          // new String[]{"0.0", "1", "0.01"},
          kbProxy.getSolrServer(PROPERTY_WEBSEARCH_CACHE_CORENAME), getNLPResourcesDir(),
          Boolean.valueOf(
              this.properties.getProperty(PROPERTY_TMP_SUBJECT_COLUMN_DETECTION_USE_WEBSEARCH)),
          getStopwords(), getAbsolutePath(PROPERTY_WEBSEARCH_PROP_FILE));
    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing SUBJECT COLUMN DETECTION components: "
          + this.properties.getProperty(PROPERTY_WEBSEARCH_PROP_FILE), e);
    }
  }

  private UPDATE initUpdate(final KBProxy kbProxy, final TContentCellRanker selector,
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
