/**
 *
 */
package cz.cuni.mff.xrg.odalic.util.configuration;

import java.util.Properties;

import com.google.common.base.Preconditions;

/**
 * Properties utilities.
 * 
 * @author Václav Brodec
 *
 */
public final class PropertiesUtil {

  public static void copyProperty(final String key, final Properties from, final Properties to) {
    Preconditions.checkNotNull(key, "The key cannot be null!");
    Preconditions.checkNotNull(from, "The from cannot be null!");
    Preconditions.checkNotNull(to, "The to cannot be null!");
    Preconditions.checkArgument(from.containsKey(key), String.format("The source properties do not caints the key %s!", key));

    to.setProperty(key, from.getProperty(key));
  }

  private PropertiesUtil() {}
}
