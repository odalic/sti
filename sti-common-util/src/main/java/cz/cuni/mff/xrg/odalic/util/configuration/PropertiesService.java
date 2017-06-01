package cz.cuni.mff.xrg.odalic.util.configuration;

import java.util.Properties;

/**
 * Provides the application configuration.
 *
 * @author Václav Brodec
 *
 */
public interface PropertiesService {
  /**
   * Provides the main configuration properties.
   *
   * @return the main configuration properties
   */
  Properties get();
}
