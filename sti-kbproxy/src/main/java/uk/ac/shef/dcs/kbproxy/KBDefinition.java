package uk.ac.shef.dcs.kbproxy;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Information about the knowledge base. Created by Jan
 */
public class KBDefinition {

  public enum SEARCH_CLASS_TYPE_MODE_VALUE {
    DIRECT, INDIRECT
  }


  // region Consts
  private static final String PATH_SEPARATOR = "\\|";
  private static final String URL_SEPARATOR = " ";

  private static final String NAME_PROPERTY_NAME = "kb.name";

  private static final String SPARQL_ENDPOINT_PROPERTY_NAME = "kb.endpoint";
  private static final String ONTOLOGY_URI_PROPERTY_NAME = "kb.ontologyURI";
  private static final String STOP_LIST_FILE_PROPERTY_NAME = "kb.stopListFile";

  private static final String CACHE_TEMPLATE_PATH_PROPERTY_NAME = "kb.cacheTemplatePath";

  private static final String STRUCTURE_PROPERTY_NAME = "kb.structure";
  private static final String LANGUAGE_SUFFIX = "kb.languageSuffix";
  private static final String SEARCH_CLASS_TYPE_MODE = "kb.search.class.type.mode";
  private static final String USE_BIF_CONTAINS = "kb.useBifContains";
  private static final String PREDICATE_NAME_PROPERTY_NAME = "kb.predicate.name";
  private static final String PREDICATE_LABEL_PROPERTY_NAME = "kb.predicate.label";
  private static final String PREDICATE_DESCRIPTION_PROPERTY_NAME = "kb.predicate.description";
  private static final String PREDICATE_TYPE_PROPERTY_NAME = "kb.predicate.type";

  private static final String STRUCTURE_CLASS = "kb.structure.class";
  private static final String STRUCTURE_PROPERTY = "kb.structure.property";

  private static final String INSERT_SUPPORTED = "kb.insert.supported";
  private static final String INSERT_PREFIX_SCHEMA_ELEMENT = "kb.insert.prefix.schema.element";
  private static final String INSERT_PREFIX_DATA_ELEMENT = "kb.insert.prefix.data.element";
  private static final String INSERT_ROOT_CLASS = "kb.insert.root.class";
  private static final String INSERT_LABEL = "kb.insert.label";
  private static final String INSERT_ALTERNATIVE_LABEL = "kb.insert.alternative.label";
  private static final String INSERT_SUBCLASS_OF = "kb.insert.subclass.of";
  private static final String INSERT_CLASS_TYPE = "kb.insert.class.type";
  private static final String INSERT_GRAPH = "kb.insert.graph";
  private static final String INSERT_PROPERTY_TYPE = "kb.insert.property.type";
  private static final String INSERT_SUB_PROPERTY = "kb.insert.sub.property";

  private static final String STRUCTURE_INSTANCE_OF = "kb.structure.instance.of";
  private static final String STRUCTURE_DOMAIN = "kb.structure.domain";
  private static final String STRUCTURE_RANGE = "kb.structure.range";

  private static final String DEFAULT_STRUCTURE_INSTANCE_OF =
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  private static final String DEFAULT_STRUCTURE_DOMAIN =
      "http://www.w3.org/2000/01/rdf-schema#domain";
  private static final String DEFAULT_STRUCTURE_RANGE =
      "http://www.w3.org/2000/01/rdf-schema#range";

  // endregion

  // region Fields

  private final Map<String, Set<String>> structure = new HashMap<>();
  protected final Logger log = LoggerFactory.getLogger(getClass());

  private String name;
  private String sparqlEndpoint;
  private String ontologyUri;
  private String stopListFile;

  private String languageSuffix;
  private SEARCH_CLASS_TYPE_MODE_VALUE searchClassTypeMode;
  private boolean useBifContains;

  private String cacheTemplatePath;

  private boolean insertSupported;
  private URI insertSchemaElementPrefix;
  private URI insertDataElementPrefix;
  private String insertRootClass;
  private String insertLabel;
  private String insertAlternativeLabel;
  private String insertSubclassOf;
  private String insertClassType;
  private String insertGraph;
  private String insertPropertyType;
  private String insertSubProperty;

