package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.util.Comparator;
import java.util.NavigableSet;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.api.rest.values.ConfigurationValue;
import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;
import cz.cuni.mff.xrg.odalic.tasks.executions.ExecutionService;
import cz.cuni.mff.xrg.odalic.users.Role;

@Component
@Path("/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class ConfigurationResource {

  public static final String TURTLE_MIME_TYPE = "text/turtle";

  private final ConfigurationService configurationService;
  private final FileService fileService;
  private final BasesService basesService;
  private final ExecutionService executionService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public ConfigurationResource(final ConfigurationService configurationService,
      final FileService fileService, final BasesService basesService,
      final ExecutionService executionService) {
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(basesService);
    Preconditions.checkNotNull(executionService);

    this.configurationService = configurationService;
    this.fileService = fileService;
    this.basesService = basesService;
    this.executionService = executionService;
  }

  @GET
  @Path("tasks/{taskId}/configuration")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConfigurationForTaskId(final @PathParam("taskId") String taskId) {
    return getConfigurationForTaskId(this.securityContext.getUserPrincipal().getName(), taskId);
  }

  @GET
  @Path("users/{userId}/tasks/{taskId}/configuration")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConfigurationForTaskId(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId) {
    Security.checkAuthorization(this.securityContext, userId);

    final Configuration configurationForTaskId;
    try {
      configurationForTaskId = this.configurationService.getForTaskId(userId, taskId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("Configuration for the task does not exist.", e);
    }

    return Reply.data(Response.Status.OK, configurationForTaskId, this.uriInfo).toResponse();
  }

  @PUT
  @Path("tasks/{taskId}/configuration")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putConfigurationForTaskId(final @PathParam("taskId") String taskId,
      final ConfigurationValue configurationValue) {
    return putConfigurationForTaskId(this.securityContext.getUserPrincipal().getName(), taskId,
        configurationValue);
  }

  @PUT
  @Path("users/{userId}/tasks/{taskId}/configuration")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putConfigurationForTaskId(final @PathParam("userId") String userId,
      final @PathParam("taskId") String taskId, final ConfigurationValue configurationValue) {
    Security.checkAuthorization(this.securityContext, userId);

    if (configurationValue == null) {
      throw new BadRequestException("Configuration must be provided!");
    }

    if (configurationValue.getInput() == null) {
      throw new BadRequestException("Input must be specified!");
    }

    if (configurationValue.getPrimaryBase() == null) {
      throw new BadRequestException("The primary base must be specified!");
    }

    final File input;
    try {
      input = this.fileService.getById(userId, configurationValue.getInput());
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The configured input file is not registered.", e);
    }

    final NavigableSet<KnowledgeBase> usedBases;
    if (configurationValue.getUsedBases() == null) {
      usedBases = this.basesService.getBases(userId);
    } else {
      usedBases = configurationValue.getUsedBases().stream().map(e -> this.basesService.getByName(userId, e.getName())).collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
    }

    final Configuration configuration;
    try {
      configuration = new Configuration(input, usedBases, this.basesService.getByName(userId, configurationValue.getPrimaryBase().getName()),
          configurationValue.getFeedback(), configurationValue.getRowsLimit(),
          configurationValue.isStatistical());
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e);
    }

    try {
      this.executionService.unscheduleForTaskId(userId, taskId);
      this.configurationService.setForTaskId(userId, taskId, configuration);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The configured task does not exist.", e);
    }
    return Message.of("Configuration set.").toResponse(Response.Status.OK, this.uriInfo);
  }
}
