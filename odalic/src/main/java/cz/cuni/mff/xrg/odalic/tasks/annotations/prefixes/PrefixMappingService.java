package cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes;

import java.util.Map;

/**
 * Service that assigns prefixes to URIs.
 * 
 * @author Václav Brodec
 *
 */
public interface PrefixMappingService {
  /**
   * Tries to apply existing prefixes for the URI.
   * 
   * <p>
   * A {@link Prefix} is applied when its {@link Prefix#getWhat()} part is an actual prefix of the
   * provided input text.
   * </p>
   * 
   * <p>
   * When none is applicable, returns {@code null}.
   * </p>
   * 
   * @param uri Text to look for the prefixed part in
   * @return The associated prefix
   */
  Prefix getPrefix(String uri);

  /**
   * The map from prefixes to URIs.
   * @return
   */
  Map<String, String> getPrefixToUriMap();
}
