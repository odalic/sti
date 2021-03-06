/**
 *
 */
package cz.cuni.mff.xrg.odalic.util.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.google.common.base.Preconditions;

/**
 * Loads the configuration defined by a system property.
 *
 * @author Václav Brodec
 *
 */
public class DefaultPropertiesService implements PropertiesService {

  public static final String CONFIGURATION_PATH_SYSTEM_PROPERTY_KEY = "cz.cuni.mff.xrg.odalic.sti";

  private static Properties loadFromPath(final String configurationPath)
      throws IOException, FileNotFoundException {
    final Properties properties = new Properties();

    try (final FileInputStream configurationStream = new FileInputStream(configurationPath)) {
      properties.load(new FileInputStream(configurationPath));
    }

    return properties;
  }

  private static String readConfigurationPath(final String configurationPathSystemPropertyKey) {
    final String configurationPath = System.getProperty(configurationPathSystemPropertyKey);
    if (configurationPath == null) {
      throw new IllegalArgumentException(
          String.format("System property %s not provided!", configurationPathSystemPropertyKey));
    }
    return configurationPath;
  }

  private final Properties properties;

  public DefaultPropertiesService() throws FileNotFoundException, IOException {
    this(CONFIGURATION_PATH_SYSTEM_PROPERTY_KEY);
  }

  public DefaultPropertiesService(final Properties properties) {
    Preconditions.checkNotNull(properties, "The properties cannot be null!");

    this.properties = properties;
  }

  public DefaultPropertiesService(final String configurationPathSystemPropertyKey)
      throws FileNotFoundException, IOException {
    this(loadFromPath(readConfigurationPath(configurationPathSystemPropertyKey)));
  }

  @Override
  public Properties get() {
    return this.properties;
  }
}
