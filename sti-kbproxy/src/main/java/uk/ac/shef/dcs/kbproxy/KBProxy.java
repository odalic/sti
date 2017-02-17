package uk.ac.shef.dcs.kbproxy;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.util.SolrCache;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 */
public abstract class KBProxy {

  private static final String CACHE_VERSION_ID = "9274dff6-c606-4f5d-8bb5-d528c764e655";
  private static final String CACHE_VERSION = "1.0.17";

  protected SolrCache cacheEntity;
  protected SolrCache cacheConcept;
  protected SolrCache cacheProperty;
  protected SolrCache cacheSimilarity;
  protected boolean fuzzyKeywords;
  private String cachesBasePath;
  private static final Map<String, CoreContainer> cacheCores = new HashMap<>();

  protected static final String KB_SEARCH_RESULT_STOP_LIST = "kb.search.result.stoplistfile";
  protected static final String KB_SEARCH_CLASS = "kb.search.class";
  protected static final String KB_SEARCH_TRY_FUZZY_KEYWORD = "kb.search.tryfuzzykeyword";

  private static final String ENTITY_CACHE = "entity";
  private static final String PROPERTY_CACHE = "property";
  private static final String CONCEPT_CACHE = "concept";
  private static final String SIMILARITY_CACHE = "similarity";

  protected static final boolean AUTO_COMMIT = true;
  protected static final boolean ALWAYS_CALL_REMOTE_SEARCH_API = false;

  protected KBSearchResultFilter resultFilter;

  protected final Logger log = LoggerFactory.getLogger(getClass());

  protected KBDefinition kbDefinition;
  protected Map<String, String> prefixToUriMap;

  /**
   * @param kbDefinition    the knowledge base definition
   * @param fuzzyKeywords   given a query string, kbproxy will firstly try to fetch results
   *                        matching the exact query. when no match is found, you can set
   *                        fuzzyKeywords to true, to let kbproxy to break the query string based
   *                        on conjunective words. So if the query string is "tom and jerry", it
   *                        will try "tom" and "jerry"
   * @param cachesBasePath  Base path for the initialized solr caches.
   */
  public KBProxy(KBDefinition kbDefinition,
                 Boolean fuzzyKeywords,
                 String cachesBasePath,
                 Map<String, String> prefixToUriMap) throws IOException {

    this.kbDefinition = kbDefinition;
    this.cachesBasePath = cachesBasePath;
    this.fuzzyKeywords = fuzzyKeywords;
    this.prefixToUriMap = ImmutableMap.copyOf(prefixToUriMap);
  }

  public void initializeCaches() throws KBProxyException {
    cacheEntity = new SolrCache(getSolrServer(ENTITY_CACHE));
    cacheProperty = new SolrCache(getSolrServer(PROPERTY_CACHE));
    cacheConcept = new SolrCache(getSolrServer(CONCEPT_CACHE));
    cacheSimilarity = new SolrCache(getSolrServer(SIMILARITY_CACHE));
  }

  public String getName() {
    return kbDefinition.getName();
  }

  public KBDefinition getKbDefinition() { return kbDefinition; }

  public EmbeddedSolrServer getSolrServer(String cacheIdentifier) throws KBProxyException {
    Path cachePath = Paths.get(cachesBasePath, kbDefinition.getName());
    EmbeddedSolrServer cacheServer;

    if (!cacheCores.containsKey(cachePath.toString())) {
      cacheServer = initializeSolrServer(cacheIdentifier, cachePath, kbDefinition.getCacheTemplatePath());
      cacheCores.put(cachePath.toString(), cacheServer.getCoreContainer());
    }
    else {
      cacheServer = new EmbeddedSolrServer(cacheCores.get(cachePath.toString()), cacheIdentifier);
    }

    return cacheServer;
  }

  /**
   * Fetches domain of the gives resource.
   * @param uri
   * @return
   * @throws KBProxyException
   */
  public abstract List<String> getPropertyDomains(String uri) throws KBProxyException;

  /**
   * Fetches range of the gives resource.
   * @param uri
   * @return
   * @throws KBProxyException
   */
  public abstract List<String> getPropertyRanges(String uri) throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB based on a fulltext search.
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  public abstract List<Entity> findResourceByFulltext(String pattern, int limit) throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (classes) from the KB based on a fulltext search.
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  public abstract List<Entity> findClassByFulltext(String pattern, int limit) throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (predicates) from the KB based on a fulltext search.
   * @param pattern
   * @param limit
   * @return
   * @throws IOException
   */
  public abstract List<Entity> findPredicateByFulltext(String pattern, int limit, URI domain, URI range) throws KBProxyException;

  /**
   * Given a string, fetch candidate entities (resources) from the KB
   * Candidate entities are those resources for which label or part of the label matches the given content
   * @param content
   * @return
   */
  public KBProxyResult<List<Entity>> findEntityCandidates(String content) {
    return Do(() -> findEntityCandidatesInternal(content), new ArrayList<Entity>());
  }

  protected abstract List<Entity> findEntityCandidatesInternal(String content) throws KBProxyException;

