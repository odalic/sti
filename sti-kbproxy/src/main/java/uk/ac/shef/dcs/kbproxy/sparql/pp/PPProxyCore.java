package uk.ac.shef.dcs.kbproxy.sparql.pp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.kbproxy.ProxyException;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.kbproxy.model.PropertyType;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlProxyCore;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.ClassDesc;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.PPRestApiCallException;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.RelationDesc;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.ResourceDesc;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

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



}
