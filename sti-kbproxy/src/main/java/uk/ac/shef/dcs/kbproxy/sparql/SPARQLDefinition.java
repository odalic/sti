package uk.ac.shef.dcs.kbproxy.sparql;

import uk.ac.shef.dcs.kbproxy.KBDefinition;
import uk.ac.shef.dcs.kbproxy.KBProxyException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

/**
 * Created by JanVa_000 on 28.02.2017.
 */
public class SPARQLDefinition extends KBDefinition {
  public enum SEARCH_CLASS_TYPE_MODE_VALUE {
    DIRECT, INDIRECT
  }


  // region Consts
  private static final String PATH_SEPARATOR = "\\|";
  private static final String URL_SEPARATOR = " ";

  private static final String STRUCTURE_PREDICATE_TYPE = "kb.structure.predicate.type";
  private static final String STRUCTURE_PREDICATE_LABEL = "kb.structure.predicate.label";
  private static final String STRUCTURE_PREDICATE_DESCRIPTION = "kb.structure.predicate.description";

  private static final String STRUCTURE_TYPE_CLASS = "kb.structure.type.class";
  private static final String STRUCTURE_TYPE_PROPERTY = "kb.structure.type.property";

  private static final String ENDPOINT = "kb.endpoint";
  private static final String STRUCTURE = "kb.structure";

  private static final String FULLTEXT_ENABLED = "kb.fulltextEnabled";
  private static final String USE_BIF_CONTAINS = "kb.useBifContains";
  private static final String LANGUAGE_SUFFIX = "kb.languageSuffix";
  private static final String CLASS_TYPE_MODE = "kb.classTypeMode";

  private static final String STRUCTURE_PREDICATE_INSTANCE_OF = "kb.structure.predicate.instanceOf";
  private static final String STRUCTURE_PREDICATE_DOMAIN = "kb.structure.predicate.domain";
  private static final String STRUCTURE_PREDICATE_RANGE = "kb.structure.predicate.range";

  private static final String INSERT_GRAPH = "kb.insert.graph";

  private static final String INSERT_DEFAULT_CLASS = "kb.insert.defaultClass";
  private static final String INSERT_PREDICATE_LABEL = "kb.insert.predicate.label";
  private static final String INSERT_PREDICATE_ALTERNATIVE_LABEL = "kb.insert.predicate.alternativeLabel";
  private static final String INSERT_PREDICATE_SUBCLASS_OF = "kb.insert.predicate.subclassOf";
  private static final String INSERT_PREDICATE_SUB_PROPERTY_OF = "kb.insert.predicate.subPropertyOf";
  private static final String INSERT_TYPE_CLASS = "kb.insert.type.class";
  private static final String INSERT_TYPE_PROPERTY = "kb.insert.type.property";

  // Default values
  private static final String DEFAULT_FULLTEXT_ENABLED = "true";
  private static final String DEFAULT_USE_BIF_CONTAINS = "false";
  private static final String DEFAULT_LANGUAGE_SUFFIX = null;
  private static final String DEFAULT_CLASS_TYPE_MODE = "indirect";

  private static final String DEFAULT_STRUCTURE_PREDICATE_INSTANCE_OF =
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  private static final String DEFAULT_STRUCTURE_PREDICATE_DOMAIN =
          "http://www.w3.org/2000/01/rdf-schema#domain";
  private static final String DEFAULT_STRUCTURE_PREDICATE_RANGE =
          "http://www.w3.org/2000/01/rdf-schema#range";

  private static final String DEFAULT_INSERT_DEFAULT_CLASS = "http://www.w3.org/2002/07/owl#Thing";
  private static final String DEFAULT_INSERT_PREDICATE_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
  private static final String DEFAULT_INSERT_PREDICATE_ALTERNATIVE_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
  private static final String DEFAULT_INSERT_PREDICATE_SUBCLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
  private static final String DEFAULT_INSERT_PREDICATE_SUB_PROPERTY_OF = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";
  private static final String DEFAULT_INSERT_TYPE_CLASS = "http://www.w3.org/2002/07/owl#Class";
  private static final String DEFAULT_INSERT_TYPE_PROPERTY = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property";

  // endregion

  // region Fields

  private String endpoint;
  private final Map<String, Set<String>> structure = new HashMap<>();

  private boolean fulltextEnabled;
  private boolean useBifContains;
  private String languageSuffix;
  private SPARQLDefinition.SEARCH_CLASS_TYPE_MODE_VALUE classTypeMode;

  private String structureInstanceOf;
  private String structureDomain;
  private String structureRange;

  private String insertGraph;

  private String insertDefaultClass;
  private String insertPredicateLabel;
  private String insertPredicateAlternativeLabel;
  private String insertPredicateSubclassOf;
  private String insertPredicateSubPropertyOf;
  private String insertTypeClass;
  private String insertTypeProperty;

  // endregion

  // region Constructor

