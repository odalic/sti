package cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes;

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
   * @param text text to look for the prefixed part in
   * @return the associated prefix
   */
  Prefix getPrefix(String uri);
}
