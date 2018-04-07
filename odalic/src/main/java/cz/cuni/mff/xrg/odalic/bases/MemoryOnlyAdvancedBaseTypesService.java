/**
 *
 */
package cz.cuni.mff.xrg.odalic.bases;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import cz.cuni.mff.xrg.odalic.bases.types.PPKnowledgeBaseDefinitionFactory;
import cz.cuni.mff.xrg.odalic.bases.types.SparqlKnowledgeBaseDefinitionFactory;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.DefaultPostProcessorFactory;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.ExtraRelatablePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.shef.dcs.kbproxy.ProxyDefinition;
import java.util.Map;
import java.util.SortedSet;
import static uk.ac.shef.dcs.kbproxy.sparql.pp.PPProxyDefinition.*;

/**
 * Default {@link AdvancedBaseTypesService} implementation.
 *
 */
public final class MemoryOnlyAdvancedBaseTypesService implements AdvancedBaseTypesService {

  public static final String SPARQL_BASE_TYPE_NAME = "SPARQL";
  public static final AdvancedBaseType SPARQL_BASE_TYPE = new DefaultAdvancedBaseTypeFactory().createRegular(
      SPARQL_BASE_TYPE_NAME, ImmutableSet.of(), ImmutableMap.of(), ImmutableMap.of());

  public static final String PP_BASE_TYPE_NAME = "PoolParty";
  public static final AdvancedBaseType PP_BASE_TYPE = new DefaultAdvancedBaseTypeFactory().createRegular(PP_BASE_TYPE_NAME,
      ImmutableSet.of(POOLPARTY_SERVER_URL, POOLPARTY_PROJECT_ID, POOLPARTY_ONTOLOGY_URL,
          POOLPARTY_CUSTOM_SCHEMA_URL, POOLPARTY_CONCEPT_SCHEMA_PROPOSED_URL),
      ImmutableMap.of(POOLPARTY_SERVER_URL, "http://adequate-project-pp.semantic-web.at/PoolParty",
          POOLPARTY_PROJECT_ID, "1DDFF124-EE5B-0001-B0C2-1F8031F51970", POOLPARTY_ONTOLOGY_URL,
          "http://adequate-project-pp.semantic-web.at/ADEQUATe-test", POOLPARTY_CUSTOM_SCHEMA_URL,
          "http://adequate-project-pp.semantic-web.at/ADEQUATe-test-scheme",
          POOLPARTY_CONCEPT_SCHEMA_PROPOSED_URL,
          "http://adequate-project-pp.semantic-web.at/ADEQUATe_KB/b39c6dab-2bf2-4788-aff0-98f2fbab4b54"),
      ImmutableMap.of(POOLPARTY_SERVER_URL, "A PoolParty Thesaurus Server url",
          POOLPARTY_PROJECT_ID, "A project in the PoolParty Thesaurus Manager",
          POOLPARTY_ONTOLOGY_URL, "Url for the ontology", POOLPARTY_CUSTOM_SCHEMA_URL,
          "Url for the custom schema", POOLPARTY_CONCEPT_SCHEMA_PROPOSED_URL,
          "Concept Schema to which new concepts are proposed"));
  
  private static final ImmutableSet<String> EXTRARELATABLE_KEYS = ImmutableSet.of(DefaultPostProcessorFactory.POST_PROCESSING_ENABLED_KEY, DefaultPostProcessorFactory.POST_PROCESSORS_LIST_KEY,
      ExtraRelatablePostProcessor.ENDPOINT_PARAMETER_KEY,
      ExtraRelatablePostProcessor.LANGUAGE_TAG_PARAMETER_KEY,
      ExtraRelatablePostProcessor.LEARN_ANNOTATED_PARAMETER_KEY);
  private static final ImmutableMap<String, String> EXTRARELATABLE_DEFAULTS = ImmutableMap.of(DefaultPostProcessorFactory.POST_PROCESSING_ENABLED_KEY, "true",
      DefaultPostProcessorFactory.POST_PROCESSORS_LIST_KEY, DefaultPostProcessorFactory.EXTRA_RELATABLE_POST_PROCESSOR_NAME,
      ExtraRelatablePostProcessor.LANGUAGE_TAG_PARAMETER_KEY, "en",
      ExtraRelatablePostProcessor.LEARN_ANNOTATED_PARAMETER_KEY, "false");
  private static final ImmutableMap<String, String> EXTRARELATABLE_COMMENTS = ImmutableMap.of(DefaultPostProcessorFactory.POST_PROCESSING_ENABLED_KEY, "Enter true to enable the post-processors, other values to disable.",
      DefaultPostProcessorFactory.POST_PROCESSORS_LIST_KEY, "A " + DefaultPostProcessorFactory.POST_PROCESSORS_LIST_SEPARATOR + "-separated list of the names of the post-processors.",
      ExtraRelatablePostProcessor.ENDPOINT_PARAMETER_KEY, "Endpoint URI of an ExtraRelatable instance.",
      ExtraRelatablePostProcessor.LANGUAGE_TAG_PARAMETER_KEY, "Language tag of the content.",
      ExtraRelatablePostProcessor.LEARN_ANNOTATED_PARAMETER_KEY, "Enter true to enable learning of every file annotated thgrough the Odalic.");
  
