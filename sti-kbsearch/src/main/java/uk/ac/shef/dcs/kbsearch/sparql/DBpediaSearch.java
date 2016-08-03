package uk.ac.shef.dcs.kbsearch.sparql;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by - on 10/06/2016.
 */
public class DBpediaSearch extends SPARQLSearch {

    private static final String DBP_SPARQL_ENDPOINT = "dbp.sparql.endpoint";
    private static final String DBP_ONTOLOGY_URL = "dbp.ontology.url";

    private OntModel ontology;

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
    public DBpediaSearch(Properties properties,
                         Boolean fuzzyKeywords,
                         EmbeddedSolrServer cacheEntity,
                         EmbeddedSolrServer cacheConcept,
                         EmbeddedSolrServer cacheProperty,
                         EmbeddedSolrServer cacheSimilarity) throws IOException {
        super(properties.getProperty(DBP_SPARQL_ENDPOINT), fuzzyKeywords, cacheEntity, cacheConcept, cacheProperty, cacheSimilarity);
        String ontURL = properties.getProperty(DBP_ONTOLOGY_URL);
        if (ontURL != null)
            ontology = loadModel(ontURL);
        resultFilter = new DBpediaSearchResultFilter(properties.getProperty(KB_SEARCH_RESULT_STOPLIST));
    }

    private OntModel loadModel(String ontURL) {
        OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        base.read(ontURL);
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, base);
    }

    

//    @Override
//    public double findGranularityOfClazz(String clazz) throws KBSearchException {
//        if (ontology == null)
//            throw new KBSearchException("Not supported");
//        return 0;
//    }

//    @Override
//    public double findEntityClazzSimilarity(String entity_id, String clazz_url) throws KBSearchException {
//        if (ontology == null)
//            throw new KBSearchException("Not supported");
//        return 0;
//    }


    @Override
    protected List<String> queryForLabel(String sparqlQuery, String resourceURI) throws KBSearchException {
        try {
            org.apache.jena.query.Query query = QueryFactory.create(sparqlQuery);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);

            List<String> out = new ArrayList<>();
            ResultSet rs = qexec.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                RDFNode domain = qs.get("?o");
                String d = null;
                if (domain != null)
                    d = domain.toString();
                if (d != null) {
                    if (d.contains("@")) { //language tag in dbpedia literals
                        if (!d.endsWith("@en"))
                            continue;
                        else {
                            int trim = d.lastIndexOf("@en");
                            if (trim != -1)
                                d = d.substring(0, trim).trim();
                        }
                    }

                }
                out.add(d);
            }

            if (out.size() == 0) { //the resource has no statement with prop "rdfs:label", apply heuristics to parse the
                //resource uri
                int trim = resourceURI.lastIndexOf("#");
                if (trim == -1)
                    trim = resourceURI.lastIndexOf("/");
                if (trim != -1) {
                    String stringValue = resourceURI.substring(trim + 1).replaceAll("[^a-zA-Z0-9]", "").trim();
                    if (resourceURI.contains("yago")) { //this is an yago resource, which may have numbered ids as suffix
                        //e.g., City015467
                        int end = 0;
                        for (int i = 0; i < stringValue.length(); i++) {
                            if (Character.isDigit(stringValue.charAt(i))) {
                                end = i;
                                break;
                            }
                        }
                        if (end > 0)
                            stringValue = stringValue.substring(0, end);
                    }
                    stringValue = StringUtils.splitCamelCase(stringValue);
                    out.add(stringValue);
                }
            }
            return out;
        }
        catch (QueryParseException ex) {
            throw new KBSearchException("Invalid query: " + sparqlQuery, ex);
        }
    }
}