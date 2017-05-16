/**
 *
 */
package cz.cuni.mff.xrg.odalic.api.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.BadRequestException;

import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import com.complexible.pinto.MappingOptions;
import com.complexible.pinto.RDFMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rdf.values.AdvancedPropertyEntry;
import cz.cuni.mff.xrg.odalic.api.rdf.values.GroupValue;
import cz.cuni.mff.xrg.odalic.api.rdf.values.KnowledgeBaseValue;
import cz.cuni.mff.xrg.odalic.bases.AdvancedBaseTypesService;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.bases.TextSearchingMethod;
import cz.cuni.mff.xrg.odalic.groups.Group;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * <p>
 * A {@link KnowledgeBaseSerializationService} implementation employing {@link RDFMapper}.
 * </p>
 * 
 * <p>
 * Apart from the equivalent classes for the REST API values, the adapters are not available, so the
 * conversion from mapped "values" to domain objects and back is unfortunately strictly manual.
 * </p>
 *
 * @author Václav Brodec
 *
 */
public class TurtleRdfMappingKnowledgeBaseSerializationService implements KnowledgeBaseSerializationService {

  private static final String VERSIONED_SERIALIZED_TASK_URI_SUFFIX_FORMAT = "SerializedKnowledgeBase/V2/%s";

  private static String format(final Model model) {
    final StringWriter stringWriter = new StringWriter();

    Rio.write(model, stringWriter, RDFFormat.TURTLE);

    return stringWriter.toString();
  }

  private static IRI getRootObject() {
    return SimpleValueFactory.getInstance().createIRI("http://odalic.eu/internal/KnowledgeBase");
  }

  private static Resource getRootResource(final Model model) {
    final Model rootModel = model.filter((Resource) null, (IRI) null, getRootObject());
    Preconditions.checkArgument(!rootModel.isEmpty(), "Missing root resource!");

    return rootModel.iterator().next().getSubject();
  }

  private static IRI getRootSubjectIri(final URI baseUri) {
    return SimpleValueFactory.getInstance()
        .createIRI(baseUri
            .resolve(String.format(VERSIONED_SERIALIZED_TASK_URI_SUFFIX_FORMAT, UUID.randomUUID()))
            .toString());
  }

