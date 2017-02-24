package cz.cuni.mff.xrg.odalic.api.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Welcome page.
 * 
 * @author Václav Brodec
 */
@Path("/")
public final class WelcomeResource {

  private static final String WELCOME_PAGE_CONTENT =
      "<html>" + "<title>Odalic REST API</title>" + "<body>"
          + "<h1>Odalic REST API is working!</h1>" + "<p>For more information about Odalic visit "
          + "<a href=\"https://github.com/odalic\">the project GitHub page.</a>" + "</p>"
          + "</body>" + "</html>";

  @GET
  @Produces(MediaType.TEXT_HTML)
  public String welcome() {
    return WELCOME_PAGE_CONTENT;
  }
}
