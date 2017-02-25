/**
 *
 */
package cz.cuni.mff.xrg.odalic.api.rest.filters;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.users.Token;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * Authentication filter.
 *
 * @author Václav Brodec
 *
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
@Component
public final class AuthenticationFilter implements ContainerRequestFilter {

  private static final String INVALID_REQUEST_CHALLENGE_FORMAT =
      "Bearer realm=\"Odalic\", error=\"invalid_request\", error_description=\"%s\"";
  private static final String INVALID_TOKEN_CHALLENGE_FORMAT =
      "Bearer realm=\"Odalic\", error=\"invalid_token\", error_description=\"%s\"";
  private static final String AUTHENTICATION_SCHEME = "Bearer";
  private static final String AUTHENTICATION_SCHEME_DELIMITER = " ";
  private static final Set<String> SECURE_PROTOCOLS_NAMES = ImmutableSet.of("https");

  @Autowired
  private UserService userService;

  @Context
  private UriInfo uriInfo;

  @Override
  public void filter(final ContainerRequestContext requestContext) throws IOException {
    final String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
    if (authorizationHeader == null) {
      requestContext
          .abortWith(
              Message.of("Authorization header must be provided!")
                  .toResponseBuilder(Response.Status.UNAUTHORIZED, this.uriInfo)
                  .header(HttpHeaders.WWW_AUTHENTICATE, String.format(
                      INVALID_REQUEST_CHALLENGE_FORMAT, "Authorization header must be provided!"))
                  .build());
      return;
    }

    if (!authorizationHeader.startsWith(AUTHENTICATION_SCHEME + AUTHENTICATION_SCHEME_DELIMITER)) {
      requestContext.abortWith(
          Message.of("Authorization header must specify the supported authentication scheme!")
              .toResponseBuilder(Response.Status.UNAUTHORIZED, this.uriInfo)
              .header(HttpHeaders.WWW_AUTHENTICATE,
                  String.format(INVALID_REQUEST_CHALLENGE_FORMAT,
                      "Authorization header must specify the supported authentication scheme!"))
              .build());
      return;
    }

    final String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

    final User user;
    try {
      user = this.userService.validateToken(new Token(token));
    } catch (final Exception e) {
      requestContext.abortWith(Message.of("Authentication failed!", e.getMessage())
          .toResponseBuilder(Response.Status.UNAUTHORIZED, this.uriInfo)
          .header(HttpHeaders.WWW_AUTHENTICATE,
              String.format(INVALID_TOKEN_CHALLENGE_FORMAT, e.getMessage()))
          .build());
      return;
    }

    requestContext.setSecurityContext(new SecurityContext() {

      @Override
      public String getAuthenticationScheme() {
        return AUTHENTICATION_SCHEME;
      }

      @Override
      public Principal getUserPrincipal() {

        return () -> user.getEmail();
      }

      @Override
      public boolean isSecure() {
        try {
          return SECURE_PROTOCOLS_NAMES
              .contains(AuthenticationFilter.this.uriInfo.getRequestUri().toURL().getProtocol());
        } catch (final Exception e) {
          return false;
        }
      }

      @Override
      public boolean isUserInRole(final String role) {
        return user.getRole().toString().equals(role);
      }
    });
  }
}