  private String structureInstanceOf = DEFAULT_STRUCTURE_INSTANCE_OF;
  private String structureDomain = DEFAULT_STRUCTURE_DOMAIN;
  private String structureRange = DEFAULT_STRUCTURE_RANGE;

  // endregion

  // region Properties

  public KBDefinition() {
    this.structure.put(PREDICATE_NAME_PROPERTY_NAME, new HashSet<>());
    this.structure.put(PREDICATE_LABEL_PROPERTY_NAME, new HashSet<>());
    this.structure.put(PREDICATE_DESCRIPTION_PROPERTY_NAME, new HashSet<>());
    this.structure.put(PREDICATE_TYPE_PROPERTY_NAME, new HashSet<>());
    this.structure.put(STRUCTURE_CLASS, new HashSet<>());
    this.structure.put(STRUCTURE_PROPERTY, new HashSet<>());
  }

  public String getCacheTemplatePath() {
    return this.cacheTemplatePath;
  }

  public String getInsertAlternativeLabel() {
    return this.insertAlternativeLabel;
  }

  public String getInsertClassType() {
    return this.insertClassType;
  }

  public URI getInsertDataElementPrefix() {
    return this.insertDataElementPrefix;
  }

  public String getInsertGraph() {
    return this.insertGraph;
  }

  public String getInsertLabel() {
    return this.insertLabel;
  }

  public String getInsertPropertyType() {
    return this.insertPropertyType;
  }

  public String getInsertRootClass() {
    return this.insertRootClass;
  }

  public URI getInsertSchemaElementPrefix() {
    return this.insertSchemaElementPrefix;
  }

  public String getInsertSubclassOf() {
    return this.insertSubclassOf;
  }

  public String getInsertSubProperty() {
    return this.insertSubProperty;
  }


  public String getLanguageSuffix() {
    return this.languageSuffix;
  }

  /**
   * Returns the name of the knowledge base
   *
   * @return
   */
  public String getName() {
    return this.name;
  }

  public String getOntologyUri() {
    return this.ontologyUri;
  }

  public Set<String> getPredicateDescription() {
    return this.structure.get(PREDICATE_DESCRIPTION_PROPERTY_NAME);
  }

  public Set<String> getPredicateLabel() {
    return this.structure.get(PREDICATE_LABEL_PROPERTY_NAME);
  }

  public Set<String> getPredicateName() {
    return this.structure.get(PREDICATE_NAME_PROPERTY_NAME);
  }

  public Set<String> getPredicateType() {
    return this.structure.get(PREDICATE_TYPE_PROPERTY_NAME);
  }

  public SEARCH_CLASS_TYPE_MODE_VALUE getSearchClassTypeMode() {
    return this.searchClassTypeMode;
  }

  /**
   * Returns the SPARQL endpoint used for connecting to the knowledge base
   *
   * @return
   */
  public String getSparqlEndpoint() {
    return this.sparqlEndpoint;
  }

  public String getStopListFile() {
    return this.stopListFile;
  }

  public Set<String> getStructureClass() {
    return this.structure.get(STRUCTURE_CLASS);
  }

  public String getStructureDomain() {
    return this.structureDomain;
  }

  public String getStructureInstanceOf() {
    return this.structureInstanceOf;
  }

  public Set<String> getStructureProperty() {
    return this.structure.get(STRUCTURE_PROPERTY);
  }

  public String getStructureRange() {
    return this.structureRange;
  }

  public boolean getUseBifContains() {
    return this.useBifContains;
  }

  public boolean isInsertSupported() {
    return this.insertSupported;
  }