  /**
   * Given a string,  fetch candidate entities (resources) from the KB that only match certain types
   */
  public KBProxyResult<List<Entity>> findEntityCandidatesOfTypes(String content, String... types) {
    return Do(() -> findEntityCandidatesOfTypesInternal(content, types), new ArrayList<Entity>());
  }

  protected abstract List<Entity> findEntityCandidatesOfTypesInternal(String content, String... types) throws KBProxyException;

  /**
   * Loads the entity from the knowledge base.
   * @param uri The entity uri.
   * @return The entity or null if no such uri was found in the knowledge base.
   */
  public KBProxyResult<Entity> loadEntity(String uri) {
    return Do(() -> loadEntityInternal(uri), null);
  }

  protected abstract Entity loadEntityInternal(String uri) throws KBProxyException;

  /**
   * Get attributes of the entity candidate
   * (all predicates and object values of the triples where the candidate entity is the subject).
   *
   * Note: Certain predicates may be blacklisted.
   */
  public KBProxyResult<List<Attribute>> findAttributesOfEntities(Entity ec) {
    return Do(() -> findAttributesOfEntitiesInternal(ec), new ArrayList<Attribute>());
  }

  protected abstract List<Attribute> findAttributesOfEntitiesInternal(Entity ec) throws KBProxyException;

  /**
   * get attributes of the class
   */
  public KBProxyResult<List<Attribute>> findAttributesOfClazz(String clazzId) {
    return Do(() -> findAttributesOfClazzInternal(clazzId), new ArrayList<Attribute>());
  }

  protected abstract List<Attribute> findAttributesOfClazzInternal(String clazzId) throws KBProxyException;

  /**
   * get attributes of the property
   */
  public KBProxyResult<List<Attribute>> findAttributesOfProperty(String propertyId) {
    return Do(() -> findAttributesOfPropertyInternal(propertyId), new ArrayList<Attribute>());
  }

  protected abstract List<Attribute> findAttributesOfPropertyInternal(String propertyId) throws KBProxyException;

  /**
   * @return the granularity of the class in the KB.
   */
  public KBProxyResult<Double> findGranularityOfClazz(String clazz) {
    return Do(() -> findGranularityOfClazzInternal(clazz), 0.0);
  }

  protected double findGranularityOfClazzInternal(String clazz) throws KBProxyException {
    return 0;
  }

  /**
   * compute the seamntic similarity between an entity and a class
   */
  public KBProxyResult<Double> findEntityClazzSimilarity(String entity_id, String clazz_url) {
    return Do(() -> findEntityClazzSimilarityInternal(entity_id, clazz_url), 0.0);
  }

  protected double findEntityClazzSimilarityInternal(String entity_id, String clazz_url) throws KBProxyException {
    return 0;
  }

  /**
   * Information about whether the knowledge base supports inserting new concepts
   */
  public abstract boolean isInsertSupported();

  /**
   * Inserts a new class into the knowledge base
   */
  public abstract Entity insertClass(URI uri, String label, Collection<String> alternativeLabels, String superClass) throws KBProxyException;

