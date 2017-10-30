package uk.ac.shef.dcs.kbproxy.sparql.pp;

import org.apache.http.util.Asserts;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.kbproxy.ProxyDefinition;
import uk.ac.shef.dcs.kbproxy.ProxyException;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.kbproxy.model.PropertyType;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlAttribute;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlProxyCore;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.ClassDesc;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.PPRestApiCallException;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.RelationDesc;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.ResourceDesc;
import uk.ac.shef.dcs.util.Pair;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by tomasknap on 20/12/16.
 */
public class PPProxyCore extends SparqlProxyCore {

    private static final Logger log = LoggerFactory.getLogger(PPProxyCore.class);

    private HttpRequestExecutorForPP queryExecutor;

    PPProxyDefinition ppDefinition;


    /**
     * @param definition   the definition of the knowledge base.
     */
    public PPProxyCore(PPProxyDefinition definition, Map<String, String> prefixToUriMap)  {
        super(definition, prefixToUriMap);

        this.ppDefinition = definition;
        this.queryExecutor = new HttpRequestExecutorForPP(ppDefinition);

    }


//    @Override
//    public  List<Entity> findClassByFulltext(String pattern, int limit)  {
//
//        try {
//            return findByFulltext(() -> createExactMatchQueryForClasses(pattern, limit), () -> createFulltextQueryForClasses(pattern, limit), pattern);
//        }
//        catch (Exception e){
//            // If the search expression causes any error on the KB side, we only log
//            // the exception and return no results. The error is very likely due to
//            // fulltext search requirements defined by the KB.
//            log.error("Unexpected exception during class search.", e);
//            return new ArrayList<>();
//        }
//
//    }

//    @Override
//    public List<Entity> findPredicateByFulltext(String pattern, int limit, URI domain, URI range) {
//        String domainString = domain != null ? domain.toString() : null;
//        String rangeString = range != null ? range.toString() : null;
//
//        try {
//            return findByFulltext(() -> createExactMatchQueryForPredicates(pattern, limit, domainString, rangeString), () -> createFulltextQueryForPredicates(pattern, limit, domainString, rangeString), pattern);
//        }
//        catch (Exception e){
//            // If the search expression causes any error on the KB side, we only log
//            // the exception and return no results. The error is very likely due to
//            // fulltext search requirements defined by the KB.
//            log.error("Unexpected exception during predicate search.", e);
//            return new ArrayList<>();
//        }
//
//    }

    @Override
    protected List<RDFNode> queryReturnSingleNodes(Query query, String columnName, StructureOrDataQueries typeOfQuery) {
        //column name is "SPARQL_VARIABLE_SUBJECT" by default

        List<RDFNode> out = new ArrayList<>();
        if (typeOfQuery.equals(StructureOrDataQueries.STRUCTURE) || typeOfQuery.equals(StructureOrDataQueries.BOTH)) {

            String queryResultString = queryExecutor.getSparqlQueryResult(query);

            JSONParser parser = new JSONParser();
            try {
                JSONObject queryResultJson = (JSONObject) parser.parse(queryResultString);

                JSONArray bindings = (JSONArray) ((JSONObject)queryResultJson.get("results")).get("bindings");
                Iterator<JSONObject> iterator = bindings.iterator();
                while (iterator.hasNext()) {
                    //process bindings:
                    JSONObject o = (JSONObject) iterator.next();
                    JSONObject subject = (JSONObject)o.get("object");
                    String type = (String) subject.get("type");
                    String value = (String) subject.get("value");

                    if (type.equals("uri")) {
                        RDFNode node = new ResourceImpl(value);
                        out.add(node);
                    } else {
                        log.error("Cannot parse output of PP sparql API call, the returned resource {} is not URI resource as depicted by attribute type: {}", value, type );
                    }

                }
            } catch (org.json.simple.parser.ParseException e) {
                log.error("Cannot parse output of PP sparql API call, {}, {}", e.getLocalizedMessage(), e.getStackTrace());
            }
            return out;
        }

        if (typeOfQuery.equals(StructureOrDataQueries.DATA) || typeOfQuery.equals(StructureOrDataQueries.BOTH)) {
            if (out.isEmpty()) {
                //if there was not result using PP API query, try the standard approach for data
                //works, because BOTH mode is used only when querying for resource label, where it is expected to have just one match!
                //so it does not make sense to union the results
                return super.queryReturnSingleNodes(query, columnName, typeOfQuery);
            }
        }


        return out;


//query Exact Match (for classes)
//        SELECT DISTINCT  ?subject
//                WHERE
//        { { SELECT DISTINCT  ?subject
//                WHERE
//            {   { ?subject  foaf:name  "Literacy Composition"@en}
//                UNION
//                { ?subject  dbpprop:fullname  "Literacy Composition"@en}
//                UNION
//                { ?subject  rdfs:label  "Literacy Composition"@en}
//                UNION
//                { ?subject  dbpprop:name  "Literacy Composition"@en}
//                UNION
//                { ?subject  skos:prefLabel  "Literacy Composition"@en}}
//        }
//            { SELECT DISTINCT  ?subject
//                    WHERE
//                {   { ?subject  rdf:type  skos:Concept}
//                    UNION
//                    { ?subject  rdf:type  rdfs:Class}
//                    UNION
//                    { ?subject  rdf:type  owl:Class}}
//            }
//        }
//        LIMIT   10


//        QueryExecution qExec = getQueryExecution(query);
//
//        qExec.setTimeout(queryTimeout, TimeUnit.SECONDS);
//
//        List<RDFNode> out = new ArrayList<>();
//        try {
//            ResultSet rs = qExec.execSelect();
//            while (rs.hasNext()) {
//                QuerySolution qs = rs.next();
//                RDFNode columnNode = qs.get(columnName);
//                out.add(columnNode);
//            }
//
//        } catch(org.apache.jena.query.QueryCancelledException e) {
//            log.info("Timeout reached for query {}", query);
//        } finally {
//            qExec.close();
//        }

    }

