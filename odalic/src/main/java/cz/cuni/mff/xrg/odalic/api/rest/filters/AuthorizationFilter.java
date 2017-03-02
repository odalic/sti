/**
 *
 */
package cz.cuni.mff.xrg.odalic.api.rest.filters;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.Secured;
import cz.cuni.mff.xrg.odalic.api.rest.responses.Message;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * Role authorization filter.
 *
 * @author Václav Brodec
 *
 */
@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
@Component
public class AuthorizationFilter implements ContainerRequestFilter {

  private static Set<Role> extractRoles(final AnnotatedElement annotatedElement) {
    Preconditions.checkNotNull(annotatedElement);

    final Secured secured = annotatedElement.getAnnotation(Secured.class);
    if (secured == null) {
      return ImmutableSet.of();
    }

    return ImmutableSet.copyOf(secured.value());
  }

  @Autowired
  private UserService userService;

  @Context
  private UriInfo uriInfo;

  @Context
  private ResourceInfo resourceInfo;

  @Override
  public void filter(final ContainerRequestContext requestContext) throws IOException {
    final String userId = requestContext.getSecurityContext().getUserPrincipal().getName();

    final Method resourceMethod = this.resourceInfo.getResourceMethod();
    Preconditions.checkArgument(resourceMethod != null);

    final Set<Role> methodRoles = extractRoles(resourceMethod);

    try {
      if (methodRoles.isEmpty()) {
        final Class<?> resourceClass = this.resourceInfo.getResourceClass();
        Preconditions.checkArgument(resourceClass != null);

        final Set<Role> classRoles = extractRoles(resourceClass);
        Preconditions.checkArgument(!classRoles.isEmpty());

        checkPermissions(userId, classRoles);
        return;
      }

      checkPermissions(userId, methodRoles);
    } catch (final Exception e) {
      requestContext
          .abortWith(Message.of("Authorization failed. Insufficient rights!", e.getMessage())
              .toResponse(Status.FORBIDDEN, this.uriInfo));
      return;
    }
  }

  private void checkPermissions(final String userId, final Set<? extends Role> allowedRoles)
      throws Exception {
    final User user = this.userService.getUser(userId);

    Preconditions.checkArgument(allowedRoles.contains(user.getRole()));
  }
}
