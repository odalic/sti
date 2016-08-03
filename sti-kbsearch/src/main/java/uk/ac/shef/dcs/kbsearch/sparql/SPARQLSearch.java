package uk.ac.shef.dcs.kbsearch.sparql;

import javafx.util.Pair;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.Levenshtein;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.util.SolrCache;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;


/**
 * test queries:
 *
 * SELECT DISTINCT ?s ?o WHERE {
 ?s <http://www.w3.org/2000/01/rdf-schema#label> ?o .
 FILTER ( regex (str(?o), "\\bcat\\b", "i") ) }


 SELECT DISTINCT ?s WHERE {
 ?s <http://www.w3.org/2000/01/rdf-schema#label> "Nature Cat"@en .
 }

 SELECT DISTINCT ?p ?o WHERE {
 wd:Q21043336 ?p ?o .
 }


 */
public abstract class SPARQLSearch extends KBSearch {

  protected String sparqlEndpoint;

    protected StringMetric stringMetric = new Levenshtein();
    /**
     * @param fuzzyKeywords   given a query string, kbsearch will firstly try to fetch results matching the exact query. when no match is
     *                        found, you can set fuzzyKeywords to true, to let kbsearch to break the query string based on conjunective words.
     *                        So if the query string is "tom and jerry", it will try "tom" and "jerry"
     * @param cacheEntity     the solr instance to cache retrieved entities from the kb. pass null if not needed
     * @param cacheConcept    the solr instance to cache retrieved classes from the kb. pass null if not needed
     * @param cacheProperty   the solr instance to cache retrieved properties from the kb. pass null if not needed
     * @param cacheSimilarity the solr instance to cache computed semantic similarity between entity and class. pass null if not needed
     * @throws IOException
     */
    public SPARQLSearch(String sparqlEndpoint, Boolean fuzzyKeywords,
                        EmbeddedSolrServer cacheEntity,
                        EmbeddedSolrServer cacheConcept,
                        EmbeddedSolrServer cacheProperty,
                        EmbeddedSolrServer cacheSimilarity) throws IOException {
        super(fuzzyKeywords, cacheEntity, cacheConcept, cacheProperty, cacheSimilarity);
        this.sparqlEndpoint=sparqlEndpoint;
    }

    protected String createRegexQuery(String content, String... types){
        StringBuilder query = new StringBuilder("SELECT DISTINCT ?s ?o WHERE {").append(
                "?s <").append(RDFEnum.RELATION_HASLABEL.getString()).append("> ?o .").append("\n");

        if(types.length>0){
            query.append("{?s a <").append(types[0]).append(">}\n");
            for(int i=1; i<types.length; i++){
                query.append("UNION { ?s a <").append(types[i]).append(">}\n");
            }
            query.append(".\n");
        }

        query.append("FILTER ( regex (str(?o), \"\\b").append(content).append("\\b\", \"i\") ) }");
        return query.toString();
    }

    protected String createExactMatchQueries(String content){
        String query = "SELECT DISTINCT ?s WHERE {"+
            "?s <"+RDFEnum.RELATION_HASLABEL.getString()+"> \""+content+"\"@en . }";

        return query;
    }

    protected String createExactMatchWithOptionalTypes(String content){
        String query = "SELECT DISTINCT ?s ?o WHERE {"+
                "?s <"+RDFEnum.RELATION_HASLABEL.getString()+"> \""+content+"\"@en . \n"+
                "OPTIONAL {?s a ?o} }";

        return query;
    }
    
    protected String createGetLabelQuery(String content){
        content=content.replaceAll("\\s+","");
        String query = "SELECT DISTINCT ?o WHERE {<"+
                content+"> <"+RDFEnum.RELATION_HASLABEL.getString()+"> ?o . }";

        return query;

    }


    /**
     *
     * @param sparqlQuery
     * @param string
     * @return
     */
    protected List<Pair<String, String>> queryByLabel(String sparqlQuery, String string){
        org.apache.jena.query.Query query = QueryFactory.create(sparqlQuery);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);