    @Override
    protected List<Pair<RDFNode, RDFNode>> queryReturnNodeTuples(Query query, StructureOrDataQueries typeOfQuery ) {

        if (typeOfQuery.equals(StructureOrDataQueries.STRUCTURE)) {

            //TODO execute and fetch results
            List<Pair<RDFNode, RDFNode>> out = new ArrayList<>();

            String queryResultString = queryExecutor.getSparqlQueryResult(query);

            JSONParser parser = new JSONParser();
            try {
                JSONObject queryResultJson = (JSONObject) parser.parse(queryResultString);

                JSONArray bindings = (JSONArray) ((JSONObject) queryResultJson.get("results")).get("bindings");
                Iterator<JSONObject> iterator = bindings.iterator();
                while (iterator.hasNext()) {
                    //process bindings:
                    JSONObject o = (JSONObject) iterator.next();
                    JSONObject subject = (JSONObject) o.get("subject");
                    String subjectType = (String) subject.get("type");
                    String subjectValue = (String) subject.get("value");

                    JSONObject object = (JSONObject) o.get("object");
                    String objectType = (String) subject.get("type");
                    String objectValue = (String) object.get("value");

                    if (subjectType.equals("uri")) {
                        if (objectType.equals("literal")) {
                            RDFNode subjectNode = new ResourceImpl(subjectValue);
                            RDFNode objectNode = new ResourceImpl(objectValue);
                            out.add(new Pair<>(subjectNode, objectNode));
                        } else {
                            log.error("Cannot parse output of PP sparql API call, the returned object {} is not literal as depicted by attribute type: {}", objectValue, objectType);

                        }
                    } else {
                        log.error("Cannot parse output of PP sparql API call, the returned subject {} is not URI resource as depicted by attribute type: {}", subjectValue, subjectType);
                    }
                }
            } catch (org.json.simple.parser.ParseException e) {
                log.error("Cannot parse output of PP sparql API call, {}, {}", e.getLocalizedMessage(), e.getStackTrace());
            }
            return out;
        }
        else if (typeOfQuery.equals(StructureOrDataQueries.DATA)) {
            return super.queryReturnNodeTuples(query, typeOfQuery);
        }
        else {
            log.error("Unsupported mode {} for queryReturnNodeTuples", typeOfQuery);
        }

        return new ArrayList<>();


//        QueryExecution qExec = getQueryExecution(query);
//        qExec.setTimeout(queryTimeout, TimeUnit.SECONDS);
//        List<Pair<RDFNode, RDFNode>> out = new ArrayList<>();
//
//        try {
//            ResultSet rs = qExec.execSelect();
//            while (rs.hasNext()) {
//
//                QuerySolution qs = rs.next();
//                RDFNode subject = qs.get(SPARQL_VARIABLE_SUBJECT);
//                RDFNode object = qs.get(SPARQL_VARIABLE_OBJECT);
//
//                out.add(new Pair<>(subject, object));
//            }
//        } catch(org.apache.jena.query.QueryCancelledException e) {
//            log.info("Timeout reached for query {}", query);
//        } finally {
//            qExec.close();
//        }


    }

//    @Override
//    public List<String> getPropertyDomains(String uri) throws ProxyException{
//        return getPropertyValues(uri, definition.getStructureDomain());
//    }
//
//    @Override
//    public List<String> getPropertyRanges(String uri) throws ProxyException{
//        return getPropertyValues(uri, definition.getStructureRange());
//    }
//
//    private List<String> getPropertyValues(String uri, String propertyUri) throws ProxyException {
//        Asserts.notBlank(uri, "uri");
//        Asserts.notBlank(propertyUri, "propertyUri");
//
//        SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_OBJECT)
//                .addWhere(createSPARQLResource(uri), createSPARQLResource(propertyUri), SPARQL_VARIABLE_OBJECT);
//        return queryReturnSingleValues(builder.build(), SPARQL_VARIABLE_OBJECT);
//
//    }

//    @Override
//    public List<Attribute> findAttributes(String resourceId) throws ProxyException {
//        if (resourceId.length() == 0)
//            return new ArrayList<>();
//
//        List<Attribute> res = new ArrayList<>();
//
//        SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_PREDICATE, SPARQL_VARIABLE_OBJECT)
//                .addWhere(createSPARQLResource(resourceId), SPARQL_VARIABLE_PREDICATE, SPARQL_VARIABLE_OBJECT);
//
//        Query query = builder.build();
//
//
//        //TODO run query, collect results attributes
//
////        QueryExecution qExec = getQueryExecution(query);
////
////        qExec.setTimeout(queryTimeout, TimeUnit.SECONDS);
////
////        try {
////            ResultSet rs = qExec.execSelect();
////            while (rs.hasNext()) {
////                QuerySolution qs = rs.next();
////                RDFNode predicate = qs.get(SPARQL_VARIABLE_PREDICATE);
////                RDFNode object = qs.get(SPARQL_VARIABLE_OBJECT);
////                if (object != null) {
////                    Attribute attr = new SparqlAttribute(predicate.toString(), object.toString());
////                    res.add(attr);
////                }
////            }
////        } catch(org.apache.jena.query.QueryCancelledException e) {
////            log.info("Timeout reached for query {}", query);
////        } finally {
////            qExec.close();
////        }
//
//        return res;
//    }







