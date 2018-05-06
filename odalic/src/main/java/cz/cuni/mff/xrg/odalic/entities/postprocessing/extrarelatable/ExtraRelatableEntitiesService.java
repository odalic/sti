package cz.cuni.mff.xrg.odalic.entities.postprocessing.extrarelatable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.net.URI;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.bases.TextSearchingMethod;
import cz.cuni.mff.xrg.odalic.entities.ClassProposal;
import cz.cuni.mff.xrg.odalic.entities.EntitiesFactory;
import cz.cuni.mff.xrg.odalic.entities.EntitiesService;
import cz.cuni.mff.xrg.odalic.entities.PropertyProposal;
import cz.cuni.mff.xrg.odalic.entities.ResourceProposal;
import cz.cuni.mff.xrg.odalic.entities.postprocessing.extrarelatable.responses.SearchReply;
import cz.cuni.mff.xrg.odalic.entities.postprocessing.extrarelatable.values.SearchResultValue;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.ExtraRelatablePostProcessor;
import uk.ac.shef.dcs.kbproxy.ProxyException;

public class ExtraRelatableEntitiesService implements EntitiesService {

  private static final URI SEARCH_SUBPATH = URI.create("search");

  private static final String PATTERN_QUERY_PARAMETER_KEY = "pattern";
  private static final String LIMIT_QUERY_PARAMTER_KEY = "limit";

  private final EntitiesFactory entitiesFactory;

  public ExtraRelatableEntitiesService(final EntitiesFactory entitiesFactory) {
    checkNotNull(entitiesFactory);

    this.entitiesFactory = entitiesFactory;
  }

  @Override
  public Entity propose(KnowledgeBase base, ClassProposal proposal) throws ProxyException {
    return null;
  }

  @Override
  public Entity propose(KnowledgeBase base, PropertyProposal proposal) throws ProxyException {
    return null; // The proposal is actually done at the time of
                                               // feedback and includes the actual range values
                                               // assigned to the property.
  }

  @Override
  public Entity propose(KnowledgeBase base, ResourceProposal proposal) throws ProxyException {
    return null;
  }

  @Override
  public NavigableSet<Entity> searchClasses(KnowledgeBase base, String query, int limit)
      throws ProxyException {
    return ImmutableSortedSet.of();
  }

  @Override
  public NavigableSet<Entity> searchProperties(KnowledgeBase base, String query, int limit,
      URI domain, URI range) throws ProxyException {
    final Map<String, String> advancedProperties = base.getAdvancedProperties();

    final String endpointValue =
        advancedProperties.get(ExtraRelatablePostProcessor.ENDPOINT_PARAMETER_KEY);
    checkArgument(endpointValue != null,
        "The endpoint URI (eu.odalic.extrarelatable.endpoint advanced type property) must be set!");
    final URI endpoint = URI.create(endpointValue);

    final String pattern;
    final TextSearchingMethod method = base.getTextSearchingMethod();
    switch (method) {
      case EXACT:
        pattern = getExactRegexPattern(query);
        break;
      case FULLTEXT:
        // Same as substring.
      case SUBSTRING:
        pattern = getSubstringRegexPattern(query);
        break;
      default:
        throw new AssertionError();
    }
    
    final SearchResultValue payload = request(endpoint.resolve(SEARCH_SUBPATH),
        ImmutableMap.of(PATTERN_QUERY_PARAMETER_KEY, pattern, LIMIT_QUERY_PARAMTER_KEY,
            Integer.valueOf(limit)),
        SearchResultValue.class);

    final URI syntheticPropertiesPath = endpoint.resolve(ExtraRelatablePostProcessor.SYNTHETIC_PROPERTIES_SUBPATH);
    
    return payload.getProperties().stream()
        .map(property -> {
          final String uriString = String.valueOf(property.getUri() == null ? syntheticPropertiesPath.resolve(URI.create(property.getUuid().toString())) : property.getUri());
          
          return this.entitiesFactory.create(uriString,
            property.getLabels().stream().findFirst().orElse("null"));
        })
        .collect(Collectors.toCollection(TreeSet::new));
  }

  private static String getSubstringRegexPattern(String query) {
    return Pattern.quote(query);
  }

  private static String getExactRegexPattern(String query) {
    return "\\A" + Pattern.quote(query) + "\\Z";
  }

  private <T, U> U request(final URI targetPath, final Map<String, Object> queryParameters,
      final Class<? extends U> responseType) {
    final Client client = ClientBuilder.newBuilder().build();

    WebTarget target = client.target(targetPath);
    for (final Map.Entry<String, Object> queryParameter : queryParameters.entrySet()) {
      target = target.queryParam(queryParameter.getKey(), queryParameter.getValue());
    }

    final Response response = target.request().accept(MediaType.APPLICATION_JSON_TYPE).get();

    if (!isSuccessful(response)) {
      throw new IllegalStateException("The request failed: " + response.getStatus() + "["
          + response.readEntity(String.class) + "]");
    }

    final MediaType responseMediaType = response.getMediaType();

    if (responseMediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
      final SearchReply reply = response.readEntity(SearchReply.class);

      return responseType.cast(reply.getPayload()); // TODO: Generalize.
    } else {
      throw new RuntimeException(response.getStatusInfo().getReasonPhrase());
    }
  }

  private static boolean isSuccessful(final Response response) {
    return response.getStatusInfo().getFamily() == Family.SUCCESSFUL;
  }

  @Override
  public NavigableSet<Entity> searchResources(KnowledgeBase base, String query, int limit)
      throws ProxyException {
    return ImmutableSortedSet.of();
  }

}
