package uk.ac.shef.dcs.kbproxy.sparql.pp;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.ClassDesc;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.ResourceDesc;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.PPRestApiCallException;
import uk.ac.shef.dcs.kbproxy.sparql.pp.helpers.RelationDesc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by tomasknap on 09/02/17.
 */
public class HttpRequestExecutorForPP {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestExecutorForPP.class);

    private DefaultHttpClient client;

    private PPProxyDefinition ppDefinition;

    public HttpRequestExecutorForPP(PPProxyDefinition ppDefinition) {

        //this.requestExecutor = new HttpRequestExecutorForPP();
        //this.client = HttpClients.custom().disableContentCompression().build();
        client = new DefaultHttpClient();
        client.setRedirectStrategy(new LaxRedirectStrategy());
        this.ppDefinition = ppDefinition;
    }

    /**
     * Gets
     */
    public String getSparqlQueryResult(String query) throws PPRestApiCallException {

        CloseableHttpResponse response = null;
        try {

            URIBuilder uriBuilder = new URIBuilder(ppDefinition.getPpServerUrl() + "/api/schema/sparql/select");
            String queryToBeExecuted = query.toString();
            uriBuilder.addParameter("query", queryToBeExecuted);

            HttpGet request = new HttpGet(uriBuilder.build().normalize());
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");

            addBasiAuthenticationForHttpRequest(request, ppDefinition.getLogin(), ppDefinition.getPassword());

            response = client.execute(request);

            //process response
            checkHttpResponseStatus(response);


            //get response
            HttpEntity responseHttpEntity = response.getEntity();
            InputStream content = responseHttpEntity.getContent();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String line;
            String responseString = "";

            while ((line = buffer.readLine()) != null) {
                responseString += line;
            }

            try {
                //release all resources held by the responseHttpEntity
                EntityUtils.consume(responseHttpEntity);

                //close the stream
                response.close();
            } finally {
                response.close();
            }

            LOG.info("Adding literal, response {}", responseString);
            return responseString;

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP raw POST request to URL %s", "xx");
            LOG.error(errorMsg);
            throw new PPRestApiCallException(errorMsg, ex);
        }
    }


    /**
     * Creates concept
     *
     * http://adequate-project-pp.semantic-web.at/PoolParty/api?method=createConcept
     *
     * @return HTTP response - in this case URL of the created resource
     * @throws Exception
     *             if request execution fails
     */
    public String createConceptRequest(ResourceDesc createdEntityDesc) throws PPRestApiCallException {
        CloseableHttpResponse response = null;
        try {

            URIBuilder uriBuilder = new URIBuilder(ppDefinition.getPpServerUrl() + "/api/thesaurus/" + ppDefinition.getPpProjectId() + "/createConcept");

            uriBuilder.addParameter("parent",ppDefinition.getPpConceptSchemaProposed());

            uriBuilder.addParameter("prefLabel",createdEntityDesc.getLabel());

            HttpPost request = new HttpPost(uriBuilder.build().normalize());
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");

            addBasiAuthenticationForHttpRequest(request, ppDefinition.getLogin(), ppDefinition.getPassword());

            response = client.execute(request);

            //process response
            checkHttpResponseStatus(response);

            //get response
            HttpEntity responseHttpEntity = response.getEntity();
            InputStream content = responseHttpEntity.getContent();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String line;
            String urlCreated = "";

            while ((line = buffer.readLine()) != null) {
                urlCreated += line;
            }

            urlCreated = urlCreated.replaceAll("\"","");
            LOG.info("URL of the created concept is: {}", urlCreated);

            //check the validatity of URL
            try {
                URL url = new URL(urlCreated);
            } catch (MalformedURLException e) {
                throw new PPRestApiCallException("Cannot parse the URL created and returned by PP ", e);
            }

            try {
                //release all resources held by the responseHttpEntity
                EntityUtils.consume(responseHttpEntity);

                //close the stream
                response.close();
            } finally {
                response.close();
            }

            return urlCreated;

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP raw POST request to URL %s", "xx");
            LOG.error(errorMsg);
            throw new PPRestApiCallException(errorMsg, ex);
        }
    }


    /**
     * Add literal
     *
     * http://<server-url>/PoolParty/api/thesaurus/APItests/addLiteral?concept=http://test.info/glossary/2614&label=New label&language=en&property=alternativeLabel
     *
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public String addLiteralRequest(ResourceDesc createdEntityDesc, String altLabel) throws PPRestApiCallException {
        CloseableHttpResponse response = null;
        try {

            URIBuilder uriBuilder = new URIBuilder(ppDefinition.getPpServerUrl() + "/api/thesaurus/" + ppDefinition.getPpProjectId() + "/addLiteral");
            uriBuilder.addParameter("concept",createdEntityDesc.getUrl());
            uriBuilder.addParameter("label",  altLabel);
            uriBuilder.addParameter("property","alternativeLabel");

            HttpPost request = new HttpPost(uriBuilder.build().normalize());
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");

            addBasiAuthenticationForHttpRequest(request, ppDefinition.getLogin(), ppDefinition.getPassword());

            response = client.execute(request);

            //process response
            checkHttpResponseStatus(response);

            //get response
            HttpEntity responseHttpEntity = response.getEntity();
            InputStream content = responseHttpEntity.getContent();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String line;
            String responseString = "";

            while ((line = buffer.readLine()) != null) {
                responseString += line;
            }

            try {
                //release all resources held by the responseHttpEntity
                EntityUtils.consume(responseHttpEntity);

                //close the stream
                response.close();
            } finally {
                response.close();
            }

            LOG.info("Adding literal, response {}", responseString);
            return responseString;

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP raw POST request to URL %s", "xx");
            LOG.error(errorMsg);
            throw new PPRestApiCallException(errorMsg, ex);
        }
    }


    /**
     * Apply type request
     *
     * http://<server-url>/PoolParty/api/thesaurus/APItests/applyType?resource=http://test.info/glossary/2614&type=http://vocabulary.poolparty.biz/PoolParty/schema/Geonames/Continent
     *
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public String applyTypeRequest(ResourceDesc createdEntityDesc, String targetClass) throws PPRestApiCallException {
        CloseableHttpResponse response = null;
        try {

            URIBuilder uriBuilder = new URIBuilder(ppDefinition.getPpServerUrl() + "/api/thesaurus/" + ppDefinition.getPpProjectId() + "/applyType");
            uriBuilder.addParameter("resource",createdEntityDesc.getUrl());
            uriBuilder.addParameter("type", targetClass);
            uriBuilder.addParameter("propagate","false");

            HttpPost request = new HttpPost(uriBuilder.build().normalize());
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");

            addBasiAuthenticationForHttpRequest(request, ppDefinition.getLogin(), ppDefinition.getPassword());

            response = client.execute(request);

            //process response
            checkHttpResponseStatus(response);

            //get response
            HttpEntity responseHttpEntity = response.getEntity();
            InputStream content = responseHttpEntity.getContent();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String line;
            String responseString = "";

            while ((line = buffer.readLine()) != null) {
                responseString += line;
            }

            try {
                //release all resources held by the responseHttpEntity
                EntityUtils.consume(responseHttpEntity);

                //close the stream
                response.close();
            } finally {
                response.close();
            }

            LOG.info("Applying type, response {}", responseString);
            return responseString;

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP raw POST request to URL %s", "xx");
            LOG.error(errorMsg);
            throw new PPRestApiCallException(errorMsg, ex);
        }
    }



    /**
     * Creates class
     *
     * http://adequate-project-pp.semantic-web.at/PoolParty/!/api?method=createClass
     *
     * Sample:
     * {
     "schemaUri": {
     "uri": "http://localhost:8080/some"
     },
     "uri": {
     "uri": "http://localhost:8080/some/sf"
     },
     "label": {
     "label" : "Test"
     },
     "addToCustomScheme" : [
     {
     "uri" : "http://localhost:8080/testdf"
     }]
     }
     *
     *
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public String createClassRequest(ClassDesc createdEntityDesc) throws PPRestApiCallException {
        CloseableHttpResponse response = null;
        try {

            JSONObject json = new JSONObject();

            JSONObject schemaUri = new JSONObject();
            schemaUri.put("uri", ppDefinition.getPpOntologyUrl());
            json.put("schemaUri", schemaUri);

            JSONObject targetUri = new JSONObject();
            targetUri.put("uri",createdEntityDesc.getClassUrl());
            json.put("uri", targetUri);

            JSONObject label = new JSONObject();
            label.put("label",createdEntityDesc.getLabel());
            json.put("label", label);

            JSONObject addToCustomSchema = new JSONObject();
            addToCustomSchema.put("uri", ppDefinition.getPpCustomSchemaUrl());
            JSONArray array = new JSONArray();
            array.add(addToCustomSchema);

            json.put("addToCustomScheme", array);


            URIBuilder uriBuilder = new URIBuilder(ppDefinition.getPpServerUrl() + "/api/schema/createClass");

            HttpPost request = new HttpPost(uriBuilder.build().normalize());
            request.addHeader("content-type", "application/json");

            LOG.debug("Request for proposing class: {}", json.toString());
            StringEntity params = new StringEntity(json.toString());
            params.setContentType("application/json");
            request.setEntity(params);

            addBasiAuthenticationForHttpRequest(request, ppDefinition.getLogin(), ppDefinition.getPassword());

            response = client.execute(request);

            checkHttpResponseStatus(response);

            //get response
            HttpEntity responseHttpEntity = response.getEntity();
            InputStream content = responseHttpEntity.getContent();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String line;
            String responseString = "";

            while ((line = buffer.readLine()) != null) {
                responseString += line;
            }

            try {
                //release all resources held by the responseHttpEntity
                EntityUtils.consume(responseHttpEntity);

                //close the stream
                response.close();
            } finally {
                response.close();
            }

            LOG.info("Creating class, response {}", responseString);
            return responseString;

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP raw POST request to URL %s", "xx");
            LOG.error(errorMsg);
            throw new PPRestApiCallException(errorMsg, ex);
        }
    }


    /**
     * Creates relation - object property
     *
     * http://adequate-project-pp.semantic-web.at/PoolParty/api?method=createDirectedRelation
     *
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public String createObjectRelationRequest(RelationDesc createdEntityDesc) throws PPRestApiCallException {
        CloseableHttpResponse response = null;
        try {

            JSONObject json = new JSONObject();

            JSONObject schemaUri = new JSONObject();
            schemaUri.put("uri", ppDefinition.getPpOntologyUrl());
            json.put("schemaUri", schemaUri);

            JSONObject targetUri = new JSONObject();
            targetUri.put("uri",createdEntityDesc.getUrl());
            json.put("uri", targetUri);

            JSONObject label = new JSONObject();
            label.put("label",createdEntityDesc.getLabel());
            json.put("label", label);

            JSONObject addToCustomSchema = new JSONObject();
            addToCustomSchema.put("uri", ppDefinition.getPpCustomSchemaUrl());
            JSONArray array = new JSONArray();
            array.add(addToCustomSchema);

            json.put("addToCustomScheme", array);

            //domain
            JSONObject domain = new JSONObject();
            domain.put("uri", createdEntityDesc.getDomain());
            JSONArray arrayDomains = new JSONArray();
            arrayDomains.add(domain);

            json.put("domain", arrayDomains);

            //range
            JSONObject range = new JSONObject();
            range.put("uri", createdEntityDesc.getRange());
            JSONArray arrayRanges = new JSONArray();
            arrayRanges.add(range);

            json.put("range", arrayRanges);

            //single
//            JSONObject singleBoolean = new JSONObject();
//            singleBoolean.put("boolean","false");
            json.put("single", "false");

            URIBuilder uriBuilder = new URIBuilder(ppDefinition.getPpServerUrl() + "/api/schema/createDirectedRelation");

            HttpPost request = new HttpPost(uriBuilder.build().normalize());
            request.addHeader("content-type", "application/json");

            StringEntity params = new StringEntity(json.toString());
            params.setContentType("application/json");
            request.setEntity(params);

            addBasiAuthenticationForHttpRequest(request, ppDefinition.getLogin(), ppDefinition.getPassword());

            response = client.execute(request);

            //process response
            checkHttpResponseStatus(response);

            //get response
            HttpEntity responseHttpEntity = response.getEntity();
            InputStream content = responseHttpEntity.getContent();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String line;
            String responseString = "";

            while ((line = buffer.readLine()) != null) {
                responseString += line;
            }

            try {
                //release all resources held by the responseHttpEntity
                EntityUtils.consume(responseHttpEntity);

                //close the stream
                response.close();
            } finally {
                response.close();
            }

            LOG.info("Creating object relation, response {}", responseString);
            return responseString;

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP raw POST request to URL %s", "xx");
            LOG.error(errorMsg);
            throw new PPRestApiCallException(errorMsg, ex);
        }
    }

    /**
     * Creates relation - data property
     *
     * http://adequate-project-pp.semantic-web.at/PoolParty/api?method=createAttribute
     *
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public String createDataTypeRelationRequest(RelationDesc createdEntityDesc) throws PPRestApiCallException {
        CloseableHttpResponse response = null;
        try {

            JSONObject json = new JSONObject();

            JSONObject schemaUri = new JSONObject();
            schemaUri.put("uri", ppDefinition.getPpOntologyUrl());
            json.put("schemaUri", schemaUri);

            JSONObject targetUri = new JSONObject();
            targetUri.put("uri",createdEntityDesc.getUrl());
            json.put("uri", targetUri);

            JSONObject label = new JSONObject();
            label.put("label",createdEntityDesc.getLabel());
            json.put("label", label);

            JSONObject addToCustomSchema = new JSONObject();
            addToCustomSchema.put("uri", ppDefinition.getPpCustomSchemaUrl());
            JSONArray array = new JSONArray();
            array.add(addToCustomSchema);

            json.put("addToCustomScheme", array);

            //domain
            JSONObject domain = new JSONObject();
            domain.put("uri", createdEntityDesc.getDomain());
            JSONArray arrayDomains = new JSONArray();
            arrayDomains.add(domain);

            json.put("domain", arrayDomains);

//            //value restriction
//            JSONObject range = new JSONObject();
//            range.put("uri", createdEntityDesc.getRange());
//            JSONArray arrayRanges = new JSONArray();
//            arrayRanges.add(range);
//
//            json.put("range", arrayRanges);

            //value restriction
            //for value restrictions, see: ValueRestriction object in ppt
            json.put("valueRestriction", "LITERAL");

            json.put("single", "false");

            URIBuilder uriBuilder = new URIBuilder(ppDefinition.getPpServerUrl() + "/api/schema/createAttribute");

            HttpPost request = new HttpPost(uriBuilder.build().normalize());
            request.addHeader("content-type", "application/json");

            StringEntity params = new StringEntity(json.toString());
            params.setContentType("application/json");
            request.setEntity(params);

            addBasiAuthenticationForHttpRequest(request, ppDefinition.getLogin(), ppDefinition.getPassword());

            response = client.execute(request);

            //process response
            checkHttpResponseStatus(response);

            //get response
            HttpEntity responseHttpEntity = response.getEntity();
            InputStream content = responseHttpEntity.getContent();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String line;
            String responseString = "";

            while ((line = buffer.readLine()) != null) {
                responseString += line;
            }

            try {
                //release all resources held by the responseHttpEntity
                EntityUtils.consume(responseHttpEntity);

                //close the stream
                response.close();
            } finally {
                response.close();
            }

            LOG.info("Creating datatype relation, response {}", responseString);
            return responseString;

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP raw POST request to URL %s", "xx");
            LOG.error(errorMsg);
            throw new PPRestApiCallException(errorMsg, ex);
        }
    }




    private static void checkHttpResponseStatus(CloseableHttpResponse response) {
        LOG.info("HTTP Response code {}", response.getStatusLine().getStatusCode());
        if (response.getStatusLine().getStatusCode() != 200) {
            StringBuilder responseAsString = new StringBuilder();
            responseAsString.append(response.getStatusLine().toString()).append('\n');
            for (Header h : response.getAllHeaders()) {
                responseAsString.append(h.toString()).append('\n');
            }
            String errorMsg = String.format("HTTP request was not successful. Received HTTP status and headers:\n%s", responseAsString);
            LOG.error(errorMsg);
            try {
                LOG.error("Response content: {}", EntityUtils.toString(response.getEntity()));
            } catch (Exception err) {
                throw new PPRestApiCallException(err.getLocalizedMessage());
            }
            throw new PPRestApiCallException(errorMsg);
        }
    }

    private static void addBasiAuthenticationForHttpRequest(HttpRequestBase request, String user, String password) {
        String basicAuth = "Basic " + encodeUserNamePassword(user, password);
        request.addHeader("Authorization", basicAuth);
    }

    private static String encodeUserNamePassword(String userName, String password) {
        String authString = userName + ":" + password;
        return Base64.encodeBase64String(authString.getBytes());
    }

}