    /**
     * Inserts class to the ontology and custom schema.
     *
     * See: https://grips.semantic-web.at/pages/editpage.action?pageId=75563973
     *
     *  TODO: For now, not supporting alternative labels! Should we adjust UI to rather support label/comment?
     *
     *  NOTE: Superclass not yet supported (also not in UI) - For now not supported
     *
     *  QUESTION: Should it also create concept? Otherwise not able to use alternative labels!, also PPX is using only concepts!
     *
     * @param uri
     * @param label
     * @param alternativeLabels
     * @param superClass
     * @return
     * @throws ProxyException
     */
    @Override
    public Entity insertClass(URI uri, String label, Collection<String> alternativeLabels, String superClass) throws ProxyException {

        performInsertChecks(label);

        //create uri of the new class
        String url = checkOrGenerateUrl(definition.getInsertPrefixSchema(), uri);

        if (isNullOrEmpty(superClass)){
            superClass = definition.getInsertDefaultClass();
        }

        //prepare new resource description - the entity (in this case class) being created
        ClassDesc classToBeCreated = new ClassDesc(url, label);

        // Add class as well (not just the concept) and
        // add that class also to custom schema at the same time (required)
        try {
            queryExecutor.createClassRequest(classToBeCreated);
        } catch (PPRestApiCallException ex) {
            throw new ProxyException(ex);
        }

        return new Entity(url, label);

    }

