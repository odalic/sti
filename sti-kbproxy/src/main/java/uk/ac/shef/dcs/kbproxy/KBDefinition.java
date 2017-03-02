package uk.ac.shef.dcs.kbproxy;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Information about the knowledge base. Created by Jan
 */
public class KBDefinition {

  // region Consts
  private static final String NAME_PROPERTY_NAME = "kb.name";
  private static final String KB_SEARCH_CLASS = "kb.class";
  private static final String CACHE_TEMPLATE_PATH_PROPERTY_NAME = "kb.cacheTemplatePath";
  private static final String STOP_LIST_FILE_PROPERTY_NAME = "kb.stopListFile";
  private static final String INSERT_SUPPORTED = "kb.insert.supported";

  private static final String DEFAULT_INSERT_SUPPORTED = "false";
  private static final String INSERT_PREFIX_DATA = "kb.insert.prefix.data";
  private static final String INSERT_PREFIX_SCHEMA = "kb.insert.prefix.schema";

  //endregion

  // region Fields

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private String name;
  private String stopListFile;
  private String cacheTemplatePath;

  private boolean insertSupported;
  private URI insertPrefixData;
  private URI insertPrefixSchema;

  // endregion

  // region Properties and Methods

  public String getCacheTemplatePath() {
    return this.cacheTemplatePath;
  }

  public URI getInsertPrefixData() {
    return this.insertPrefixData;
  }

  public URI getInsertPrefixSchema() {
    return this.insertPrefixSchema;
  }

  public static String getKBClass(Properties properties) throws KBProxyException {
    return getMandatoryValue(properties, KB_SEARCH_CLASS);
  }

  /**
   * Returns the name of the knowledge base
   *
   * @return
   */
  public String getName() {
    return this.name;
  }

  public String getStopListFile() {
    return this.stopListFile;
  }

  public boolean isInsertSupported() {
    return this.insertSupported;
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
  public void load(
          final Properties kbProperties,
          final String workingDirectory,
          final String cacheDirectory)
          throws IOException, URISyntaxException, KBProxyException {
    // Name
    setName(getMandatoryValue(kbProperties,NAME_PROPERTY_NAME));

    // Stoplist definition
    setStopListFile(combinePaths(workingDirectory,
            getMandatoryValue(kbProperties, STOP_LIST_FILE_PROPERTY_NAME)));

    // Cache template
    setCacheTemplatePath(combinePaths(cacheDirectory,
            getMandatoryValue(kbProperties, CACHE_TEMPLATE_PATH_PROPERTY_NAME)));

    // SPARQL insert
    setInsertSupported(Boolean.parseBoolean(
            getOptionalValue(kbProperties, INSERT_SUPPORTED, DEFAULT_INSERT_SUPPORTED)));

    if (isInsertSupported()) {
      setInsertPrefixData(new URI(getMandatoryValue(kbProperties, INSERT_PREFIX_DATA)));
      setInsertPrefixSchema(new URI(getMandatoryValue(kbProperties, INSERT_PREFIX_SCHEMA)));
    }
  }

  protected static String getMandatoryValue(
          Properties properties,
          final String propertyName) throws KBProxyException {
    if (!properties.containsKey(propertyName)) {
      throw new KBProxyException("Property " + propertyName + " is mandatory.");
    }

    return properties.getProperty(propertyName);
  }

  protected String getOptionalValue(
          Properties properties,
          final String propertyName,
          final String defaultValue) {
    if (!properties.containsKey(propertyName)) {
      log.warn("Configuration does not contain the " + propertyName + " setting. Setting to the default \"" + defaultValue + "\".");
      return defaultValue;
    }

    return properties.getProperty(propertyName);
  }

  private void setCacheTemplatePath(final String cacheTemplatePath) {
    this.cacheTemplatePath = cacheTemplatePath;
  }

  private void setInsertPrefixData(final URI insertPrefixData) {
    this.insertPrefixData = insertPrefixData;
  }

  private void setInsertPrefixSchema(final URI insertPrefixSchema) {
    this.insertPrefixSchema = insertPrefixSchema;
  }

  private void setInsertSupported(final boolean insertSupported) {
    this.insertSupported = insertSupported;
  }

  private void setName(final String name) {
    this.name = name;
  }

  private void setStopListFile(final String stopListFile) {
    this.stopListFile = stopListFile;
  }

  // endregion
}