  private static Model parse(final InputStream knowledgeBaseStream) throws IOException {
    final Model model;
    try {
      model = Rio.parse(knowledgeBaseStream, "http://odalic.eu/internal/", RDFFormat.TURTLE);
    } catch (final RDFParseException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return model;
  }

  private static void setRootSubjectIdentifier(final URI baseUri, final KnowledgeBaseValue knowledgeBaseValue) {
    knowledgeBaseValue.id(getRootSubjectIri(baseUri));
  }

  private static KnowledgeBaseValue toProxy(final KnowledgeBase knowledgeBase) {
    return new KnowledgeBaseValue(knowledgeBase);
  }

  private final RDFMapper.Builder rdfMapperBuilder;

  private final UserService userService;

  private final AdvancedBaseTypesService advancedBaseTypesService;

  private final GroupsService groupsService;

  public TurtleRdfMappingKnowledgeBaseSerializationService(final RDFMapper.Builder rdfMapperBuilder,
      final UserService userService, final AdvancedBaseTypesService advancedBaseTypesService,
      final GroupsService groupsService) {
    Preconditions.checkNotNull(rdfMapperBuilder);
    Preconditions.checkNotNull(userService);
    Preconditions.checkNotNull(advancedBaseTypesService);
    Preconditions.checkNotNull(groupsService);
    
    this.rdfMapperBuilder = rdfMapperBuilder;
    this.userService = userService;
    this.advancedBaseTypesService = advancedBaseTypesService;
    this.groupsService = groupsService;
  }

  @Autowired
  public TurtleRdfMappingKnowledgeBaseSerializationService(final UserService userService, final AdvancedBaseTypesService advancedBaseTypesService, final GroupsService groupsService) {
    this(
        RDFMapper.builder().set(MappingOptions.IGNORE_CARDINALITY_VIOLATIONS, false)
            .set(MappingOptions.IGNORE_INVALID_ANNOTATIONS, false),
        userService, advancedBaseTypesService, groupsService);
  }

  private RDFMapper buildMapper(final URI baseUri) {
    return this.rdfMapperBuilder.namespace("", baseUri.resolve("SerializedKnowledgeBase/Node/").toString())
        .build();
  }

  @Override
  public KnowledgeBase deserialize(final InputStream knowledgeBaseStream, final String userId, final String knowledgeBaseId,
      final URI baseUri) throws IOException {
    final Model model = parse(knowledgeBaseStream);

    final KnowledgeBaseValue knowledgeBaseValue;
    try {
      knowledgeBaseValue = fromModel(baseUri, model);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    final KnowledgeBase knowledgeBase = fromProxies(userId, knowledgeBaseId, knowledgeBaseValue);

    return knowledgeBase;
  }

  private KnowledgeBaseValue fromModel(final URI baseUri, final Model model) {
    return buildMapper(baseUri).readValue(model, KnowledgeBaseValue.class, getRootResource(model));
  }

  private KnowledgeBase fromProxies(final String userId, final String knowledgeBaseId, final KnowledgeBaseValue knowledgeBaseValue) {
    final User owner = this.userService.getUser(userId);
    
    final Set<Group> selectedGroups = extractSelectedGroups(knowledgeBaseValue, owner);
    final Map<String, String> advancedProperties = extractAdvancedProperties(knowledgeBaseValue);
    
    try {
      return new KnowledgeBase(owner, knowledgeBaseId, new URL(knowledgeBaseValue.getEndpoint()), knowledgeBaseValue.getDescription(), TextSearchingMethod.valueOf(knowledgeBaseValue.getTextSearchingMethod()), knowledgeBaseValue.getLanguageTag(), knowledgeBaseValue.getSkippedAttributes(), knowledgeBaseValue.getSkippedClasses(), knowledgeBaseValue.getGroupsAutoSelected(), selectedGroups, knowledgeBaseValue.isInsertEnabled(), URI.create(knowledgeBaseValue.getInsertGraph()), URI.create(knowledgeBaseValue.getUserClassesPrefix()), URI.create(knowledgeBaseValue.getUserResourcesPrefix()), this.advancedBaseTypesService.getType(knowledgeBaseValue.getAdvancedType()), advancedProperties);
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private Set<Group> extractSelectedGroups(final KnowledgeBaseValue knowledgeBaseValue,
      final User owner) {
    final ImmutableSet.Builder<Group> selectedGroupsBuilder = ImmutableSet.builder();
    for (final GroupValue groupValue : knowledgeBaseValue.getSelectedGroups()) {
      final Group group = new Group(owner, groupValue.getId(), groupValue.getLabelPredicates(), groupValue.getDescriptionPredicates(), groupValue.getInstanceOfPredicates(), groupValue.getClassTypes(), groupValue.getPropertyTypes());
      
      this.groupsService.merge(group);
      
      selectedGroupsBuilder.add(group);
    }
    final Set<Group> selectedGroups = selectedGroupsBuilder.build();
    return selectedGroups;
  }

  private Map<String, String> extractAdvancedProperties(
      final KnowledgeBaseValue knowledgeBaseValue) {
    final ImmutableMap.Builder<String, String> advancedPropertiesBuilder =
        ImmutableMap.builder();
    for (final AdvancedPropertyEntry entry : knowledgeBaseValue.getAdvancedProperties()) {
      advancedPropertiesBuilder.put(entry.getKey(), entry.getValue());
    }
    final Map<String, String> advancedProperties = advancedPropertiesBuilder.build();
    return advancedProperties;
  }

  @Override
  public String serialize(final KnowledgeBase knowledgeBase, final URI baseUri) {
    Preconditions.checkNotNull(knowledgeBase);
    Preconditions.checkNotNull(baseUri);

    final KnowledgeBaseValue knowledgeBaseValue = toProxy(knowledgeBase);

    setRootSubjectIdentifier(baseUri, knowledgeBaseValue);

    final Model model = toModel(knowledgeBaseValue, baseUri);
    return format(model);
  }

  private Model toModel(final KnowledgeBaseValue proxy, final URI baseUri) {
    return buildMapper(baseUri).writeValue(proxy);
  }

  @Override
  public KnowledgeBase deserialize(final String userId, final KnowledgeBaseValue knowledgeBaseValue) {
    return fromProxies(userId, knowledgeBaseValue.getName(), knowledgeBaseValue);
  }
}