    /**
     * Inserts new concept to the taxonomy.
     *
     * See: https://grips.semantic-web.at/pages/editpage.action?pageId=75563973
     *
     * @param uri
     * @param label
     * @param alternativeLabels
     * @param classes
     * @return
     * @throws ProxyException
     */
    @Override
    public Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels, Collection<String> classes) throws ProxyException {

        performInsertChecks(label);

        //TODO? requested URL suffix not supported, new URL is always generated - is that ok? Probably yes, but UI should be adjusted?
        String url = checkOrGenerateUrl(definition.getInsertPrefixData(), uri);

        //prepare new entity description - the entity being created - in this case concept
        ResourceDesc resourceToBeCreatedDesc = new ResourceDesc(label);

        //Step 1: Create concept
        //For the list of API calls, see: https://grips.semantic-web.at/pages/viewpage.action?pageId=75563973
        String urlCreated;
        try {
            urlCreated = queryExecutor.createConceptRequest(resourceToBeCreatedDesc);
            resourceToBeCreatedDesc.setUrl(urlCreated);
        } catch (PPRestApiCallException ex) {
            throw new ProxyException(ex);
        }

        //Step 2: Add alternative labels
        for (String altLabel : alternativeLabels) {
            if (!altLabel.isEmpty()) {
                try {
                    queryExecutor.addLiteralRequest(resourceToBeCreatedDesc, altLabel);
                } catch (PPRestApiCallException ex) {
                    log.error("Cannot add alternative label {} to concept {}, reason: {}", altLabel, resourceToBeCreatedDesc.getUrl(), ex.getStackTrace());
                }
            }
        }

        //Step 3: Apply type to the created concept? http://adequate-project-pp.semantic-web.at/PoolParty/api/?method=applyType
        // so that the concept has a type from the custom schema
        if (classes.size() == 0) {
            log.error("No class for classification");
        }
        else if (classes.size() > 1) {
            log.error("We do not support more than one class for classification");
        }
        else {
            //there is exactly one class
            for (String c : classes) {
                try {
                    queryExecutor.applyTypeRequest(resourceToBeCreatedDesc, c);
                } catch (PPRestApiCallException ex) {
                    log.error("Cannot apply type {} to concept {}, reason: {}", c, resourceToBeCreatedDesc.getUrl(), ex.getStackTrace());
                    log.info("Usually this is the case when you try to apply type which is not a class (e.g. it is a skos:Concept) or it is not available in the KB");
                }
            }
        }

        return new Entity(urlCreated, label);
    }

    /**
     *
     * Inserts predicate to the ontology and custom schema.
     *
     * See: https://grips.semantic-web.at/pages/editpage.action?pageId=75563973
     *
     *
     * http://adequate-project-pp.semantic-web.at/PoolParty/api?method=createDirectedRelation
     *
     * TODO: For now, not supporting alternative labels! Should we change UI to support for relations/classes rather label/comment and not alternative labels?
     * TODO: There is range in UI, but not anyhow used in the call. Plain Literal is produced. Discuss support for types !
     *
     *  QUESTION: Should it also create concept? Otherwise not able to use alternative labels!, also PPX is using only concepts!
     *
     *  NOTE: Sub/super property relation not supported by UI - not implemented for now (also not in UI!)
     *
     * @param uri
     * @param label
     * @param alternativeLabels
     * @param superProperty
     * @param domain
     * @param range
     * @return
     * @throws ProxyException
     */
    @Override
    public Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels, String superProperty, String domain, String range, PropertyType type) throws ProxyException {
        performInsertChecks(label);

        String url = checkOrGenerateUrl(definition.getInsertPrefixSchema(), uri);

        //prepare new entity description - the entity being created
        RelationDesc resourceToBeCreatedDesc = new RelationDesc(url.toString(), label, domain, range, type);

        if (resourceToBeCreatedDesc.getType().equals(PropertyType.Object)) {
            //Step: Add object property
            try {
                queryExecutor.createObjectRelationRequest(resourceToBeCreatedDesc);
            } catch (PPRestApiCallException ex) {
                throw new ProxyException(ex);
            }
        } else {
            //Step: Add data property
            try {
                queryExecutor.createDataTypeRelationRequest(resourceToBeCreatedDesc);
            } catch (PPRestApiCallException ex) {
                throw new ProxyException(ex);
            }
        }

        return new Entity(url, label);
    }


    @Override
    public ProxyDefinition getDefinition() {
        return ppDefinition;
    }


}
