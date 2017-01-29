package cz.cuni.mff.xrg.odalic.api.rest.util;

import java.security.Principal;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.SecurityContext;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.users.Role;

/**
 * REST API security utility methods.
 * 
 * @author Václav Brodec
 *
 */
public final class Security {



  private Security() {}

  /**
   * <p>
   * Verifies that the authenticated user is either {@link Role#ADMINISTRATOR} or it can access
   * resources under the provided user ID.
   * </p>
   * 
   * <p>
   * Raises standard {@link WebApplicationException}s when respective authentication and
   * authorization requirements are not met.
   * </p>
   * 
   * @param securityContext a {@link SecurityContext} instance
   * @param userId user ID
   */
  public static void checkAuthorization(final SecurityContext securityContext,
      final String userId) {
    Preconditions.checkNotNull(userId, "The user ID cannot be null!");

    final Principal userPrincipal = securityContext.getUserPrincipal();
    if (userPrincipal == null) {
      throw new BadRequestException("No authenticated user!");
    }

    if (securityContext.isUserInRole(Role.ADMINISTRATOR.toString())) {
      return;
    }

    if (userId.equals(userPrincipal.getName())) {
      return;
    }

    throw new ForbiddenException(
        "The authenticated user is not authorized to access the resource!");
  }
}
