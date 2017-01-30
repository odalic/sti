package cz.cuni.mff.xrg.odalic.api.rest.resources;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

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

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Reply;
import cz.cuni.mff.xrg.odalic.api.rest.util.Security;
import cz.cuni.mff.xrg.odalic.api.rest.values.FormatValue;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.files.formats.FormatService;
import cz.cuni.mff.xrg.odalic.users.Role;

@Component
@Path("/")
@Secured({Role.ADMINISTRATOR, Role.USER})
public final class FormatResource {

  private final FormatService formatService;

  @Context
  private SecurityContext securityContext;

  @Context
  private UriInfo uriInfo;

  @Autowired
  public FormatResource(final FormatService formatService) {
    Preconditions.checkNotNull(formatService);

    this.formatService = formatService;
  }

  @PUT
  @Path("users/{userId}/files/{fileId}/format")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFormatForFileId(final @PathParam("userId") String userId, final @PathParam("fileId") String fileId, final FormatValue formatValue) {
    Security.checkAuthorization(securityContext, userId);
    
    if (formatValue == null) {
      throw new BadRequestException("Format must be provided!");
    }

    final Charset charset;
    try {
      charset = Charset.forName(formatValue.getCharset());
    } catch (final IllegalCharsetNameException e) {
      throw new BadRequestException("Illegal character set name.", e);
    } catch (final UnsupportedCharsetException e) {
      throw new BadRequestException("Character set not supported.", e);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("Character set not set.", e);
    }

    final Format format = new Format(charset, formatValue.getDelimiter(),
        formatValue.isEmptyLinesIgnored(), formatValue.getQuoteCharacter(),
        formatValue.getEscapeCharacter(), formatValue.getCommentMarker());

    try {
      formatService.setForFileId(userId, fileId, format);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException("The file does not exist.", e);
    }
    return Message.of("Format set.").toResponse(Response.Status.OK, uriInfo);
  }
  
  @PUT
  @Path("files/{fileId}/format")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFormatForFileId(final @PathParam("fileId") String fileId, final FormatValue formatValue) {
    return putFormatForFileId(securityContext.getUserPrincipal().getName(), fileId, formatValue);
  }

  @GET
  @Path("users/{userId}/files/{fileId}/format")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFormatForFileId(final @PathParam("userId") String userId, final @PathParam("fileId") String fileId) {
    Security.checkAuthorization(securityContext, userId);
    
    final Format formatForFileId;
    try {
      formatForFileId = formatService.getForFileId(userId, fileId);
    } catch (final IllegalArgumentException e) {
      throw new NotFoundException("File does not exist.", e);
    }

    return Reply.data(Response.Status.OK, formatForFileId, uriInfo).toResponse();
  }
  
  @GET
  @Path("files/{fileId}/format")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFormatForFileId(final @PathParam("fileId") String fileId) {
    return getFormatForFileId(securityContext.getUserPrincipal().getName(), fileId);
  }
}
