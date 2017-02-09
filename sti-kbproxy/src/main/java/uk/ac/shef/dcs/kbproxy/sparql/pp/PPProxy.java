package uk.ac.shef.dcs.kbproxy.sparql.pp;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.kbproxy.KBDefinition;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.kbproxy.sparql.DBpediaSearchResultFilter;
import uk.ac.shef.dcs.kbproxy.sparql.SPARQLProxy;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

/**
 * Created by tomasknap on 20/12/16.
 */
public class PPProxy extends SPARQLProxy {

    private static final Logger log = LoggerFactory.getLogger(PPProxy.class);

    private OntModel ontology;

    private HttpRequestExecutorForPP helper;

    /**
     * @param kbDefinition   the definition of the knowledge base.
     * @param fuzzyKeywords  given a query string, kbproxy will firstly try to fetch results matching the exact query. when no match is
     *                       found, you can set fuzzyKeywords to true, to let kbproxy to break the query string based on conjunective words.
     *                       So if the query string is "tom and jerry", it will try "tom" and "jerry"
     * @param cachesBasePath Base path for the initialized solr caches.
     * @throws IOException
     */
    public PPProxy(KBDefinition kbDefinition, Boolean fuzzyKeywords, String cachesBasePath) throws IOException, KBProxyException {
        super(kbDefinition, fuzzyKeywords, cachesBasePath);

        this.helper = new HttpRequestExecutorForPP();

        String ontologyURL = kbDefinition.getOntologyUri();
        if (ontologyURL != null) {
            ontology = loadModel(ontologyURL);
        }
        resultFilter = new DBpediaSearchResultFilter(kbDefinition.getStopListFile());
    }

    private OntModel loadModel(String ontURL) {
        OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        base.read(ontURL);
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, base);
    }

    @Override
    public Entity insertClass(URI uri, String label, Collection<String> alternativeLabels, String superClass) throws KBProxyException {

        //It creates class in the ontology
        //TODO Should it also create concept? Otherwise not able to use alternative labels, also PPX is using only concepts

        performInsertChecks(label);

        String url = checkOrGenerateUrl(kbDefinition.getInsertSchemaElementPrefix(), uri);

        if (isNullOrEmpty(superClass)){
            superClass = kbDefinition.getInsertRootClass();
        }

        //prepare new entity description - the entity being created
        ResourceDesc resourceToBeCreatedDesc = new ResourceDesc(url, label);


        //Step: Add class as well (not just the concept) and add that class also to custom schema at the same time (required)
        try {
            //TODO Add subclass superclass relation if available
            helper.createClassRequest(resourceToBeCreatedDesc);
        } catch (PPRestApiCallException ex) {
            throw new KBProxyException(ex);
        }

        return new Entity(url, label);

    }

    @Override
    public Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels, Collection<String> classes) throws KBProxyException {

        performInsertChecks(label);

        //String url = checkOrGenerateUrl(kbDefinition.getInsertDataElementPrefix(), uri);

        //prepare new entity description - the entity being created
        ResourceDesc resourceToBeCreatedDesc = new ResourceDesc(label);

        //Step 1: Create concept
        //For the list of API calls, see: https://grips.semantic-web.at/pages/viewpage.action?pageId=75563973
        //TODO requested URL suffix not supported? - So new URL for the concept is used. - is that ok?
        String urlCreated;
        try {
            urlCreated = helper.createConceptRequest(resourceToBeCreatedDesc);
            resourceToBeCreatedDesc.setConceptUrl(urlCreated);
        } catch (PPRestApiCallException ex) {
            throw new KBProxyException(ex);
        }

        //Step 2: Add alternative labels
        for (String altLabel : alternativeLabels) {
            if (!altLabel.isEmpty()) {
                try {
                    helper.addLiteralRequest(resourceToBeCreatedDesc, altLabel);
                } catch (PPRestApiCallException ex) {
                    log.error("Cannot add alternative label {} to concept {}, reason: {}", altLabel, resourceToBeCreatedDesc.getConceptUrl(), ex.getStackTrace());
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
                    helper.applyTypeRequest(resourceToBeCreatedDesc, c);
                } catch (PPRestApiCallException ex) {
                    log.error("Cannot apply type {} to concept {}, reason: {}", c, resourceToBeCreatedDesc.getConceptUrl(), ex.getStackTrace());
                }
            }
        }

        return new Entity(urlCreated, label);
    }

    @Override
    public Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels, String superProperty, String domain, String range) throws KBProxyException {
        performInsertChecks(label);

        String url = checkOrGenerateUrl(kbDefinition.getInsertSchemaElementPrefix(), uri);

        //TODO properties

//        String url = checkOrGenerateUrl(kbDefinition.getInsertSchemaElementPrefix(), uri);
//
//        StringBuilder tripleDefinition = createTripleDefinitionBase(url, label);
//        appendCollection(tripleDefinition, kbDefinition.getInsertAlternativeLabel(), alternativeLabels, true);
//        appendValue(tripleDefinition, kbDefinition.getInsertInstanceOf(), kbDefinition.getInsertPropertyType(), false);
//
//        appendValueIfNotEmpty(tripleDefinition, kbDefinition.getInsertSubProperty(), superProperty, false);
//        appendValueIfNotEmpty(tripleDefinition, kbDefinition.getInsertDomain(), domain, false);
//        appendValueIfNotEmpty(tripleDefinition, kbDefinition.getInsertRange(), range, false);
//
//        insert(tripleDefinition.toString());
        return new Entity(url, label);
    }



}
