/**
 *
 */
package cz.cuni.mff.xrg.odalic.bases;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Default {@link AdvancedBaseTypesService} implementation.
 *
 */
public final class MemoryOnlyAdvancedBaseTypesService implements AdvancedBaseTypesService {

  public static final String SPARQL_BASE_TYPE_NAME = "SPARQL";

  public static final AdvancedBaseType SPARQL_BASE_TYPE = new AdvancedBaseType(SPARQL_BASE_TYPE_NAME, ImmutableSet.of(), ImmutableMap.of(), ImmutableMap.of());
  
  private final Map<? extends String, ? extends AdvancedBaseType> types;
  
  public MemoryOnlyAdvancedBaseTypesService() {
    this(ImmutableSet.of(SPARQL_BASE_TYPE));
  }
  
  private MemoryOnlyAdvancedBaseTypesService(final Set<? extends AdvancedBaseType> types) {
    this(types.stream().collect(ImmutableMap.toImmutableMap(e -> e.getName(), Function.identity())));
  }
  
  private MemoryOnlyAdvancedBaseTypesService(final Map<? extends String, ? extends AdvancedBaseType> types) {
    Preconditions.checkNotNull(types);
    
    this.types = types;
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
}
