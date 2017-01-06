package cz.cuni.mff.xrg.odalic.api.rest.resources;

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
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.values.ConfigurationValue;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService;

@Component
@Path("/tasks/{id}/configuration")
public final class ConfigurationResource {

  private final ConfigurationService configurationService;
  private final FileService fileService;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public ConfigurationResource(ConfigurationService configurationService, FileService fileService) {
    Preconditions.checkNotNull(configurationService);
    Preconditions.checkNotNull(fileService);

    this.configurationService = configurationService;
    this.fileService = fileService;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putConfigurationForTaskId(@PathParam("id") String id,
      ConfigurationValue configurationValue) {
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
      input = fileService.getById(configurationValue.getInput());
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The configured input file is not registered.", e);
    }

    final Configuration configuration;
    try {
      if (configurationValue.getFeedback() == null) {
        configuration = new Configuration(input, configurationValue.getPrimaryBase(),
            configurationValue.getRowsLimit());
      } else {
        configuration = new Configuration(input, configurationValue.getPrimaryBase(),
            configurationValue.getFeedback(), configurationValue.getRowsLimit());
      }
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e);
    }

    try {
      configurationService.setForTaskId(id, configuration);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The configured task does not exist.", e);
    }
    return Message.of("Configuration set.").toResponse(Response.Status.OK, uriInfo);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getConfigurationForTaskId(@PathParam("id") String taskId) {
    final Configuration configurationForTaskId;
    try {
      configurationForTaskId = configurationService.getForTaskId(taskId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("Configuration for the task does not exist.", e);
    }

    return Reply.data(Response.Status.OK, configurationForTaskId, uriInfo).toResponse();
  }
}
