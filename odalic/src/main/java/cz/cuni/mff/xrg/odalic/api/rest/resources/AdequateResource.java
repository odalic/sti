package cz.cuni.mff.xrg.odalic.api.rest.resources;

import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.UserService;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.json.Json;
import javax.json.JsonReader;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;

/**
 * Adequate helper service
 */
@Component
@Path("/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class AdequateResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdequateResource.class);

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;


  @Autowired
  public AdequateResource(final UserService userService, final FileService fileService) {
    Preconditions.checkNotNull(userService, "The userService cannot be null!");
    Preconditions.checkNotNull(fileService, "The fileService cannot be null!");
  }

  public AdequateResource() {
  }

  @POST
  @Path("adequate")
  @Secured({Role.ADMINISTRATOR, Role.USER})
  @Consumes("application/x-www-form-urlencoded")
  @Produces({MediaType.APPLICATION_JSON})
  public Response saveToAddequate(
            @Context  HttpHeaders headers,
            final @FormParam("userId") String userId,
            final @FormParam("taskId") String taskId) {

    Security.checkAuthorization(this.securityContext, userId);

    LOGGER.info("Calling adequate service with: {} {}", userId, taskId);

    //TODO later copy pipeline X to always create fresh pipeline and get dynamically pipeline Id
    //now we use one fixed pipeline
    String pipelineId = "118";

    String authorizationHeader = headers.getHeaderString("Authorization");
    LOGGER.info("Auth header:", authorizationHeader);
    String token = authorizationHeader;

    if (token == null || token.isEmpty()) {
        return Response.serverError().entity("{ \"error\": \"Cannot get any token from the header of the request\" }").build();
    }

    //prepare config file for UV pipeline - task ID, token
    //save it to file:////data/odalic/configs/{{execId}}-config.ttl
    //sample:
    //  <http://odalic.adequate.at/config/1> <http://odalic.adequate.at/scheme/taskId> "autoimport-KTRPTvAXiXM-MFU" .
    //  <http://odalic.adequate.at/config/1> <http://odalic.adequate.at/scheme/token> "ba6476e0cb96157dbe27d72809f81781c824051c1dd68d0fb40c0aa379e4ee1d" .

    StringBuilder configFile = new StringBuilder();
    configFile.append("<http://odalic.adequate.at/config/1> <http://odalic.adequate.at/scheme/taskId> \"");
    configFile.append(taskId);
    configFile.append("\" . <http://odalic.adequate.at/config/1> <http://odalic.adequate.at/scheme/token> \"");
    configFile.append(token);
    configFile.append("\" .");

    java.io.File f = new java.io.File("/data/odalic/configs/" + pipelineId + "-config.ttl");
    try {
      FileUtils.writeStringToFile(f, configFile.toString());
    } catch (IOException e) {
      return Response.serverError().entity("{ \"error\": \"Cannot write configuration file for UnifiedViews: "+ e.getLocalizedMessage() + "\" }").build();

    }

    //run pipeline with that pipeline ID
    String requestUrl = "https://tools.adequate.at/master/api/1/pipelines/" + pipelineId + "/executions";
    String jsonResponse;

    try {
      CloseableHttpClient client = HttpClients.createDefault();

      URIBuilder uriBuilder = new URIBuilder(requestUrl);
      uriBuilder.setPath(uriBuilder.getPath());
      HttpPost httpPost = new HttpPost(uriBuilder.build().normalize());

      String json = "{  \"userExternalId\" : \"admin\" }";
      StringEntity entity = new StringEntity(json);
      httpPost.setEntity(entity);
      httpPost.setHeader("Accept", "application/json");
      httpPost.setHeader("Content-type", "application/json");

      UsernamePasswordCredentials creds
              = new UsernamePasswordCredentials("admin", "");
      try {
        httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
      } catch (AuthenticationException ex) {
        return Response.serverError().entity("{ \"error\": \"Failed to execute HTTP request to get status of pipeline exec - auth header: "+ ex.getLocalizedMessage() + "\" }").build();
      }

      CloseableHttpResponse response = client.execute(httpPost);
      if (response.getStatusLine().getStatusCode() != 200) {
        return Response.serverError().entity("{ \"error\": \"Failed to execute HTTP request to get status of pipeline exec - response code was: "+ response.getStatusLine().getStatusCode() + "\" }").build();
      }
      //parse response
      HttpEntity respEntity = response.getEntity();
      jsonResponse = EntityUtils.toString(respEntity);

      client.close();

    } catch (URISyntaxException | IllegalStateException | IOException ex) {
      return Response.serverError().entity("{ \"error\": \"Failed to execute HTTP request to execute pipeline: "+ ex.getLocalizedMessage() + "\" }").build();
    }

    int execId = getExecutionId(jsonResponse);

    //get status of pipeline execution - and wait until it is finished
    requestUrl = "https://tools.adequate.at/master/api/1/pipelines/" + pipelineId + "/executions/" + String.valueOf(execId);

    String status = "QUEUED";
    while(status.equals("QUEUED") || status.equals("RUNNING") ) {
      try {
        Thread.sleep(1000);
        CloseableHttpClient client = HttpClients.createDefault();

        URIBuilder uriBuilder = new URIBuilder(requestUrl);
        uriBuilder.setPath(uriBuilder.getPath());
        HttpGet httpGet = new HttpGet(uriBuilder.build().normalize());

        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-type", "application/json");

        UsernamePasswordCredentials creds
                = new UsernamePasswordCredentials("admin", "ree2xaegie2U");
        try {
          httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet, null));
        } catch (AuthenticationException ex) {
          return Response.serverError().entity("{ \"error\": \"Failed to execute HTTP request to get status of pipeline exec - auth header: "+ ex.getLocalizedMessage() + "\" }").build();
        }

        CloseableHttpResponse response = client.execute(httpGet);
        if (response.getStatusLine().getStatusCode() != 200) {
          return Response.serverError().entity("{ \"error\": \"Failed to execute HTTP request to get status of pipeline exec - response code was: "+ response.getStatusLine().getStatusCode() + "\" }").build();

        }
        //parse response
        HttpEntity respEntity = response.getEntity();
        jsonResponse = EntityUtils.toString(respEntity);
        LOGGER.info("Status of calling information about the pipeline execution is: {}", status);
        status = getStatus(jsonResponse);
        client.close();

      } catch (URISyntaxException | IllegalStateException | IOException ex) {
        return Response.serverError().entity("{ \"error\": \"Failed to execute HTTP request to execute pipeline: "+ ex.getLocalizedMessage() + "\" }").build();

      } catch (InterruptedException ex) {
        return Response.serverError().entity("{ \"error\": \"Failed to execute HTTP request to execute pipeline: "+ ex.getLocalizedMessage() + "\" }").build();
      }
    }

    String resultingStatus = status;
    LOGGER.info("Final status of pipeline execution is: {}", resultingStatus);
    if (resultingStatus.equals("FINISHED_SUCCESS") || resultingStatus.equals("FINISHED_WARNING")) {
      return Response.ok().build();
    }
    else {
      return Response.serverError().entity("{ \"error\": \"There was an issue running UnifiedViews pipeline responsible for saving the data. Status of executing pipeline "+ pipelineId + " was: " + status + "\" }").build();
    }

  }

  public int getExecutionId(String jsonResponse) {

    JSONObject obj = new JSONObject(jsonResponse);
    return obj.getInt("id");

  }

  public String getStatus(String jsonResponse) {

    JSONObject obj = new JSONObject(jsonResponse);
    return obj.getString("status");

  }

}
