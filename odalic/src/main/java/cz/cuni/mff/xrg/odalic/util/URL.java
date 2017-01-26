package cz.cuni.mff.xrg.odalic.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.google.common.base.Preconditions;

/**
 * Utility class for -- you guessed it -- working with URLs.
 * 
 * @author Václav Brodec
 *
 */
public final class URL {

  /**
   * We want to keep this class uninstantiable, so no visible constructor is available.
   */
  private URL() {}

  /**
   * One-off function that essentially takes the absolute path of the request URI (encapsulated by
   * the {@link UriInfo} instance) and resolves the sub-resource name against it to get the absolute
   * path of the sub-resource.
   * 
   * @param requestUriInfo URI info
   * @param subResource a string to resolve against the URI
   * @return absolute path of the string
   * @throws MalformedURLException If a protocol handler for the URL could not be found, or if some
   *         other error occurred while constructing the URL
   * @throws IllegalStateException If called outside a scope of a request
   * @throws IllegalArgumentException If the given string violates RFC 2396
   */
  public static java.net.URL getSubResourceAbsolutePath(UriInfo requestUriInfo, String subResource)
      throws MalformedURLException, IllegalStateException {
    try {
      return requestUriInfo.getAbsolutePath()
          .resolve(URLEncoder.encode(subResource, StandardCharsets.UTF_8.displayName())).toURL();
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Sets the query parameter and return the modified URL.
   * 
   * @param url URL
   * @param key query parameter key
   * @param value query parameter value
   * @return modified URL
   * @throws MalformedURLException
   */
  public static java.net.URL setQueryParameter(final java.net.URL url, final String key,
      final String value) throws MalformedURLException {
    Preconditions.checkNotNull(url);
    Preconditions.checkNotNull(key);
    Preconditions.checkNotNull(value);

    try {
      return UriBuilder.fromUri(url.toURI()).queryParam(key, value).build().toURL();
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Extracts a stamp string from the URI information.
   * 
   * @param uriInfo request URI information
   * @param queryParameterName stamp query parameter name
   * @return the stamp string, {@code null} when not provided
   */
  @Nullable
  public static String getStamp(UriInfo uriInfo, String queryParameterName) {
    return uriInfo.getQueryParameters().getFirst(queryParameterName);
  }
}
