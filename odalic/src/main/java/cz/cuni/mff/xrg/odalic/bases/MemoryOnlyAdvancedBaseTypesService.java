/**
 *
 */
package cz.cuni.mff.xrg.odalic.bases;

import java.util.Map;
import java.util.SortedSet;

import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.bases.types.SparqlKnowledgeBaseDefinitionFactory;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;
import uk.ac.shef.dcs.kbproxy.ProxyDefinition;

/**
 * Default {@link AdvancedBaseTypesService} implementation.
 *
 */
public final class MemoryOnlyAdvancedBaseTypesService implements AdvancedBaseTypesService {

  public static final String SPARQL_BASE_TYPE_NAME = "SPARQL";

  public static final AdvancedBaseType SPARQL_BASE_TYPE = new AdvancedBaseType(SPARQL_BASE_TYPE_NAME, ImmutableSet.of(), ImmutableMap.of(), ImmutableMap.of());
  
  private final GroupsService groupsService;
  
  private final Map<? extends String, ? extends AdvancedBaseType> types;
  
  private final Map<? extends AdvancedBaseType, ? extends ProxyDefinitionFactory> typesToDefinitionFactories;
  

  @Autowired
  public MemoryOnlyAdvancedBaseTypesService(final GroupsService groupsService) {
    this(groupsService, ImmutableMap.of(SPARQL_BASE_TYPE_NAME, SPARQL_BASE_TYPE), ImmutableMap.of(SPARQL_BASE_TYPE, new SparqlKnowledgeBaseDefinitionFactory()));
  }
  
  private MemoryOnlyAdvancedBaseTypesService(final GroupsService groupsService, final Map<? extends String, ? extends AdvancedBaseType> types, final Map<? extends AdvancedBaseType, ? extends ProxyDefinitionFactory> typesToDefinitionFactories) {
    Preconditions.checkNotNull(groupsService);
    Preconditions.checkNotNull(types);
    Preconditions.checkNotNull(typesToDefinitionFactories);
    
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
    Preconditions.checkNotNull(name);
    
    final AdvancedBaseType type = this.types.get(name);
    Preconditions.checkArgument(type != null, "Unknown advanced base type!");

    return type;
  }

  @Override
  public AdvancedBaseType verifyTypeExistenceByName(String name) {
    Preconditions.checkNotNull(name);

    return this.types.get(name);
  }
  
  @Override
  public ProxyDefinition toProxyDefinition(final KnowledgeBase base) {
    final ProxyDefinitionFactory factory = this.typesToDefinitionFactories.get(base.getAdvancedType());
    Preconditions.checkArgument(factory != null, String.format("The type %s has no definition factory assigned!", base));
    
    return factory.create(base, this.groupsService.getGroups(base.getOwner().getEmail()));
  }

  @Override
  public AdvancedBaseType getDefault() {
    return SPARQL_BASE_TYPE;
  }
}