  /**
   * Loads KB definition from the knowledge base properties.
   *
   * @param kbProperties Properties of the knowledge base.
   * @throws IOException
   * @throws URISyntaxException
   */
  public void load(final Properties kbProperties, final String workingDirectory)
      throws IOException, URISyntaxException {
    // Name
    setName(kbProperties.getProperty(NAME_PROPERTY_NAME));

    // Endpoint and ontology
    setSparqlEndpoint(kbProperties.getProperty(SPARQL_ENDPOINT_PROPERTY_NAME));
    setOntologyUri(kbProperties.getProperty(ONTOLOGY_URI_PROPERTY_NAME));
    setStopListFile(
        combinePaths(workingDirectory, kbProperties.getProperty(STOP_LIST_FILE_PROPERTY_NAME)));

    setCacheTemplatePath(combinePaths(workingDirectory,
        kbProperties.getProperty(CACHE_TEMPLATE_PATH_PROPERTY_NAME)));

    // Language preferences
    if (kbProperties.containsKey(LANGUAGE_SUFFIX)) {
      setLanguageSuffix(kbProperties.getProperty(LANGUAGE_SUFFIX));
    }

    if (kbProperties.containsKey(SEARCH_CLASS_TYPE_MODE)) {
      setSearchClassTypeMode(SEARCH_CLASS_TYPE_MODE_VALUE
          .valueOf(kbProperties.getProperty(SEARCH_CLASS_TYPE_MODE).toUpperCase()));
      // check the value is correct
      if (!getSearchClassTypeMode().equals(SEARCH_CLASS_TYPE_MODE_VALUE.INDIRECT)
          && !getSearchClassTypeMode().equals(SEARCH_CLASS_TYPE_MODE_VALUE.DIRECT)) {
        this.log.warn("Incorect value for kb.search.class.type.mode: {}", getSearchClassTypeMode());
        setSearchClassTypeMode(SEARCH_CLASS_TYPE_MODE_VALUE.INDIRECT);
        this.log.info("Using default value for kb.search.class.type.mode: ",
            SEARCH_CLASS_TYPE_MODE_VALUE.INDIRECT.toString());
      }
    } else {
      // set default choice
      this.log.warn(
          "Option kb.search.class.type.mode not available in KB config file, setting to default value: indirect");
      setSearchClassTypeMode(SEARCH_CLASS_TYPE_MODE_VALUE.INDIRECT);
    }

    // Vistuoso specific settings
    if (kbProperties.containsKey(USE_BIF_CONTAINS)) {
      setUseBifContains(Boolean.parseBoolean(kbProperties.getProperty(USE_BIF_CONTAINS)));
    }

    // Loading structure
    // Individual paths to definition files are separated by ";"
    final String structureDefinitions = kbProperties.getProperty(STRUCTURE_PROPERTY_NAME);
    final String[] structureDefinitionsArray = structureDefinitions.split(PATH_SEPARATOR);

    for (final String structureDefinitionsFile : structureDefinitionsArray) {
      final String definitionFileNormalized =
          combinePaths(workingDirectory, structureDefinitionsFile);

      final File file = new File(definitionFileNormalized);
      if (!file.exists() || file.isDirectory()) {
        this.log.error("The specified properties file does not exist: " + definitionFileNormalized);
        continue;
      }

      final Properties properties = new Properties();
      try (InputStream fileStream = new FileInputStream(definitionFileNormalized)) {
        properties.load(fileStream);
        loadStructure(properties);
      }
    }

    if (kbProperties.containsKey(STRUCTURE_INSTANCE_OF)) {
      setStructureInstanceOf(kbProperties.getProperty(STRUCTURE_INSTANCE_OF));
    }
    if (kbProperties.containsKey(STRUCTURE_DOMAIN)) {
      setStructureDomain(kbProperties.getProperty(STRUCTURE_DOMAIN));
    }
    if (kbProperties.containsKey(STRUCTURE_RANGE)) {
      setStructureRange(kbProperties.getProperty(STRUCTURE_RANGE));
    }

    // SPARQL insert
    if (kbProperties.containsKey(INSERT_SUPPORTED)) {
      setInsertSupported(Boolean.parseBoolean(kbProperties.getProperty(INSERT_SUPPORTED)));
    }

    if (isInsertSupported()) {
      setInsertSchemaElementPrefix(new URI(kbProperties.getProperty(INSERT_PREFIX_SCHEMA_ELEMENT)));
      setInsertDataElementPrefix(new URI(kbProperties.getProperty(INSERT_PREFIX_DATA_ELEMENT)));
      setInsertLabel(kbProperties.getProperty(INSERT_LABEL));
      setInsertAlternativeLabel(kbProperties.getProperty(INSERT_ALTERNATIVE_LABEL));
      setInsertRootClass(kbProperties.getProperty(INSERT_ROOT_CLASS));
      setInsertSubclassOf(kbProperties.getProperty(INSERT_SUBCLASS_OF));
      setInsertClassType(kbProperties.getProperty(INSERT_CLASS_TYPE));
      setInsertGraph(kbProperties.getProperty(INSERT_GRAPH));
      setInsertPropertyType(kbProperties.getProperty(INSERT_PROPERTY_TYPE));
      setInsertSubProperty(kbProperties.getProperty(INSERT_SUB_PROPERTY));
    }
  }

