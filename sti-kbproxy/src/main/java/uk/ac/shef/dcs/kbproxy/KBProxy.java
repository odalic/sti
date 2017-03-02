package uk.ac.shef.dcs.kbproxy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.util.SolrCache;

public abstract class KBProxy {

  protected interface Func<Type> {
    Type Do() throws Exception;
  }

  private static final String CACHE_VERSION_ID = "9274dff6-c606-4f5d-8bb5-d528c764e655";

  private static final String CACHE_VERSION = "1.0.18";
  private static final Map<String, CoreContainer> cacheCores = new HashMap<>();
  private static final String ENTITY_CACHE = "entity";
  private static final String PROPERTY_CACHE = "property";

  private static final String CONCEPT_CACHE = "concept";
  private static final String SIMILARITY_CACHE = "similarity";
  protected static final boolean AUTO_COMMIT = true;

  protected static final boolean ALWAYS_CALL_REMOTE_SEARCH_API = false;
  protected SolrCache cacheEntity;
  protected SolrCache cacheConcept;
  protected SolrCache cacheProperty;

  protected SolrCache cacheSimilarity;

  private final String cachesBasePath;

  protected KBSearchResultFilter resultFilter;

  protected final Logger log = LoggerFactory.getLogger(getClass());

  protected Map<String, String> prefixToUriMap;

  /**
   * @param cachesBasePath Base path for the initialized solr caches.
   * @param prefixToUriMap Map of user defined prefixes.
   */
  public KBProxy(final String cachesBasePath, final Map<String, String> prefixToUriMap)
      throws IOException, URISyntaxException, KBProxyException {

    this.cachesBasePath = cachesBasePath;
    this.prefixToUriMap = ImmutableMap.copyOf(prefixToUriMap);
  }

  /**
   * save the computed semantic similarity between the entity and class
   */
  public void cacheEntityClazzSimilarity(final String entity_id, final String clazz_url,
      final double score, final boolean biDirectional, final boolean commit) {
    String query = createSolrCacheQuery_findEntityClazzSimilarity(entity_id, clazz_url);
    try {
      this.cacheSimilarity.cache(query, score, commit);
      this.log.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
      if (biDirectional) {
        query = clazz_url + "<>" + entity_id;
        this.cacheSimilarity.cache(query, score, commit);
        this.log.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
      }
    } catch (final Exception e) {
      this.log.error(e.getLocalizedMessage(), e);
    }
  }

  void cacheValue(final String queryCache, final Object value, final SolrCache cache) {
    try {
      this.log.debug("QUERY (" + cache.getServer().getCoreContainer().getSolrHome()
          + ", cache save)=" + queryCache);
      cache.cache(queryCache, value, AUTO_COMMIT);
    } catch (final Exception ex) {
      this.log.error("Error saving resource to the cache.", ex);
    }
  }

  public void closeConnection() throws KBProxyException {

    try {
      if (this.cacheEntity != null) {
        this.cacheEntity.shutdown();
      }
      if (this.cacheConcept != null) {
        this.cacheConcept.shutdown();
      }
      if (this.cacheProperty != null) {
        this.cacheProperty.shutdown();
      }
      if (this.cacheSimilarity != null) {
        this.cacheSimilarity.shutdown();
      }
    } catch (final Exception e) {
      throw new KBProxyException(e);
    }
  }

  public void commitChanges() throws KBProxyException {

    try {
      if (this.cacheConcept != null) {
        this.cacheConcept.commit();
      }
      if (this.cacheEntity != null) {
        this.cacheEntity.commit();
      }
      if (this.cacheProperty != null) {
        this.cacheProperty.commit();
      }
      if (this.cacheSimilarity != null) {
        this.cacheSimilarity.commit();
      }
    } catch (final Exception e) {
      throw new KBProxyException(e);
    }
  }

  protected String createSolrCacheQuery_findAttributesOfResource(final String resource) {
    return "ATTR_" + resource;
  }

  protected String createSolrCacheQuery_findEntityClazzSimilarity(final String entity,
      final String concept) {
    return entity + "<>" + concept;
  }

  protected String createSolrCacheQuery_findGranularityOfClazz(final String clazz) {
    return "GRANULARITY_" + clazz;
  }

  protected String createSolrCacheQuery_findResources(final String content, final String... types) {
    final StringBuilder builder = new StringBuilder("FIND_RESOURCE_");
    builder.append(content);

    for (final String type : Arrays.stream(types).sorted().collect(Collectors.toList())) {
      builder.append("_TYPE_");
      builder.append(type);
    }

    return builder.toString();
  }

  protected String createSolrCacheQuery_getPropertyValues(final String uri,
      final String propertyUri) {
    return "GET_PROPERTY_VALUES_" + uri + "_" + propertyUri;
  }

  protected String createSolrCacheQuery_loadResource(final String uri) {
    return "LOAD_RESOURCE_" + uri;
  }

