/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rest.filters;

import java.io.IOException;
import java.security.Principal;

import javax.ws.rs.Priorities;
import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
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

    @Autowired
    private UserService userService;
  
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header must be provided!");
        }

        final String token = authorizationHeader.substring("Bearer".length()).trim();

        final User user;
        try {
            user = userService.validateToken(new Token(token));
        } catch (final Exception e) {
          requestContext.abortWith(
              Response.status(Response.Status.UNAUTHORIZED).build());
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
              return true;
          }

          @Override
          public boolean isSecure() {
              return false;
          }

          @Override
          public String getAuthenticationScheme() {
              return null;
          }
      });
    }
}