  public static final String PP_ER_BASE_TYPE_NAME = "PoolParty_ER";
  public static final AdvancedBaseType PP_ER_BASE_TYPE = new DefaultAdvancedBaseTypeFactory().createPostProcessable(
      PP_BASE_TYPE, PP_ER_BASE_TYPE_NAME,
      EXTRARELATABLE_KEYS,
      EXTRARELATABLE_DEFAULTS,
      EXTRARELATABLE_COMMENTS
      );
  
  public static final String SPARQL_ER_BASE_TYPE_NAME = "SPARQL_ER";
  public static final AdvancedBaseType SPARQL_ER_BASE_TYPE = new DefaultAdvancedBaseTypeFactory().createPostProcessable(
      SPARQL_BASE_TYPE, SPARQL_ER_BASE_TYPE_NAME,
      EXTRARELATABLE_KEYS,
      EXTRARELATABLE_DEFAULTS,
      EXTRARELATABLE_COMMENTS
      );

  private final GroupsService groupsService;

  private final Map<? extends String, ? extends AdvancedBaseType> types;

  private final Map<? extends AdvancedBaseType, ? extends ProxyDefinitionFactory> typesToDefinitionFactories;


  @Autowired
  public MemoryOnlyAdvancedBaseTypesService(final GroupsService groupsService) {
    this(groupsService,
        ImmutableMap.of(SPARQL_BASE_TYPE_NAME, SPARQL_BASE_TYPE, PP_BASE_TYPE_NAME, PP_BASE_TYPE, SPARQL_ER_BASE_TYPE_NAME, SPARQL_ER_BASE_TYPE, PP_ER_BASE_TYPE_NAME, PP_ER_BASE_TYPE),
        ImmutableMap.of(SPARQL_BASE_TYPE, new SparqlKnowledgeBaseDefinitionFactory(), PP_BASE_TYPE,
            new PPKnowledgeBaseDefinitionFactory(), SPARQL_ER_BASE_TYPE, new SparqlKnowledgeBaseDefinitionFactory(), PP_ER_BASE_TYPE, new PPKnowledgeBaseDefinitionFactory()));
  }

  private MemoryOnlyAdvancedBaseTypesService(final GroupsService groupsService,
      final Map<? extends String, ? extends AdvancedBaseType> types,
      final Map<? extends AdvancedBaseType, ? extends ProxyDefinitionFactory> typesToDefinitionFactories) {
    Preconditions.checkNotNull(groupsService, "The groupsService cannot be null!");
    Preconditions.checkNotNull(types, "The types cannot be null!");
    Preconditions.checkNotNull(typesToDefinitionFactories,
        "The typesToDefinitionFactories cannot be null!");

    this.groupsService = groupsService;
    this.types = types;
    this.typesToDefinitionFactories = typesToDefinitionFactories;
  }

  @Override
  public SortedSet<AdvancedBaseType> getTypes() {
    return ImmutableSortedSet.copyOf(this.types.values());
  }

  @Override
  public AdvancedBaseType getType(final String name) {
    Preconditions.checkNotNull(name, "The name cannot be null!");

    final AdvancedBaseType type = this.types.get(name);
    Preconditions.checkArgument(type != null, "Unknown advanced base type!");

    return type;
  }

  @Override
  public AdvancedBaseType verifyTypeExistenceByName(String name) {
    Preconditions.checkNotNull(name, "The name cannot be null!");

    return this.types.get(name);
  }

  @Override
  public ProxyDefinition toProxyDefinition(final KnowledgeBase base) {
    Preconditions.checkArgument(!base.getAdvancedType().isPostProcessing(),
        String.format("The type of %s is intended for post-processing only!", base));

    final ProxyDefinitionFactory factory =
        this.typesToDefinitionFactories.get(base.getAdvancedType());
    Preconditions.checkArgument(factory != null,
        String.format("The type of %s has no definition factory assigned!", base));

    return factory.create(base, this.groupsService.getGroups(base.getOwner().getEmail()));
  }

  @Override
  public AdvancedBaseType getDefault() {
    return SPARQL_BASE_TYPE;
  }
}
