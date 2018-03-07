/**
 *
 */
package cz.cuni.mff.xrg.odalic.bases;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import cz.cuni.mff.xrg.odalic.bases.types.ExtraRelatableProxyDefinitionFactory;
import cz.cuni.mff.xrg.odalic.bases.types.PPKnowledgeBaseDefinitionFactory;
import cz.cuni.mff.xrg.odalic.bases.types.SparqlKnowledgeBaseDefinitionFactory;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;
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
  public static final AdvancedBaseType SPARQL_BASE_TYPE = new AdvancedBaseType(
      SPARQL_BASE_TYPE_NAME, ImmutableSet.of(), ImmutableMap.of(), ImmutableMap.of(), false);

  public static final String PP_BASE_TYPE_NAME = "PoolParty";
  public static final AdvancedBaseType PP_BASE_TYPE = new AdvancedBaseType(PP_BASE_TYPE_NAME,
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
          "Concept Schema to which new concepts are proposed"),
      false);

  public static final String EXTRARELATABLE_BASE_TYPE_NAME = "ExtraRelatable";
  public static final AdvancedBaseType EXTRARELATABLE_BASE_TYPE =
      new AdvancedBaseType(EXTRARELATABLE_BASE_TYPE_NAME,
          ImmutableSet.of(ExtraRelatablePostProcessor.LEARN_ANNOTATED_PARAMETER_KEY),
          ImmutableMap.of(ExtraRelatablePostProcessor.LEARN_ANNOTATED_PARAMETER_KEY, "false"),
          ImmutableMap.of(ExtraRelatablePostProcessor.LEARN_ANNOTATED_PARAMETER_KEY,
              "Allow the processor to learn from every input."),
          true);

  private final GroupsService groupsService;

  private final Map<? extends String, ? extends AdvancedBaseType> types;

  private final Map<? extends AdvancedBaseType, ? extends ProxyDefinitionFactory> typesToDefinitionFactories;


  @Autowired
  public MemoryOnlyAdvancedBaseTypesService(final GroupsService groupsService) {
    this(groupsService,
        ImmutableMap.of(SPARQL_BASE_TYPE_NAME, SPARQL_BASE_TYPE, PP_BASE_TYPE_NAME, PP_BASE_TYPE,
            EXTRARELATABLE_BASE_TYPE_NAME, EXTRARELATABLE_BASE_TYPE),
        ImmutableMap.of(SPARQL_BASE_TYPE, new SparqlKnowledgeBaseDefinitionFactory(), PP_BASE_TYPE,
            new PPKnowledgeBaseDefinitionFactory(), EXTRARELATABLE_BASE_TYPE,
            new ExtraRelatableProxyDefinitionFactory()));
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
