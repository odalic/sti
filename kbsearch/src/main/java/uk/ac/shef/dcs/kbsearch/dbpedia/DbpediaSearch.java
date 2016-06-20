package uk.ac.shef.dcs.kbsearch.dbpedia;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
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
 * Created by Jan on 18.05.2016.
 */
public class DBpediaSearch extends KBSearch {
    private static final Logger LOG = Logger.getLogger(DBpediaSearch.class.getName());
    private static final boolean AUTO_COMMIT = true;

    private DBpediaQueryProxy searcher;

    public DBpediaSearch(Properties properties, Boolean fuzzyKeywords,
                         EmbeddedSolrServer cacheEntity, EmbeddedSolrServer cacheConcept,
                         EmbeddedSolrServer cacheProperty, EmbeddedSolrServer cacheSimilarity) throws IOException {
        super(fuzzyKeywords, cacheEntity, cacheConcept, cacheProperty, cacheSimilarity);

        searcher = new DBpediaQueryProxy();
        otherCache = new HashMap<>();
        resultFilter = new DBpediaSearchResultFilter(properties.getProperty(KB_SEARCH_RESULT_STOPLIST));    }

    @Override
    public List<Entity> findEntityCandidates(String content) throws KBSearchException {
        return find_matchingEntitiesForTextAndType(content);
    }

    @Override
    public List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws KBSearchException {
        return find_matchingEntitiesForTextAndType(content, types);
    }

    @Override
    public List<Attribute> findAttributesOfEntities(Entity ec) throws KBSearchException {
        return find_attributes(ec.getId(), cacheEntity);
    }

    @Override
    public List<Attribute> findAttributesOfProperty(String propertyId) throws KBSearchException {
        return find_attributes(propertyId, cacheProperty);
    }

