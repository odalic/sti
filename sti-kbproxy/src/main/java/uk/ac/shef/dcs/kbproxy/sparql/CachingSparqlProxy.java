package uk.ac.shef.dcs.kbproxy.sparql;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.util.Asserts;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.Levenshtein;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.BasicKnowledgeBaseProxy;
import uk.ac.shef.dcs.kbproxy.Configurable;
import uk.ac.shef.dcs.kbproxy.KBProxyUtils;
import uk.ac.shef.dcs.kbproxy.KnowledgeBaseInterface;
import uk.ac.shef.dcs.kbproxy.BasicKnowledgeBaseProxy.Func;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.kbproxy.solr.KbProxySolr;
import uk.ac.shef.dcs.util.Cache;
import uk.ac.shef.dcs.util.Pair;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CachingSparqlProxy implements KnowledgeBaseInterface {

  private static final Logger log = LoggerFactory.getLogger(CachingSparqlProxy.class);
  
  private final SPARQLProxy proxy;

  private final Cache cache;
  
  public CachingSparqlProxy(final SPARQLProxy proxy, final Cache cache) {
    Preconditions.checkNotNull(proxy);
    Preconditions.checkNotNull(cache);
    
    this.proxy = proxy;
    this.cache = cache;
  }
  
  @SuppressWarnings("unchecked")
  private <T> T retrieveCachedValue(final String queryCache) {
    try {
      log.debug("QUERY (" + this.cache + ", cache load)=" + queryCache);

      return (T) cache.retrieve(queryCache);
    } catch (final Exception ex) {
      log.error("Error fetching resource from the cache.", ex);
      
      return null;
    }
  }
  
  private <T> T retrieveOrTryExecute(final String queryCache, final Supplier<T> regularSupplier) throws KBProxyException {
    T result = retrieveCachedValue(queryCache);

    if (isNullOrEmpty(result)) {
      try {
        result = regularSupplier.get();

        if (!isNullOrEmpty(result)) {
          cacheValue(queryCache, result);
        }
      } catch (final Exception ex) {
        throw new KBProxyException("Unexpected error during KB access.", ex);
      }
    }

    return result;
  }
  
  private void cacheValue(final String queryCache, final Object value) {
    try {
      log.debug("QUERY (" + cache
          + ", cache save)=" + queryCache);
      cache.cache(queryCache, value, true);
    } catch (final Exception ex) {
      log.error("Error saving resource to the cache.", ex);
    }
  }
  
  private static boolean isNullOrEmpty(final Object obj) {
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
  
  @Override
  public List<String> getPropertyDomains(String uri) throws KBProxyException{
    final String cacheQuery = KbProxySolr.createSolrCacheQuery_getPropertyValues(uri, this.proxy.getKbDefinition().getStructureDomain());
    
    return retrieveOrTryExecute(cacheQuery, () -> { return this.proxy.getPropertyDomains(uri); });
  }

  @Override
  public List<String> getPropertyRanges(String uri) throws KBProxyException{
    final String cacheQuery = KbProxySolr.createSolrCacheQuery_getPropertyValues(uri, this.proxy.getKbDefinition().getStructureRange()());
    
    return retrieveOrTryExecute(cacheQuery, () -> { return this.proxy.getPropertyRanges(uri); });
  }

  @Override
  protected Entity loadEntityInternal(String uri) throws KBProxyException {
    //prepare cache
    String queryCache = KbProxySolr.createSolrCacheQuery_loadResource(uri);
    Entity result = retrieveOrTryExecute(queryCache, () -> {
      //there is nothing suitable in the cache or the cache is disabled
      AskBuilder builder = getAskBuilder().addWhere(createSPARQLResource(uri), createSPARQLResource(kbDefinition.getStructureInstanceOf()), "?Type");
      boolean askResult = ask(builder.build());

      if (!askResult) {
        return null;
      }

      String label = getResourceLabel(uri);
      Entity res = new Entity(uri, label);
      loadEntityAttributes(res);

      return res;
    });

    filterEntityTypes(result);
    return result;
  }

  private List<Entity> queryEntityCandidates(final String content, String... types)
      throws KBProxyException {

    //prepare cache
    String queryCache = KbProxySolr.createSolrCacheQuery_findResources(content, types);

    List<Entity> result = retrieveOrTryExecute(queryCache, () -> {
      // adjust the content string before query is executed
      String unescapedContent = StringEscapeUtils.unescapeXml(content);
      int bracket = unescapedContent.indexOf("(");
      if (bracket != -1) {
        unescapedContent = unescapedContent.substring(0, bracket).trim();
      }
      if (StringUtils.toAlphaNumericWhitechar(unescapedContent).trim().length() == 0)
        return new ArrayList<Entity>();

      List<Entity> res = new ArrayList<>();
      List<Pair<String, String>> queryResult = new ArrayList<>();

      //1. try exact string
      // prepare the query
      Query sparqlQuery = createExactMatchQueryForResources(unescapedContent,null, false, types);
      List<String> resourceAndType = queryReturnSingleValues(sparqlQuery);
      for(String resource : resourceAndType) {
        queryResult.add(new Pair<>(resource, unescapedContent)); //I may add content because it is exact match
      }

      //2. if result is empty, try regex (if allowed)
      //the same for type/non-type restrictions ??
      if (resourceAndType.size() == 0 && kbDefinition.isFulltextEnabled()) {
        log.debug("(query by regex. This can take a long time)");
        sparqlQuery = createFulltextQueryForResources(unescapedContent, null, false,  types);
        queryResult = queryReturnTuples(sparqlQuery, unescapedContent);
      }

      //3. rank result by the degree of matches.
      rank(queryResult, unescapedContent);

      //get all attributes for the candidates, set also types (based on the predicates which may contain type)
      log.debug("(DB QUERY =" + queryResult.size() + " results)");
      for (Pair<String, String> candidate : queryResult) {
        //Next get attributes for each topic
        String label = candidate.getValue();
        if (label == null)
          label = unescapedContent;
        Entity ec = new Entity(candidate.getKey(), label);
        loadEntityAttributes(ec);
        res.add(ec);
      }

      return res;
    });

    //filter entity's clazz, and attributes
    for (Entity ec : result) {
      filterEntityTypes(ec);
    }

    return result;
  }

  private String getResourceLabel(String uri) throws KBProxyException {
    if (uri.startsWith("http")) {
      String queryCache = createSolrCacheQuery_findLabelForResource(uri);

      List<String> result = retrieveOrTryExecute(queryCache, () -> {
        Query sparqlQuery = createGetLabelQuery(uri);
        return queryForLabel(sparqlQuery, uri);
      });

      if (result.size() > 0) {
        return result.get(0);
      }
    }

    return uri;
  }

 private List<Attribute> findAttributes(String id) throws KBProxyException {
    if (id.length() == 0)
      return new ArrayList<>();

    String queryCache = KbProxySolr.createSolrCacheQuery_findAttributesOfResource(id);
    List<Attribute> result = retrieveOrTryExecute(queryCache, () -> {
      List<Attribute> res = new ArrayList<>();

      SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_PREDICATE, SPARQL_VARIABLE_OBJECT)
              .addWhere(createSPARQLResource(id), SPARQL_VARIABLE_PREDICATE, SPARQL_VARIABLE_OBJECT);

      Query query = builder.build();
      log.info("SPARQL query: \n" + query.toString());

      QueryExecution qExec = QueryExecutionFactory.sparqlService(kbDefinition.getEndpoint(), query);

      ResultSet rs = qExec.execSelect();
      while (rs.hasNext()) {
        QuerySolution qs = rs.next();
        RDFNode predicate = qs.get(SPARQL_VARIABLE_PREDICATE);
        RDFNode object = qs.get(SPARQL_VARIABLE_OBJECT);
        if (object != null) {
          Attribute attr = new SPARQLAttribute(predicate.toString(), object.toString());
          res.add(attr);
        }
      }

      return res;
    });

    //filtering
    result = resultFilter.filterAttribute(result);
    return result;
  }
}