  public SPARQLDefinition() {
    this.structure.put(STRUCTURE_PREDICATE_LABEL, new HashSet<>());
    this.structure.put(STRUCTURE_PREDICATE_DESCRIPTION, new HashSet<>());
    this.structure.put(STRUCTURE_PREDICATE_TYPE, new HashSet<>());
    this.structure.put(STRUCTURE_TYPE_CLASS, new HashSet<>());
    this.structure.put(STRUCTURE_TYPE_PROPERTY, new HashSet<>());
  }

  // endregion

  // region Properties and Methods

  /**
   * Returns the SPARQL endpoint used for connecting to the knowledge base
   *
   * @return
   */
  public String getEndpoint() {
    return this.endpoint;
  }

  public String getInsertDefaultClass() {
    return this.insertDefaultClass;
  }

  public String getInsertGraph() {
    return this.insertGraph;
  }

  public String getInsertPredicateAlternativeLabel() {
    return this.insertPredicateAlternativeLabel;
  }

  public String getInsertPredicateLabel() {
    return this.insertPredicateLabel;
  }

  public String getInsertPredicateSubclassOf() {
    return this.insertPredicateSubclassOf;
  }

  public String getInsertPredicateSubPropertyOf() {
    return this.insertPredicateSubPropertyOf;
  }

  public String getInsertTypeClass() {
    return this.insertTypeClass;
  }

  public String getInsertTypeProperty() {
    return this.insertTypeProperty;
  }

  public String getLanguageSuffix() {
    return this.languageSuffix;
  }

  public Set<String> getStructurePredicateDescription() {
    return this.structure.get(STRUCTURE_PREDICATE_DESCRIPTION);
  }

  public Set<String> getStructurePredicateLabel() {
    return this.structure.get(STRUCTURE_PREDICATE_LABEL);
  }

  public Set<String> getStructurePredicateType() {
    return this.structure.get(STRUCTURE_PREDICATE_TYPE);
  }

  public SPARQLDefinition.SEARCH_CLASS_TYPE_MODE_VALUE getClassTypeMode() {
    return this.classTypeMode;
  }

  public String getStructureDomain() {
    return this.structureDomain;
  }

  public String getStructureInstanceOf() {
    return this.structureInstanceOf;
  }

  public String getStructureRange() {
    return this.structureRange;
  }

  public Set<String> getStructureTypeClass() {
    return this.structure.get(STRUCTURE_TYPE_CLASS);
  }

  public Set<String> getStructureTypeProperty() {
    return this.structure.get(STRUCTURE_TYPE_PROPERTY);
  }

  public boolean getUseBifContains() {
    return this.useBifContains;
  }

  public boolean isFulltextEnabled() {
    return fulltextEnabled;
  }

