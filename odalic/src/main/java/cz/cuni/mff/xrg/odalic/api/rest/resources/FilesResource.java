package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.api.rest.values.FileValueInput;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * File resource definition.
 * 
 * @author Václav Brodec
 */
@Component
@Path("/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class FilesResource {

  public static final String TEXT_CSV_MEDIA_TYPE = "text/csv";

  private static final Logger LOGGER = LoggerFactory.getLogger(FilesResource.class);

  private final FileService fileService;
  private final UserService userService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;


  @Autowired
  public FilesResource(final UserService userService, final FileService fileService) {
    Preconditions.checkNotNull(userService);
    Preconditions.checkNotNull(fileService);

    this.userService = userService;
    this.fileService = fileService;
  }

  @GET
  @Path("users/{userId}/files")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFiles(final @PathParam("userId") String userId) {
    Security.checkAuthorization(securityContext, userId);

    final List<File> files = fileService.getFiles(userId);

    return Reply.data(Response.Status.OK, files, uriInfo).toResponse();
  }

  @GET
  @Path("files")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFiles() {
    return getFiles(securityContext.getUserPrincipal().getName());
  }

  @GET
  @Path("users/{userId}/files/{fileId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFileById(final @PathParam("userId") String userId,
      final @PathParam("fileId") String fileId) {
    Security.checkAuthorization(securityContext, userId);

    final File file;
    try {
      file = fileService.getById(userId, fileId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The file does not exist!", e);
    }

    return Reply.data(Response.Status.OK, file, uriInfo).toResponse();
  }

  @GET
  @Path("files/{fileId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFileById(final @PathParam("fileId") String fileId) {
    return getFileById(securityContext.getUserPrincipal().getName(), fileId);
  }

  @PUT
  @Path("users/{userId}/files/{fileId}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(final @PathParam("userId") String userId,
      @PathParam("fileId") String fileId, @FormDataParam("input") InputStream fileInputStream)
      throws IOException {
    Security.checkAuthorization(securityContext, userId);

    if (fileInputStream == null) {
      throw new BadRequestException("No input provided!");
    }

    final URL location =
        cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, fileId);
    
    final File file;
    try {
      file = new File(userService.getUser(userId), fileId, location, new Format(), true);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    if (!fileService.existsFileWithId(userId, fileId)) {
      fileService.create(file, fileInputStream);

      return Message.of("A new file has been created AT THE STANDARD LOCATION.")
          .toResponse(Response.Status.CREATED, location, uriInfo);
    } else {
      fileService.replace(file);
      return Message.of("The file you specified has been fully updated AT THE STANDARD LOCATION.")
          .toResponse(Response.Status.OK, location, uriInfo);
    }
  }

  @PUT
  @Path("files/{fileId}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(@PathParam("fileId") String fileId,
      @FormDataParam("input") InputStream fileInputStream) throws IOException {
    return putFileById(securityContext.getUserPrincipal().getName(), fileId, fileInputStream);
  }

  @PUT
  @Path("users/{userId}/files/{fileId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(final @PathParam("userId") String userId,
      final @PathParam("fileId") String fileId, final FileValueInput fileInput)
      throws MalformedURLException {
    Security.checkAuthorization(securityContext, userId);

    if (fileInput == null) {
      throw new BadRequestException("No file description provided!");
    }

    if (fileInput.getLocation() == null) {
      throw new BadRequestException("No location provided!");
    }

    final Format usedFormat = getFormatOrDefault(fileInput);

    final File file;
    try {
      file = new File(this.userService.getUser(userId), fileId, fileInput.getLocation(), usedFormat,
          false);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    if (!fileService.existsFileWithId(userId, fileId)) {
      fileService.create(file);

      return Message.of("A new remote file has been registered.").toResponse(
          Response.Status.CREATED,
          cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, fileId), uriInfo);
    } else {
      fileService.replace(file);

      return Message.of("The file description has been updated.").toResponse(Response.Status.OK,
          cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, fileId), uriInfo);
    }
  }

  @PUT
  @Path("files/{fileId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFileById(final @PathParam("fileId") String fileId,
      final FileValueInput fileInput) throws MalformedURLException {
    return putFileById(securityContext.getUserPrincipal().getName(), fileId, fileInput);
  }

  private Format getFormatOrDefault(FileValueInput fileInput) {
    final @Nullable Format format = fileInput.getFormat();
    final Format usedFormat;
    if (format == null) {
      usedFormat = new Format();
    } else {
      usedFormat = format;
    }
    return usedFormat;
  }

  @POST
  @Path("users/{userId}/files")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postFile(final @PathParam("userId") String userId,
      final @FormDataParam("input") InputStream fileInputStream,
      final @FormDataParam("input") FormDataContentDisposition fileDetail) throws IOException {
    Security.checkAuthorization(securityContext, userId);

    if (fileInputStream == null) {
      throw new BadRequestException("No input provided!");
    }

    if (fileDetail == null) {
      throw new BadRequestException("No input detail provided!");
    }

    final String fileId = fileDetail.getFileName();
    if (fileId == null) {
      throw new BadRequestException("No file name provided!");
    }

    final File file;
    try {
      file = new File(userService.getUser(userId), fileId,
          cz.cuni.mff.xrg.odalic.util.URL.getSubResourceAbsolutePath(uriInfo, fileId), new Format(),
          true);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    if (fileService.existsFileWithId(userId, fileId)) {
      throw new WebApplicationException(
          "There already exists a file with the same name as you provided.",
          Response.Status.CONFLICT);
    }

    fileService.create(file, fileInputStream);
    return Message
        .of("A new file has been registered AT THE LOCATION DERIVED from the name of the one uploaded.")
        .toResponse(Response.Status.CREATED, file.getLocation(), uriInfo);
  }

  @POST
  @Path("files")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postFile(final @FormDataParam("input") InputStream fileInputStream,
      final @FormDataParam("input") FormDataContentDisposition fileDetail) throws IOException {
    return postFile(securityContext.getUserPrincipal().getName(), fileInputStream, fileDetail);
  }

  @DELETE
  @Path("users/{userId}/files/{fileId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteFileById(final @PathParam("userId") String userId, final @PathParam("fileId") String fileId) {
    Security.checkAuthorization(securityContext, userId);
    
    try {
      fileService.deleteById(userId, fileId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("The file does not exist!", e);
    } catch (final IllegalStateException e) {
      throw new WebApplicationException(e.getMessage(), e, Response.Status.CONFLICT);
    }

    return Message.of("File definition deleted.").toResponse(Response.Status.OK, uriInfo);
  }
  
  @DELETE
  @Path("files/{fileId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteFileById(final @PathParam("fileId") String fileId) {
    return deleteFileById(securityContext.getUserPrincipal().getName(), fileId);
  }

  @GET
  @Path("users/{userId}/files/{fileId}")
  @Produces(TEXT_CSV_MEDIA_TYPE)
  public Response getCsvDataById(final @PathParam("userId") String userId, final @PathParam("fileId") String fileId) throws IOException {
    Security.checkAuthorization(securityContext, userId);
    
    final String data;
    try {
      data = fileService.getDataById(userId, fileId);
    } catch (final IllegalArgumentException e) {
      LOGGER.error(e.getMessage(), e);

      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok(data).build();
  }
  
  @GET
  @Path("files/{fileId}")
  @Produces(TEXT_CSV_MEDIA_TYPE)
  public Response getCsvDataById(final @PathParam("fileId") String fileId) throws IOException {
    return getCsvDataById(securityContext.getUserPrincipal().getName(), fileId);
  }
}
