package uk.ac.shef.dcs.kbproxy.utils;

import java.util.Set;

/**
 * URIs utilities.
 * 
 * @author Jan Váňa
 */
public class Uris {
  
  /**
   * This is a little hack for inconsistent http and https in predicate links.
   *
   * @param set the set to search
   * @param value the value to find
   * @return true if the set contains the specified URI without respect to the HTTP protocol version
   */
  public static boolean httpVersionAgnosticContains(final Set<String> set, final String value) {
    if (set.size() == 0) {
      return false;
    }

    return set.contains(value) || set.contains(value.replace("http://", "https://"))
        || set.contains(value.replace("https://", "http://"));
  }
}