  private <ResultType> KBProxyResult<ResultType> Do(final Func<ResultType> func,
      final ResultType defaultValue) {
    try {
      final ResultType result = func.Do();
      return new KBProxyResult<>(result);
    } catch (final Exception ex) {
      this.log.error(ex.getLocalizedMessage(), ex);
      return new KBProxyResult<>(defaultValue, ex.getLocalizedMessage());
    }
  }

  /**
   * get attributes of the class
   */
  public KBProxyResult<List<Attribute>> findAttributesOfClazz(final String clazzId) {
    return Do(() -> findAttributesOfClazzInternal(clazzId), new ArrayList<Attribute>());
  }

  protected abstract List<Attribute> findAttributesOfClazzInternal(String clazzId)
      throws KBProxyException;

  /**
   * Get attributes of the entity candidate (all predicates and object values of the triples where
   * the candidate entity is the subject).
   *
   * Note: Certain predicates may be blacklisted.
   */
  public KBProxyResult<List<Attribute>> findAttributesOfEntities(final Entity ec) {
    return Do(() -> findAttributesOfEntitiesInternal(ec), new ArrayList<Attribute>());
  }

  protected abstract List<Attribute> findAttributesOfEntitiesInternal(Entity ec)
      throws KBProxyException;

  /**
   * get attributes of the property
   */
  public KBProxyResult<List<Attribute>> findAttributesOfProperty(final String propertyId) {
    return Do(() -> findAttributesOfPropertyInternal(propertyId), new ArrayList<Attribute>());
  }

  protected abstract List<Attribute> findAttributesOfPropertyInternal(String propertyId)
      throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (classes) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  public abstract List<Entity> findClassByFulltext(String pattern, int limit)
      throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB Candidate entities are those
   * resources for which label or part of the label matches the given content
   *
   * @param content
   * @return
   */
  public KBProxyResult<List<Entity>> findEntityCandidates(final String content) {
    return Do(() -> findEntityCandidatesInternal(content), new ArrayList<Entity>());
  }

  protected abstract List<Entity> findEntityCandidatesInternal(String content)
      throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB that only match certain types
   */
  public KBProxyResult<List<Entity>> findEntityCandidatesOfTypes(final String content,
      final String... types) {
    return Do(() -> findEntityCandidatesOfTypesInternal(content, types), new ArrayList<Entity>());
  }

  protected abstract List<Entity> findEntityCandidatesOfTypesInternal(String content,
      String... types) throws KBProxyException;

  /**
   * compute the seamntic similarity between an entity and a class
   */
  public KBProxyResult<Double> findEntityClazzSimilarity(final String entity_id,
      final String clazz_url) {
    return Do(() -> findEntityClazzSimilarityInternal(entity_id, clazz_url), 0.0);
  }

  protected double findEntityClazzSimilarityInternal(final String entity_id, final String clazz_url)
      throws KBProxyException {
    return 0;
  }

  /**
   * @return the granularity of the class in the KB.
   */
  public KBProxyResult<Double> findGranularityOfClazz(final String clazz) {
    return Do(() -> findGranularityOfClazzInternal(clazz), 0.0);
  }

  protected double findGranularityOfClazzInternal(final String clazz) throws KBProxyException {
    return 0;
  }

  /**
   * Given a string, fetch candidate entities (predicates) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  public abstract List<Entity> findPredicateByFulltext(String pattern, int limit, URI domain,
      URI range) throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB based on a fulltext search.
   *
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  public abstract List<Entity> findResourceByFulltext(String pattern, int limit)
      throws KBProxyException;

  public abstract KBDefinition getKbDefinition();

  public String getName() {
    return getKbDefinition().getName();
  }


  /**
   * Fetches domain of the gives resource.
   *
   * @param uri
   * @return
   * @throws KBProxyException
   */
  public abstract List<String> getPropertyDomains(String uri) throws KBProxyException;


  // TODO the properties below should be moved to a different class (SolrCacheHelper?) and renamed
  // properly
  /*
   * createSolrCacheQuery_XXX defines how a solr query should be constructed. If your implementing
   * class want to benefit from solr cache, you should call these methods to generate a query
   * string, which will be considered as the id of a record in the solr index. that query will be
   * performed, to attempt to retrieve previously saved results if any.
   *
   * If there are no previously cached results, you have to perform your remote call to the KB,
   * obtain the results, then cache the results in solr. Again you should call these methods to
   * create a query string, which should be passed as the id of the record to be added to solr
   */

  /**
   * Fetches range of the gives resource.
   *
   * @param uri
   * @return
   * @throws KBProxyException
   */
  public abstract List<String> getPropertyRanges(String uri) throws KBProxyException;