  /**
   * Inserts a new concept into the knowledge base
   */
  public abstract Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels, Collection<String> classes) throws KBProxyException;

  /**
   * Inserts a new propety into the knowledge base
   */
  public abstract Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels, String superProperty, String domain, String range) throws KBProxyException;

  /**
   * save the computed semantic similarity between the entity and class
   */
  public void cacheEntityClazzSimilarity(String entity_id, String clazz_url, double score, boolean biDirectional,
                                         boolean commit) {
    String query = createSolrCacheQuery_findEntityClazzSimilarity(entity_id, clazz_url);
    try {
      cacheSimilarity.cache(query, score, commit);
      log.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
      if (biDirectional) {
        query = clazz_url + "<>" + entity_id;
        cacheSimilarity.cache(query, score, commit);
        log.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
      }
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
    }
  }

  public void commitChanges() throws KBProxyException {

    try {
      if (cacheConcept != null) {
        cacheConcept.commit();
      }
      if (cacheEntity != null) {
        cacheEntity.commit();
      }
      if (cacheProperty != null) {
        cacheProperty.commit();
      }
      if (cacheSimilarity != null) {
        cacheSimilarity.commit();
      }
    } catch (Exception e) {
      throw new KBProxyException(e);
    }
  }


  public void closeConnection() throws KBProxyException {

    try {
      if (cacheEntity != null) {
        cacheEntity.shutdown();
      }
      if (cacheConcept != null) {
        cacheConcept.shutdown();
      }
      if (cacheProperty != null) {
        cacheProperty.shutdown();
      }
      if (cacheSimilarity != null) {
        cacheSimilarity.shutdown();
      }
    } catch (Exception e) {
      throw new KBProxyException(e);
    }
  }


  //TODO the properties below should be moved to a different class (SolrCacheHelper?) and renamed properly
    /*
    createSolrCacheQuery_XXX defines how a solr query should be constructed. If your implementing class
     want to benefit from solr cache, you should call these methods to generate a query string, which will
     be considered as the id of a record in the solr index. that query will be performed, to attempt to retrieve
     previously saved results if any.

     If there are no previously cached results, you have to perform your remote call to the KB, obtain the results,
     then cache the results in solr. Again you should call these methods to create a query string, which should be
     passed as the id of the record to be added to solr
     */

  @SuppressWarnings("unchecked")
  protected <T> T retrieveCachedValue(String queryCache, SolrCache cache) {
    T result = null;
    if (!ALWAYS_CALL_REMOTE_SEARCH_API) {
      //if cache is not disabled, try to examine the cache first
      try {
        log.debug("QUERY (" + cache.getServer().getCoreContainer().getSolrHome() + ", cache load)=" + queryCache);

        result = (T) cache.retrieve(queryCache);
      } catch (Exception ex) {
        log.error("Error fetching resource from the cache.", ex);
      }
    }

    return result;
  }

  void cacheValue(String queryCache, Object value, SolrCache cache) {
    try {
      log.debug("QUERY (" + cache.getServer().getCoreContainer().getSolrHome() + ", cache save)=" + queryCache);
      cache.cache(queryCache, value, AUTO_COMMIT);
    }
    catch (Exception ex) {
      log.error("Error saving resource to the cache.", ex);
    }
  }

  protected String createSolrCacheQuery_findResources(String content, String... types) {
    StringBuilder builder = new StringBuilder("FIND_RESOURCE_");
    builder.append(content);

    for(String type : Arrays.stream(types).sorted().collect(Collectors.toList())) {
      builder.append("_TYPE_");
      builder.append(type);
    }

    return builder.toString();
  }

  protected String createSolrCacheQuery_getPropertyValues(String uri, String propertyUri) {
    return "GET_PROPERTY_VALUES_" + uri + "_" + propertyUri;
  }

  protected String createSolrCacheQuery_loadResource(String uri) {
    return "LOAD_RESOURCE_" + uri;
  }

  protected String createSolrCacheQuery_findAttributesOfResource(String resource) {
    return "ATTR_" + resource;
  }

  protected String createSolrCacheQuery_findGranularityOfClazz(String clazz) {
    return "GRANULARITY_" + clazz;
  }

  protected String createSolrCacheQuery_findEntityClazzSimilarity(String entity, String concept) {
    return entity + "<>" + concept;
  }

  private EmbeddedSolrServer initializeSolrServer(String cacheIdentifier, Path cachePath, String templatePathString) throws KBProxyException {
    if (!Files.exists(cachePath)) {
      Path templatePath = Paths.get(templatePathString);
      if (!Files.exists(templatePath)) {
        String error = "Cannot proceed: the cache dir is not set or does not exist: "
                + templatePathString;
        log.error(error);
        throw new KBProxyException(error);
      }

      try {
        FileUtils.copyDirectory(templatePath.toFile(), cachePath.toFile());
      }
      catch (IOException exception) {
        String error = "Cannot proceed: the cache template cannot be copied. source: "
                + templatePath
                + "target: "
                + cachePath;

        log.error(error);
        throw new KBProxyException(error, exception);
      }
    }

    EmbeddedSolrServer server = new EmbeddedSolrServer(cachePath, cacheIdentifier);
    verifyServerVersion(server);
    return server;
  }

  private void verifyServerVersion(EmbeddedSolrServer server) throws KBProxyException {
    try {
      SolrCache cache = new SolrCache(server);
      String cacheVersion = (String)cache.retrieve(CACHE_VERSION_ID);
      if (!CACHE_VERSION.equals(cacheVersion)) {
        server.deleteByQuery("*:*");
        cache.cache(CACHE_VERSION_ID, CACHE_VERSION, true);
      }
    } catch (SolrServerException | IOException | ClassNotFoundException e) {
      String error = "Error initializing the cache.";
      log.error(error, e);
      throw new KBProxyException(error, e);
    }
  }

  private <ResultType> KBProxyResult<ResultType> Do(Func<ResultType> func, ResultType defaultValue) {
    try {
      ResultType result = func.Do();
      return new KBProxyResult<>(result);
    }
    catch (Exception ex) {
      log.error(ex.getLocalizedMessage(), ex);
      return new KBProxyResult<>(defaultValue, ex.getLocalizedMessage());
    }
  }

  protected <ResultType> ResultType retrieveOrTryExecute(String queryCache, SolrCache cache, Func<ResultType> func) throws KBProxyException {
    ResultType result = retrieveCachedValue(queryCache, cache);

    if (isNullOrEmpty(result)) {
      try {
        result = func.Do();

        if (!isNullOrEmpty(result)) {
          cacheValue(queryCache, result, cache);
        }
      }
      catch (Exception ex) {
        throw new KBProxyException("Unexpected error during KB access.", ex);
      }
    }

    return result;
  }

  private boolean isNullOrEmpty(Object obj) {
    if (obj == null) {
      return true;
    }

    if (obj instanceof String) {
      String objString = (String)obj;
      return objString.isEmpty();
    }

    if (obj instanceof List<?>) {
      List<?> objList = (List<?>)obj;
      return objList.isEmpty();
    }

    return false;
  }

  protected interface Func<Type> {
    Type Do() throws Exception;
  }
}