        List<Pair<String, String>> out = new ArrayList<>();
        ResultSet rs = qexec.execSelect();
        while(rs.hasNext()){

            QuerySolution qs = rs.next();
            RDFNode subject = qs.get("?s");
            RDFNode object = qs.get("?o");
            out.add(new Pair<>(subject.toString(), object!=null  ? object.toString() : string));
        }
        return out;
    }

    protected abstract List<String> queryForLabel(String sparqlQuery, String resourceURI) throws KBSearchException;


    /**
     * Compares the similarity of the object value of certain resource (entity) and the cell value text (original label).
     * Then, it also sorts the list of candidates based on the scores.
     * @param candidates
     * @param originalQueryLabel
     */
    protected void rank(List<Pair<String, String>> candidates, String originalQueryLabel){
        final Map<Pair<String, String>, Double> scores = new HashMap<>();
        for(Pair<String, String> p : candidates){
            String label = p.getValue();
            double s = stringMetric.compare(label, originalQueryLabel);
            scores.put(p, s);
        }

        Collections.sort(candidates, (o1, o2) -> {
            Double s1 = scores.get(o1);
            Double s2 = scores.get(o2);
            return s2.compareTo(s1);
        });
    }

    @Override
    public List<Entity> findEntityCandidates(String content) throws KBSearchException {
      return findEntityCandidatesOfTypes(content);
    }

    @Override
    public List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws KBSearchException {
      final String sparqlQuery; 
      if (types.length > 0) {
        sparqlQuery =  createExactMatchQueries(content);
      } else {
        sparqlQuery =  createExactMatchWithOptionalTypes(content);
      }
      
      return queryEntityCandidates(content, sparqlQuery, types);
    }

    private List<Entity> queryEntityCandidates(String content, String sparqlQuery, String... types)
        throws KBSearchException {
      String queryCache = createSolrCacheQuery_findResources(content);
  
      content = StringEscapeUtils.unescapeXml(content);
      int bracket = content.indexOf("(");
      if (bracket != -1) {
          content = content.substring(0, bracket).trim();
      }
      if (StringUtils.toAlphaNumericWhitechar(content).trim().length() == 0)
          return new ArrayList<>();
  
  
      List<Entity> result = null;
      if (!ALWAYS_CALL_REMOTE_SEARCHAPI) {
          try {
              result = (List<Entity>) cacheEntity.retrieve(queryCache);
              if (result != null) {
                  log.debug("QUERY (entities, cache load)=" + queryCache + "|" + queryCache);
                  if (types.length > 0) {
                      Iterator<Entity> it = result.iterator();
                      while (it.hasNext()) {
                          Entity ec = it.next();
                          boolean typeSatisfied = false;
                          for (String t : types) {
                              if (ec.hasType(t)) {
                                  typeSatisfied = true;
                                  break;
                              }
                          }
                          if (!typeSatisfied)
                              it.remove();
                      }
                  }
              }
          } catch (Exception e) {
              log.error(e.getLocalizedMessage(),e);
          }
      }
      if (result == null) {
          result = new ArrayList<>();
          try {
              //1. try exact string
              List<Pair<String, String>> resourceAndType = queryByLabel(sparqlQuery, content);
              boolean hasExactMatch = resourceAndType.size() > 0;
              if (types.length > 0) {
                  Iterator<Pair<String, String>> it = resourceAndType.iterator();
                  while (it.hasNext()) {
                      Pair<String, String> ec = it.next();
                      boolean typeSatisfied = false;
                      for (String t : types) {
                          if (t.equals(ec.getValue())) {
                              typeSatisfied = true;
                              break;
                          }
                      }
                      if (!typeSatisfied)
                          it.remove();
                  }
              }//with this query the 'value' of the pair will be the type, now need to reset it to actual value
              List<Pair<String, String>> queryResult = new ArrayList<>();
              if (resourceAndType.size() > 0) {
                  Pair<String, String> matchedResource = resourceAndType.get(0);
                  queryResult.add(new Pair<>(matchedResource.getKey(), content));
              }
  
              //2. if result is empty, try regex
              if (!hasExactMatch && fuzzyKeywords) {
                  log.debug("(query by regex. This can take a long time)");
                  sparqlQuery = createRegexQuery(content, types);
                  queryResult = queryByLabel(sparqlQuery, content);
              }
              //3. rank result by the degree of matches
              rank(queryResult, content);
  
              //firstly fetch candidate freebase topics. pass 'true' to only keep candidates whose name overlap with the query term
              log.debug("(DB QUERY =" + queryResult.size() + " results)");
              for (Pair<String, String> candidate : queryResult) {
                  //Next get attributes for each topic
                  String label = candidate.getValue();
                  if (label == null)
                      label = content;
                  Entity ec = new Entity(candidate.getKey(), label);
                  List<Attribute> attributes = findAttributesOfEntities(ec);
                  ec.setAttributes(attributes);
                  for (Attribute attr : attributes) {
                      adjustValueOfURLResource(attr);
                      if (attr.getRelationURI().endsWith(RDFEnum.RELATION_HASTYPE_SUFFIX_PATTERN.getString()) &&
                              !ec.hasType(attr.getValueURI())) {
                          ec.addType(new Clazz(attr.getValueURI(), attr.getValue()));
                      }
                  }
                  result.add(ec);
              }
  
              cacheEntity.cache(queryCache, result, AUTO_COMMIT);
              log.debug("QUERY (entities, cache save)=" + queryCache + "|" + queryCache);
          } catch (Exception e) {
              throw new KBSearchException(e);
          }
      }
  
      //filter entity's clazz, and attributes
      String id = "|";
      for (Entity ec : result) {
          id = id + ec.getId() + ",";
          //ec.setTypes(FreebaseSearchResultFilter.filterClazz(ec.getTypes()));
          List<Clazz> filteredTypes = resultFilter.filterClazz(ec.getTypes());
          ec.clearTypes();
          for (Clazz ft : filteredTypes)
              ec.addType(ft);
      }
  
      return result;
    }

    private void adjustValueOfURLResource(Attribute attr) throws KBSearchException {
        String value = attr.getValue();
        if (value.startsWith("http")) {
            String queryCache = createSolrCacheQuery_findLabelForResource(value);
    
    
            List<String> result = null;
            if (!ALWAYS_CALL_REMOTE_SEARCHAPI) {
                try {
                    result = (List<String>) cacheEntity.retrieve(queryCache);
                    if (result != null) {
                        log.debug("QUERY (resource labels, cache load)=" + queryCache + "|" + queryCache);
                    }
                } catch (Exception e) {
                }
            }
            if (result == null) {
                try {
                    //1. try exact string
                    String sparqlQuery = createGetLabelQuery(value);
                    result = queryForLabel(sparqlQuery, value);
    
                    cacheEntity.cache(queryCache, result, AUTO_COMMIT);
                    log.debug("QUERY (entities, cache save)=" + queryCache + "|" + queryCache);
                } catch (Exception e) {
                    throw new KBSearchException(e);
                }
            }
    
            if (result.size() > 0) {
                attr.setValueURI(value);
                attr.setValue(result.get(0));
            } else {
                attr.setValueURI(value);
            }
        }
    }

    @Override
    public List<Attribute> findAttributesOfEntities(Entity ec) throws KBSearchException {
        return findAttributes(ec.getId(), cacheEntity);
    }

    private List<Attribute> findAttributes(String id, SolrCache cache) throws KBSearchException {
        if (id.length() == 0)
            return new ArrayList<>();
    
        String queryCache = createSolrCacheQuery_findAttributesOfResource(id);
        List<Attribute> result = null;
        if (!ALWAYS_CALL_REMOTE_SEARCHAPI) {
            try {
                result = (List<Attribute>) cache.retrieve(queryCache);
                if (result != null)
                    log.debug("QUERY (attributes of id, cache load)=" + queryCache + "|" + queryCache);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    
        if (result == null) {
            result = new ArrayList<>();
            String query = "SELECT DISTINCT ?p ?o WHERE {\n" +
                    "<" + id + "> ?p ?o .\n" +
                    "}";
    
            Query sparqlQuery = QueryFactory.create(query);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, sparqlQuery);
    
            ResultSet rs = qexec.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                RDFNode predicate = qs.get("?p");
                RDFNode object = qs.get("?o");
                if (object != null) {
                    Attribute attr = new SPARQLAttribute(predicate.toString(), object.toString());
                    result.add(attr);
                }
            }
    
            try {
                cache.cache(queryCache, result, AUTO_COMMIT);
                log.debug("QUERY (attributes of id, cache save)=" + query + "|" + query);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    
        //filtering
        result = resultFilter.filterAttribute(result);
        return result;
    }

    @Override
    public List<Attribute> findAttributesOfClazz(String clazzId) throws KBSearchException {
        return findAttributes(clazzId, cacheEntity);
    }

    @Override
    public List<Attribute> findAttributesOfProperty(String propertyId) throws KBSearchException {
        return findAttributes(propertyId, cacheEntity);
    }

    protected String createSolrCacheQuery_findLabelForResource(String url) {
        return "LABEL_" + url;
    }
}