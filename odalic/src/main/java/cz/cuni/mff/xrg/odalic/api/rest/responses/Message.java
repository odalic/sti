package cz.cuni.mff.xrg.odalic.api.rest.responses;

import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;


/**
 * Reporting message with extra details for developers.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement
public final class Message {

  private static final String LOCATION_HEADER_NAME = "Location";

  @XmlTransient
  public static Message of(final String text) {
    return new MessageBuilder().text(text).build();
  }

  @XmlTransient
  public static Message of(final String text, final String debugContent) {
    return new MessageBuilder().text(text).debugContent(debugContent).build();
  }

  private final String text;

  private final List<URI> additionalResources;

  private final String debugContent;

  public Message(@Nullable final String text, final List<URI> additionalResources,
      @Nullable final String debugContent) {
    Preconditions.checkNotNull(additionalResources, "The additionalResources cannot be null!");

    this.text = text;
    this.additionalResources = ImmutableList.copyOf(additionalResources);
    this.debugContent = debugContent;
  }

  /**
   * @return the additional resources
   */
  @XmlElement
  public List<URI> getAdditionalResources() {
    return this.additionalResources;
  }

  /**
   * @return the debug content
   */
  @XmlElement
  @Nullable
  public String getDebugContent() {
    return this.debugContent;
  }

  /**
   * @return the text
   */
  @XmlElement
  @Nullable
  public String getText() {
    return this.text;
  }

  /**
   * Utility method that wraps the message into a JSON response and assigns it the provided
   * {@link StatusType}.
   *
   * @param statusType status type
   * @param uriInfo request URI information
   * @return a {@link Reply}
   */
  @XmlTransient
  public Response toResponse(final StatusType statusType, final UriInfo uriInfo) {
    Preconditions.checkNotNull(statusType, "The statusType cannot be null!");

    return Reply.message(statusType, this, uriInfo).toResponse();
  }

  /**
   * Utility method that wraps the message into a JSON response and assigns it the provided
   * {@link StatusType} and location header content.
   *
   * @param statusType status type
   * @param location location header content
   * @param uriInfo request URI information
   * @return a {@link Reply}
   */
  @XmlTransient
  public Response toResponse(final StatusType statusType, final URL location,
      final UriInfo uriInfo) {
    Preconditions.checkNotNull(statusType, "The statusType cannot be null!");
    Preconditions.checkNotNull(location, "The location cannot be null!");

    return toResponseBuilder(statusType, uriInfo).header(LOCATION_HEADER_NAME, location).build();
  }

  /**
   * Utility method that wraps the message into a JSON response builder and assigns it the provided
   * {@link StatusType}.
   *
   * @param statusType status type
   * @param uriInfo request URI information
   * @return a {@link ResponseBuilder}
   */
  @XmlTransient
  public ResponseBuilder toResponseBuilder(final StatusType statusType, final UriInfo uriInfo) {
    Preconditions.checkNotNull(statusType, "The statusType cannot be null!");

    return Reply.message(statusType, this, uriInfo).toResponseBuilder();
  }

  @Override
  public String toString() {
    return "Message [text=" + this.text + ", additionalResources=" + this.additionalResources
        + ", debugContent=" + this.debugContent + "]";
  }
}
