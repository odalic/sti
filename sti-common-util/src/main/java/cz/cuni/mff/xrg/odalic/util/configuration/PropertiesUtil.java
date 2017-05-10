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
    Preconditions.checkNotNull(key);
    Preconditions.checkNotNull(from);
    Preconditions.checkNotNull(to);
    Preconditions.checkArgument(from.containsKey(key));

    to.setProperty(key, from.getProperty(key));
  }

  private PropertiesUtil() {}
}
