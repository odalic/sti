package uk.ac.shef.dcs.kbproxy.sparql;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import uk.ac.shef.dcs.kbproxy.KBDefinition;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by tomasknap on 20/12/16.
 */
public class PPProxy extends SPARQLProxy {

    private OntModel ontology;

    /**
     * @param kbDefinition   the definition of the knowledge base.
     * @param fuzzyKeywords  given a query string, kbproxy will firstly try to fetch results matching the exact query. when no match is
     *                       found, you can set fuzzyKeywords to true, to let kbproxy to break the query string based on conjunective words.
     *                       So if the query string is "tom and jerry", it will try "tom" and "jerry"
     * @param cachesBasePath Base path for the initialized solr caches.
     * @throws IOException
     */
    public PPProxy(KBDefinition kbDefinition, Boolean fuzzyKeywords, String cachesBasePath) throws IOException {
        super(kbDefinition, fuzzyKeywords, cachesBasePath);

        String ontologyURL = kbDefinition.getOntologyUri();
        if (ontologyURL != null) {
            ontology = loadModel(ontologyURL);
        }
        resultFilter = new DBpediaSearchResultFilter(kbDefinition.getStopListFile());
    }


    @Override
    protected List<String> queryForLabel(String sparqlQuery, String resourceURI) throws KBProxyException {
        //TODO temp hack. When fixed by Jan, rely on the SPARQL version

        try {
            org.apache.jena.query.Query query = QueryFactory.create(sparqlQuery);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), query);

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
        } catch (QueryParseException ex) {
            throw new KBProxyException("Invalid query: " + sparqlQuery, ex);
        }


    }

    private OntModel loadModel(String ontURL) {
        OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        base.read(ontURL);
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, base);
    }

    @Override
    public Entity insertClass(URI uri, String label, Collection<String> alternativeLabels, String superClass) throws KBProxyException {

        if (!isInsertSupported()){
            throw new KBProxyException("Insertion of new classes is not supported for the " + kbDefinition.getName() + " knowledge base.");
        }

        if (isNullOrEmpty(label)){
            throw new KBProxyException("Label of the new class must not be empty.");
        }

        String url = checkOrGenerateUrl(kbDefinition.getInsertSchemaElementPrefix(), uri);

        if (isNullOrEmpty(superClass)){
            superClass = kbDefinition.getInsertRootClass();
        }


        // /thesaurus/1DDFF124-EE5B-0001-B0C2-1F8031F51970/createConcept

        //Basic definition - uri + prefLabel
//        StringBuilder tripleDefinition = new StringBuilder("<");
//        tripleDefinition.append(url);
//        tripleDefinition.append("> <");
//        tripleDefinition.append(kbDefinition.getInsertLabel());
//        tripleDefinition.append("> \"");
//        tripleDefinition.append(escapeSPARQLLiteral(label));
//        tripleDefinition.append("\"");

        //TODO alternative labels
        //appendCollection(tripleDefinition, kbDefinition.getInsertAlternativeLabel(), alternativeLabels, true);

        //TODO subclass
        //appendValue(tripleDefinition, kbDefinition.getInsertSubclassOf(), superClass, false);

        //appendValue(tripleDefinition, kbDefinition.getInsertInstanceOf(), kbDefinition.getInsertClassType(), false);
//        String sparqlQuery = String.format(INSERT_BASE, kbDefinition.getInsertGraph(), tripleDefinition);
//        log.info("SPARQL query: \n" + sparqlQuery);

        return new Entity(url, label);

    }

    @Override
    public Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels, Collection<String> classes) throws KBProxyException {
//        if (!isInsertSupported()){
//            throw new KBProxyException("Insertion of new concepts is not supported for the " + kbDefinition.getName() + " knowledge base.");
//        }
//
//        if (isNullOrEmpty(label)){
//            throw new KBProxyException("Label of the new concept must not be empty.");
//        }
//
//        String url = checkOrGenerateUrl(kbDefinition.getInsertDataElementPrefix(), uri);
//
//        StringBuilder tripleDefinition = createTripleDefinitionBase(url, label);
//        appendCollection(tripleDefinition, kbDefinition.getInsertAlternativeLabel(), alternativeLabels, true);
//        boolean typeSpecified = appendCollection(tripleDefinition, kbDefinition.getInsertInstanceOf(), classes, false);
//        if (!typeSpecified){
//            appendValue(tripleDefinition, kbDefinition.getInsertInstanceOf(), kbDefinition.getInsertRootClass(), false);
//        }
//
//        insert(tripleDefinition.toString());
//        return new Entity(url, label);
        return null;
    }


}
