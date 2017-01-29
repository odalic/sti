/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rest.filters;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.ws.rs.Priorities;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

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
@Priority(Priorities.AUTHENTICATION)
@Component
public final class AuthenticationFilter implements ContainerRequestFilter {

  private static final String CHALLENGE_FORMAT =
      "Bearer realm=\"Odalic\", error=\"invalid_token\", error_description=\"%s\"";
  private static final String AUTHENTICATION_SCHEME = "Bearer";
  private static final String AUTHENTICATION_SCHEME_DELIMITER = " ";
  private static final Set<String> SECURE_PROTOCOLS_NAMES = ImmutableSet.of("https");

  @Autowired
  private UserService userService;

  @Context
  private UriInfo uriInfo;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    final String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
    if (authorizationHeader == null) {
      requestContext.abortWith(Message.of("Authorization header must be provided!")
          .toResponse(Status.BAD_REQUEST, uriInfo));
      return;
    }

    if (!authorizationHeader.startsWith(AUTHENTICATION_SCHEME + AUTHENTICATION_SCHEME_DELIMITER)) {
      requestContext.abortWith(
          Message.of("Authorization header must specify the supported authentication scheme!")
              .toResponse(Status.BAD_REQUEST, uriInfo));
      return;
    }

    final String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

    final User user;
    try {
      user = userService.validateToken(new Token(token));
    } catch (final Exception e) {
      requestContext.abortWith(Message.of("Authentication failed!", e.getMessage())
          .toResponseBuilder(Response.Status.UNAUTHORIZED, uriInfo)
          .header(HttpHeaders.WWW_AUTHENTICATE, String.format(CHALLENGE_FORMAT, e.getMessage()))
          .build());
      return;
    }

    requestContext.setSecurityContext(new SecurityContext() {

      @Override
      public Principal getUserPrincipal() {

        return new Principal() {

          @Override
          public String getName() {
            return user.getEmail();
          }
        };
      }

      @Override
      public boolean isUserInRole(String role) {
        return user.getRole().toString().equals(role);
      }

      @Override
      public boolean isSecure() {
        try {
          return SECURE_PROTOCOLS_NAMES.contains(uriInfo.getRequestUri().toURL().getProtocol());
        } catch (final Exception e) {
          return false;
        }
      }

      @Override
      public String getAuthenticationScheme() {
        return AUTHENTICATION_SCHEME;
      }
    });
  }
}
