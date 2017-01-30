/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rest;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBException;

import cz.cuni.mff.xrg.odalic.api.rest.resources.*;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import cz.cuni.mff.xrg.odalic.api.rest.filters.AuthenticationFilter;
import cz.cuni.mff.xrg.odalic.api.rest.filters.AuthorizationFilter;
import cz.cuni.mff.xrg.odalic.api.rest.filters.CorsResponseFilter;
import cz.cuni.mff.xrg.odalic.api.rest.filters.LoggingResponseFilter;
import cz.cuni.mff.xrg.odalic.api.rest.responses.ThrowableMapper;

/**
 * Configures the provided resources, filters, mappers and features.
 * 
 * @author Václav Brodec
 * 
 * @see org.glassfish.jersey.server.ResourceConfig
 */
public final class Configuration extends ResourceConfig {

  public Configuration() throws JAXBException {
    /* Jersey JSON exception mapping bug workaround.
     * 
     * https://java.net/jira/browse/JERSEY-2722
     * 
     */
    register(JacksonJaxbJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
    
    // Resources registration
    register(FilesResource.class);
    register(TasksResource.class);
    register(ConfigurationResource.class);
    register(FeedbackResource.class);
    register(ExecutionResource.class);
    register(ResultResource.class);
    register(StateResource.class);
    register(AnnotatedTableResource.class);
    register(CsvExportResource.class);
    register(RdfExportResource.class);
    register(EntitiesResource.class);
    register(BasesResource.class);
    register(FormatResource.class);
    register(UsersResource.class);

    // Filters registration
    register(RequestContextFilter.class);
    register(LoggingResponseFilter.class);
    register(CorsResponseFilter.class);
    register(AuthenticationFilter.class);
    register(AuthorizationFilter.class);
    
    // Features registration
    register(JacksonFeature.class);
    register(MultiPartFeature.class);
    
    // Exception mappers registration
    register(ThrowableMapper.class);
    
    // Prevent the container to interfere with the error entities. 
    property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
  }
}