  /**
   * Loads KB definition from the knowledge base properties.
   *
   * @param kbProperties Properties of the knowledge base.
   * @param workingDirectory Current working directory.
   * @param cacheDirectory Current cache directory.
   * @throws IOException
   * @throws URISyntaxException
   */
  @Override
  public void load(
          final Properties kbProperties,
          final String workingDirectory,
          final String cacheDirectory)
          throws IOException, URISyntaxException, KBProxyException {
    super.load(kbProperties, workingDirectory, cacheDirectory);
    // Endpoint
    setEndpoint(getMandatoryValue(kbProperties, ENDPOINT));

    // Fulltext settings
    setFulltextEnabled(Boolean.parseBoolean(
            getOptionalValue(kbProperties, FULLTEXT_ENABLED, DEFAULT_FULLTEXT_ENABLED)));

    setUseBifContains(Boolean.parseBoolean(
            getOptionalValue(kbProperties, USE_BIF_CONTAINS, DEFAULT_USE_BIF_CONTAINS)));

    // Language suffix
    setLanguageSuffix(getOptionalValue(kbProperties, LANGUAGE_SUFFIX, DEFAULT_LANGUAGE_SUFFIX));

    // Class type mode
    setClassTypeMode(SPARQLDefinition.SEARCH_CLASS_TYPE_MODE_VALUE.valueOf(
            getOptionalValue(kbProperties, CLASS_TYPE_MODE, DEFAULT_CLASS_TYPE_MODE)
                    .toUpperCase()));

    // check the value is correct
    if (!getClassTypeMode().equals(SPARQLDefinition.SEARCH_CLASS_TYPE_MODE_VALUE.INDIRECT)
            && !getClassTypeMode().equals(SPARQLDefinition.SEARCH_CLASS_TYPE_MODE_VALUE.DIRECT)) {
      this.log.warn("Incorect value for kb.search.class.type.mode: {}", getClassTypeMode());
      setClassTypeMode(SPARQLDefinition.SEARCH_CLASS_TYPE_MODE_VALUE.INDIRECT);
      this.log.info("Using default value for kb.search.class.type.mode: ",
              SPARQLDefinition.SEARCH_CLASS_TYPE_MODE_VALUE.INDIRECT.toString());
    }

    // Loading structure
    // Individual paths to definition files are separated by ";"
    final String structureDefinitions = kbProperties.getProperty(STRUCTURE);
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

    validateMandatoryCollection(STRUCTURE_PREDICATE_LABEL);
    validateMandatoryCollection(STRUCTURE_PREDICATE_TYPE);

    setStructureInstanceOf(getOptionalValue(
            kbProperties,
            STRUCTURE_PREDICATE_INSTANCE_OF,
            DEFAULT_STRUCTURE_PREDICATE_INSTANCE_OF));

    setStructureDomain(getOptionalValue(
            kbProperties,
            STRUCTURE_PREDICATE_DOMAIN,
            DEFAULT_STRUCTURE_PREDICATE_DOMAIN));

    setStructureRange(getOptionalValue(
            kbProperties,
            STRUCTURE_PREDICATE_RANGE,
            DEFAULT_STRUCTURE_PREDICATE_RANGE));

    // SPARQL insert
    if (isInsertSupported()) {
      setInsertGraph(getMandatoryValue(kbProperties, INSERT_GRAPH));

      setInsertDefaultClass(getOptionalValue(
              kbProperties,
              INSERT_DEFAULT_CLASS,
              DEFAULT_INSERT_DEFAULT_CLASS));
      setInsertPredicateLabel(getOptionalValue(
              kbProperties,
              INSERT_PREDICATE_LABEL,
              DEFAULT_INSERT_PREDICATE_LABEL));
      setInsertPredicateAlternativeLabel(getOptionalValue(
              kbProperties,
              INSERT_PREDICATE_ALTERNATIVE_LABEL,
              DEFAULT_INSERT_PREDICATE_ALTERNATIVE_LABEL));
      setInsertPredicateSubclassOf(getOptionalValue(
              kbProperties,
              INSERT_PREDICATE_SUBCLASS_OF,
              DEFAULT_INSERT_PREDICATE_SUBCLASS_OF));
      setInsertPredicateSubPropertyOf(getOptionalValue(
              kbProperties,
              INSERT_PREDICATE_SUB_PROPERTY_OF,
              DEFAULT_INSERT_PREDICATE_SUB_PROPERTY_OF));
      setInsertTypeClass(getOptionalValue(
              kbProperties,
              INSERT_TYPE_CLASS,
              DEFAULT_INSERT_TYPE_CLASS));
      setInsertTypeProperty(getOptionalValue(
              kbProperties,
              INSERT_TYPE_PROPERTY,
              DEFAULT_INSERT_TYPE_PROPERTY));
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

  private void setFulltextEnabled(boolean fulltextEnabled) {
    this.fulltextEnabled = fulltextEnabled;
  }

  private void setInsertPredicateAlternativeLabel(final String insertPredicateAlternativeLabel) {
    this.insertPredicateAlternativeLabel = insertPredicateAlternativeLabel;
  }

  private void setInsertTypeClass(final String insertTypeClass) {
    this.insertTypeClass = insertTypeClass;
  }

  private void setInsertGraph(final String insertGraph) {
    this.insertGraph = insertGraph;
  }

  private void setInsertPredicateLabel(final String insertPredicateLabel) {
    this.insertPredicateLabel = insertPredicateLabel;
  }

  private void setInsertTypeProperty(final String insertTypeProperty) {
    this.insertTypeProperty = insertTypeProperty;
  }

  private void setInsertDefaultClass(final String insertDefaultClass) {
    this.insertDefaultClass = insertDefaultClass;
  }

  private void setInsertPredicateSubclassOf(final String insertPredicateSubclassOf) {
    this.insertPredicateSubclassOf = insertPredicateSubclassOf;
  }

  private void setInsertPredicateSubPropertyOf(final String insertPredicateSubPropertyOf) {
    this.insertPredicateSubPropertyOf = insertPredicateSubPropertyOf;
  }

  private void setLanguageSuffix(final String languageSuffix) {
    this.languageSuffix = languageSuffix;
  }

  public void setClassTypeMode(final SPARQLDefinition.SEARCH_CLASS_TYPE_MODE_VALUE classTypeMode) {
    this.classTypeMode = classTypeMode;
  }

  private void setEndpoint(final String endpoint) {
    this.endpoint = endpoint;
  }

  private void setStructureDomain(final String structureDomain) {
    this.structureDomain = structureDomain;
  }

  private void setStructureInstanceOf(final String structureInstanceOf) {
    this.structureInstanceOf = structureInstanceOf;
  }

  private void setStructureRange(final String structureRange) {
    this.structureRange = structureRange;
  }

  private void setUseBifContains(final boolean useBifContains) {
    this.useBifContains = useBifContains;
  }

  private void validateMandatoryCollection(String propertyName) throws KBProxyException {
    if (structure.get(propertyName).size() == 0) {
      throw new KBProxyException("Property " + propertyName + " is mandatory.");
    }
  }

  // endregion
}