  private void loadStructure(final Properties properties) {
    for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
      final String key = (String) entry.getKey();
      final Set<String> structureValues = this.structure.getOrDefault(key, null);

      if (structureValues == null) {
        this.log.error("Unknown structure key: " + key);
        continue;
      }

      final String values = (String) entry.getValue();
      final String[] valuesArray = values.split(URL_SEPARATOR);

      for (final String value : valuesArray) {
        final boolean valueAdded = structureValues.add(value);

        if (!valueAdded) {
          this.log.warn(String.format("Structure value %1$s for the key %2$s is already loaded.",
              value, key));
        }
      }
    }
  }

  private void setCacheTemplatePath(final String cacheTemplatePath) {
    this.cacheTemplatePath = cacheTemplatePath;
  }

  private void setInsertAlternativeLabel(final String insertAlternativeLabel) {
    this.insertAlternativeLabel = insertAlternativeLabel;
  }

  private void setInsertClassType(final String insertClassType) {
    this.insertClassType = insertClassType;
  }

  private void setInsertDataElementPrefix(final URI insertDataElementPrefix) {
    this.insertDataElementPrefix = insertDataElementPrefix;
  }

  private void setInsertGraph(final String insertGraph) {
    this.insertGraph = insertGraph;
  }

  private void setInsertLabel(final String insertLabel) {
    this.insertLabel = insertLabel;
  }

  private void setInsertPropertyType(final String insertPropertyType) {
    this.insertPropertyType = insertPropertyType;
  }

  private void setInsertRootClass(final String insertRootClass) {
    this.insertRootClass = insertRootClass;
  }

  private void setInsertSchemaElementPrefix(final URI insertSchemaElementPrefix) {
    this.insertSchemaElementPrefix = insertSchemaElementPrefix;
  }

  private void setInsertSubclassOf(final String insertSubclassOf) {
    this.insertSubclassOf = insertSubclassOf;
  }

  private void setInsertSubProperty(final String insertSubProperty) {
    this.insertSubProperty = insertSubProperty;
  }

  private void setInsertSupported(final boolean insertSupported) {
    this.insertSupported = insertSupported;
  }

  private void setLanguageSuffix(final String languageSuffix) {
    this.languageSuffix = languageSuffix;
  }

  private void setName(final String name) {
    this.name = name;
  }

  private void setOntologyUri(final String ontologyUri) {
    this.ontologyUri = ontologyUri;
  }

  public void setSearchClassTypeMode(final SEARCH_CLASS_TYPE_MODE_VALUE searchClassTypeMode) {
    this.searchClassTypeMode = searchClassTypeMode;
  }

  private void setSparqlEndpoint(final String sparqlEndpoint) {
    this.sparqlEndpoint = sparqlEndpoint;
  }

  private void setStopListFile(final String stopListFile) {
    this.stopListFile = stopListFile;
  }

  private void setStructureDomain(final String structureDomain) {
    this.structureDomain = structureDomain;
  }

  // endregion

  // region constructor

  private void setStructureInstanceOf(final String structureInstanceOf) {
    this.structureInstanceOf = structureInstanceOf;
  }

  // endregion

  // region Methods

  private void setStructureRange(final String structureRange) {
    this.structureRange = structureRange;
  }

  private void setUseBifContains(final boolean useBifContains) {
    this.useBifContains = useBifContains;
  }

  // endregion
}