  public EmbeddedSolrServer getSolrServer(final String cacheIdentifier) throws KBProxyException {
    final Path cachePath = Paths.get(this.cachesBasePath, getKbDefinition().getName());
    EmbeddedSolrServer cacheServer;

    if (!cacheCores.containsKey(cachePath.toString())) {
      cacheServer = initializeSolrServer(cacheIdentifier, cachePath,
          getKbDefinition().getCacheTemplatePath());
      cacheCores.put(cachePath.toString(), cacheServer.getCoreContainer());
    } else {
      cacheServer = new EmbeddedSolrServer(cacheCores.get(cachePath.toString()), cacheIdentifier);
    }

    return cacheServer;
  }

  public void initializeCaches() throws KBProxyException {
    this.cacheEntity = new SolrCache(getSolrServer(ENTITY_CACHE));
    this.cacheProperty = new SolrCache(getSolrServer(PROPERTY_CACHE));
    this.cacheConcept = new SolrCache(getSolrServer(CONCEPT_CACHE));
    this.cacheSimilarity = new SolrCache(getSolrServer(SIMILARITY_CACHE));
  }

  private EmbeddedSolrServer initializeSolrServer(final String cacheIdentifier,
      final Path cachePath, final String templatePathString) throws KBProxyException {
    if (!Files.exists(cachePath)) {
      final Path templatePath = Paths.get(templatePathString);
      if (!Files.exists(templatePath)) {
        final String error =
            "Cannot proceed: the cache dir is not set or does not exist: " + templatePathString;
        this.log.error(error);
        throw new KBProxyException(error);
      }

      try {
        FileUtils.copyDirectory(templatePath.toFile(), cachePath.toFile());
      } catch (final IOException exception) {
        final String error = "Cannot proceed: the cache template cannot be copied. source: "
            + templatePath + "target: " + cachePath;

        this.log.error(error);
        throw new KBProxyException(error, exception);
      }
    }

    final EmbeddedSolrServer server = new EmbeddedSolrServer(cachePath, cacheIdentifier);
    verifyServerVersion(server);
    return server;
  }

  /**
   * Inserts a new class into the knowledge base
   */
  public abstract Entity insertClass(URI uri, String label, Collection<String> alternativeLabels,
      String superClass) throws KBProxyException;

  /**
   * Inserts a new concept into the knowledge base
   */
  public abstract Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels,
      Collection<String> classes) throws KBProxyException;

  /**
   * Inserts a new property into the knowledge base
   */
  public abstract Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels,
      String superProperty, String domain, String range) throws KBProxyException;

  /**
   * Information about whether the knowledge base supports inserting new concepts
   */
  public abstract boolean isInsertSupported();

  private boolean isNullOrEmpty(final Object obj) {
    if (obj == null) {
      return true;
    }

    if (obj instanceof String) {
      final String objString = (String) obj;
      return objString.isEmpty();
    }

    if (obj instanceof List<?>) {
      final List<?> objList = (List<?>) obj;
      return objList.isEmpty();
    }

    return false;
  }

  /**
   * Loads the entity from the knowledge base.
   *
   * @param uri The entity uri.
   * @return The entity or null if no such uri was found in the knowledge base.
   */
  public KBProxyResult<Entity> loadEntity(final String uri) {
    return Do(() -> loadEntityInternal(uri), null);
  }

  protected abstract Entity loadEntityInternal(String uri) throws KBProxyException;

  @SuppressWarnings("unchecked")
  protected <T> T retrieveCachedValue(final String queryCache, final SolrCache cache) {
    T result = null;
    if (!ALWAYS_CALL_REMOTE_SEARCH_API) {
      // if cache is not disabled, try to examine the cache first
      try {
        this.log.debug("QUERY (" + cache.getServer().getCoreContainer().getSolrHome()
            + ", cache load)=" + queryCache);

        result = (T) cache.retrieve(queryCache);
      } catch (final Exception ex) {
        this.log.error("Error fetching resource from the cache.", ex);
      }
    }

    return result;
  }

  protected <ResultType> ResultType retrieveOrTryExecute(final String queryCache,
      final SolrCache cache, final Func<ResultType> func) throws KBProxyException {
    ResultType result = retrieveCachedValue(queryCache, cache);

    if (isNullOrEmpty(result)) {
      try {
        result = func.Do();

        if (!isNullOrEmpty(result)) {
          cacheValue(queryCache, result, cache);
        }
      } catch (final Exception ex) {
        throw new KBProxyException("Unexpected error during KB access.", ex);
      }
    }

    return result;
  }

  private void verifyServerVersion(final EmbeddedSolrServer server) throws KBProxyException {
    try {
      final SolrCache cache = new SolrCache(server);
      final String cacheVersion = (String) cache.retrieve(CACHE_VERSION_ID);
      if (!CACHE_VERSION.equals(cacheVersion)) {
        server.deleteByQuery("*:*");
        cache.cache(CACHE_VERSION_ID, CACHE_VERSION, true);
      }
    } catch (SolrServerException | IOException | ClassNotFoundException e) {
      final String error = "Error initializing the cache.";
      this.log.error(error, e);
      throw new KBProxyException(error, e);
    }
  }
}