    @Override
    public List<Attribute> findAttributesOfClazz(String clazzId) throws KBSearchException {
        boolean forceQuery = false;

        List<Attribute> attributes = new ArrayList<>();
        String query = createSolrCacheQuery_findAttributesOfResource(clazzId);
        if (query.length() == 0) return attributes;

        try {
            attributes = (List<Attribute>) cacheConcept.retrieve(query);
            if (attributes != null)
                LOG.debug("QUERY (attributes of clazz, cache load)=" + query + "|" + query);
        } catch (Exception e) {
        }

        if (attributes == null || forceQuery) {
            try {
                attributes = new ArrayList<>();
                List<Attribute> retrievedAttributes = searcher.topicapi_getAttributesOfTopic(clazzId);
                //check firstly, is this a concept?
                boolean isConcept = false;
                for (Attribute f : retrievedAttributes) {
                    if (f.getRelationURI().equals(DBpediaEnum.RELATION_HASTYPE.getString())
                            && f.getValueURI() != null && f.getValueURI().equals(DBpediaEnum.TYPE_TYPE.getString())) {
                        isConcept = true;
                        break;
                    }
                }
                if (!isConcept) {
                    try {
                        cacheConcept.cache(query, attributes, AUTO_COMMIT);
                        LOG.debug("QUERY (attributes of clazz, cache save)=" + query + "|" + query);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return attributes;
                }

                //ok, this is a concept. We need to deep-fetch its properties, and find out the range of their properties
                for (Attribute f : retrievedAttributes) {
                    if (f.getRelationURI().equals(DBpediaEnum.TYPE_PROPERTYOFTYPE.getString())) { //this is a property of a concept, we need to process it further
                        String propertyId = f.getValueURI();
                        if (propertyId == null) continue;

                        List<Attribute> attrOfProperty = findAttributesOfProperty(propertyId);
                        for (Attribute t : attrOfProperty) {
                            if (t.getRelationURI().equals(DBpediaEnum.RELATION_RANGEOFPROPERTY.getString())) {
                                String rangeLabel = t.getValue();
                                String rangeURL = t.getValueURI();
                                Attribute attr = new DBpediaAttribute(f.getValueURI(), rangeLabel);
                                attr.setValueURI(rangeURL);
                                attr.setIsDirect(true);
                                //attributes.add(new String[]{f[2], rangeLabel, rangeURL, "n"});
                            }
                        }
                    } else {
                        attributes.add(f);
                    }
                }

                cacheConcept.cache(query, attributes, AUTO_COMMIT);
                LOG.debug("QUERY (attributes of clazz, cache save)=" + query + "|" + query);
            } catch (Exception e) {
                throw new KBSearchException(e);
            }
        }

        //filtering
        attributes=getResultFilter().filterAttribute(attributes);
        return attributes;
    }

    @Override
    public double findGranularityOfClazz(String clazz) throws KBSearchException {
        /*if(clazz.equals("/location/citytown"))
            System.out.println();*/
        String query = createSolrCacheQuery_findGranularityOfClazz(clazz);
        Double result = null;
        try {
            Object o = cacheConcept.retrieve(query);
            if (o != null) {
                LOG.debug("QUERY (granularity of clazz, cache load)=" + query + "|" + clazz);
                return (Double) o;
            }
        } catch (Exception e) {
        }

        if (result == null) {
            try {
                double granularity = searcher.find_granularityForType(clazz);
                result = granularity;
                try {
                    cacheConcept.cache(query, result, AUTO_COMMIT);
                    LOG.debug("QUERY (granularity of clazz, cache save)=" + query + "|" + clazz);
                } catch (Exception e) {
                    LOG.error("FAILED:" + clazz);
                    e.printStackTrace();
                }
            } catch (IOException ioe) {
                LOG.error("ERROR(Instances of Type): Unable to fetch freebase page of instances of type: " + clazz);
            }
        }
        if (result == null)
            return -1.0;
        return result;
    }

    @Override
    public double findEntityClazzSimilarity(String entity_id, String clazz_url) throws KBSearchException {
        String query = createSolrCacheQuery_findEntityClazzSimilarity(entity_id, clazz_url);
        Object result = null;
        try {
            result = cacheSimilarity.retrieve(query);
            if (result != null)
                LOG.debug("QUERY (entity-clazz similarity, cache load)=" + query + "|" + query);
        } catch (Exception e) {
        }
        if (result == null)
            return -1.0;
        return (Double) result;
    }

    @Override
    public void cacheEntityClazzSimilarity(String entity_id, String clazz_url, double score, boolean biDirectional, boolean commit) throws KBSearchException {
        String query = createSolrCacheQuery_findEntityClazzSimilarity(entity_id, clazz_url);
        try {
            cacheSimilarity.cache(query, score, commit);
            LOG.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
            if (biDirectional) {
                query = clazz_url + "<>" + entity_id;
                cacheSimilarity.cache(query, score, commit);
                LOG.debug("QUERY (entity-clazz similarity, cache saving)=" + query + "|" + query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commitChanges() throws KBSearchException {
        try {
            cacheConcept.commit();
            cacheEntity.commit();
            cacheProperty.commit();
            for (SolrCache cache : otherCache.values())
                cache.commit();
        } catch (Exception e) {
            throw new KBSearchException(e);
        }
    }

    @Override
    public void closeConnection() throws KBSearchException {
        try {
            if (cacheEntity != null)
                cacheEntity.shutdown();
            if (cacheConcept != null)
                cacheConcept.shutdown();
            if (cacheProperty != null)
                cacheProperty.shutdown();
        } catch (Exception e) {
            throw new KBSearchException(e);
        }
    }

    private List<Entity> find_matchingEntitiesForTextAndType(String text, String... types) throws KBSearchException {
        String query = createSolrCacheQuery_findResources(text);
        boolean forceQuery = false;

        text = StringEscapeUtils.unescapeXml(text);
        int bracket = text.indexOf("(");
        if (bracket != -1) {
            text = text.substring(0, bracket).trim();
        }
        if (StringUtils.toAlphaNumericWhitechar(text).trim().length() == 0)
            return new ArrayList<>();


        List<Entity> result = null;

        if (!forceQuery) {
            try {
                result = (List<Entity>) cacheEntity.retrieve(query);
                if (result != null)
                    LOG.debug("QUERY (entities, cache load)=" + query + "|" + query);
            } catch (Exception e) {
            }
        }
        if (result == null) {
            result = new ArrayList<>();
            try {
                //firstly fetch candidate freebase topics. pass 'true' to only keep candidates whose name overlap with the query term
                List<Entity> topics = searcher.searchapi_getTopicsByNameAndType(text, "any", true, 20); //search api does not retrieve complete types, find types for them
                LOG.debug("(FB QUERY =" +topics.size()+" results)");
                for (Entity ec : topics) {
                    //Next get attributes for each topic
                    List<Attribute> attributes = findAttributesOfEntities(ec);
                    ec.setAttributes(attributes);
                    for (Attribute attr : attributes) {
                        if (attr.getRelationURI().equals(DBpediaEnum.RELATION_HASTYPE.getString()) &&
                                attr.isDirect() &&
                                !ec.hasType(attr.getValueURI())) {
                            ec.addType(new Clazz(attr.getValueURI(), attr.getValue()));
                        }
                    }
                }

                if (topics.size() == 0 && fuzzyKeywords) { //does the query has conjunection word? if so, we may need to try again with split queries
                    String[] queries = text.split("\\band\\b");
                    if (queries.length < 2) {
                        queries = text.split("\\bor\\b");
                        if (queries.length < 2) {
                            queries = text.split("/");
                            if (queries.length < 2) {
                                queries = text.split(",");
                            }
                        }
                    }
                    if (queries.length > 1) {
                        for (String q : queries) {
                            q = q.trim();
                            if (q.length() < 1) continue;
                            result.addAll(find_matchingEntitiesForTextAndType(q, types));
                        }
                    }
                }

                result.addAll(topics);
                cacheEntity.cache(query, result, AUTO_COMMIT);
                LOG.debug("QUERY (entities, cache save)=" + query + "|" + query);
            } catch (Exception e) {
                throw new KBSearchException(e);
            }
        }

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

        //filter entity's clazz, and attributes
        String id = "|";
        for (Entity ec : result) {
            id = id + ec.getId() + ",";
            //ec.setTypes(FreebaseSearchResultFilter.filterClazz(ec.getTypes()));
            List<Clazz> filteredTypes = getResultFilter().filterClazz(ec.getTypes());
            ec.clearTypes();
            for (Clazz ft : filteredTypes)
                ec.addType(ft);
        }

        return result;
    }

    private List<Attribute> find_attributes(String id, SolrCache cache) throws KBSearchException {
        if (id.length() == 0)
            return new ArrayList<>();
        boolean forceQuery = false;

        String query = createSolrCacheQuery_findAttributesOfResource(id);
        List<Attribute> result = null;

        try {
            result = (List<Attribute>) cache.retrieve(query);
            if (result != null)
                LOG.debug("QUERY (attributes of id, cache load)=" + query + "|" + query);
        } catch (Exception e) {
        }
        if (result == null || forceQuery) {
            List<Attribute> attributes;
            try {
                attributes = searcher.topicapi_getAttributesOfTopic(id);
            } catch (Exception e) {
                throw new KBSearchException(e);
            }
            result = new ArrayList<>();
            result.addAll(attributes);
            try {
                cache.cache(query, result, AUTO_COMMIT);
                LOG.debug("QUERY (attributes of id, cache save)=" + query + "|" + query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //filtering
        result = getResultFilter().filterAttribute(result);
        return result;
    }
}
