package cz.cuni.mff.xrg.odalic.api.rest.resources;

import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.api.rest.values.FileValueInput;
import cz.cuni.mff.xrg.odalic.api.rest.values.StateValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.util.States;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.entities.ClassProposal;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.UserService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.shef.dcs.kbproxy.ProxyException;
import uk.ac.shef.dcs.sti.STIException;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Adequate helper service
 */
@Component
@Path("/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class AdequateResource {

  public static final String TEXT_CSV_MEDIA_TYPE = "text/csv";

  private static final Logger LOGGER = LoggerFactory.getLogger(AdequateResource.class);

  private final FileService fileService;
  private final UserService userService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;


  @Autowired
  public AdequateResource(final UserService userService, final FileService fileService) {
    Preconditions.checkNotNull(userService, "The userService cannot be null!");
    Preconditions.checkNotNull(fileService, "The fileService cannot be null!");

    this.userService = userService;
    this.fileService = fileService;
  }

  @POST
  @Path("adequate")
  @Secured({Role.ADMINISTRATOR, Role.USER})
  @Consumes("application/x-www-form-urlencoded")
  @Produces({MediaType.APPLICATION_JSON})
  public Response propose(
            final @FormParam("userId") String userId,
            final @FormParam("taskId") String taskId,
            final @FormParam("taskId") String authToken) {

    Security.checkAuthorization(this.securityContext, userId);

    //copy pipeline X

    //prepare config file for UV pipeline - task ID, token

    //save it to /data/odalic/{{pipelineId}}/config.ttl

    //run pipeline with that pipeline ID

    return Response.ok().build(); //Reply.data(Response.Status.OK, state, this.uriInfo).toResponse();
  }


}
