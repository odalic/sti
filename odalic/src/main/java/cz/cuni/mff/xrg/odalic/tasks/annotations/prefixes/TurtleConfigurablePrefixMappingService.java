/**
 *
 */
package cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.jena.atlas.lib.Trie;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;
import uk.ac.shef.dcs.util.StringUtils;

/**
 * A {@link PrefixMappingService} implementation using Turtle configuration file to define the
 * mapping.
 *
 * @author Václav Brodec
 *
 */
public final class TurtleConfigurablePrefixMappingService implements PrefixMappingService {

  private static final String BASE_PATH_CONFIGURATION_KEY = "sti.home";
  private static final String MAPPING_LANGUAGE = "TURTLE";
  private static final String PREFIX_MAPPING_PATH_CONFIGURATION_KEY =
      "cz.cuni.mff.xrg.odalic.prefixes";

  private static Map<String, String> readFromConfiguration(
      final PropertiesService configurationService) throws IOException {
    final Properties properties = configurationService.get();

    final String basePath = properties.getProperty(BASE_PATH_CONFIGURATION_KEY);
    if (basePath == null) {
      throw new IllegalArgumentException(
          String.format("Configuration key %s is missing!", BASE_PATH_CONFIGURATION_KEY));
    }

    final String relativePrefixMappingPath =
        properties.getProperty(PREFIX_MAPPING_PATH_CONFIGURATION_KEY);
    if (relativePrefixMappingPath == null) {
      throw new IllegalArgumentException(
          String.format("Configuration key %s is missing!", PREFIX_MAPPING_PATH_CONFIGURATION_KEY));
    }

    final String prefixMappingPath = StringUtils.combinePaths(basePath, relativePrefixMappingPath);

    final Model model = ModelFactory.createDefaultModel();
    try (final FileInputStream configurationStream = new FileInputStream(prefixMappingPath)) {
      model.read(configurationStream, null, MAPPING_LANGUAGE);
    }

    return model.getNsPrefixMap();
  }

  private static Trie<String> toTrie(final Map<String, String> mapping) {
    final Trie<String> trie = new Trie<>();

    mapping.forEach((prefix, uri) -> {
      Preconditions.checkNotNull(prefix);
      Preconditions.checkNotNull(uri);

      trie.add(uri, prefix);
    });

    return trie;
  }

  private final Trie<String> urisToPrefixes;

  private final Map<String, String> prefixesToUris;

  /**
   * Creates the service, using the provided mapping.
   *
   * @param mapping the mapping
   */
  public TurtleConfigurablePrefixMappingService(final Map<String, String> mapping) {
    Preconditions.checkNotNull(mapping);

    this.urisToPrefixes = toTrie(mapping);
    this.prefixesToUris = ImmutableMap.copyOf(mapping);
  }

  /**
   * Creates the service, using mapping based on the configuration files.
   *
   * @throws IOException when I/O exception during mapping configuration happens
   * @throws FileNotFoundException when the mapping describing the mapping could not be loaded
   *
   */
  @Autowired
  public TurtleConfigurablePrefixMappingService(final PropertiesService configurationService)
      throws IOException {
    this(readFromConfiguration(configurationService));
  }

  @Override
  public Prefix getPrefix(final String uri) {
    final String prefix = this.urisToPrefixes.longestMatch(uri);
    if (prefix == null) {
      return null;
    }

    return Prefix.create(prefix, this.prefixesToUris.get(prefix));
  }

  @Override
  public Map<String, String> getPrefixToUriMap() {
    return ImmutableMap.copyOf(this.prefixesToUris);
  }
}
