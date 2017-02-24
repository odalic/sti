package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.complexible.pinto.RDFMapper;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Works around the limitation of {@link RDFMapper} in mapping of nested {@link Map}s.
 * 
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/EntityCandidateNavigableSetWrapper")
public class EntityCandidateNavigableSetWrapper {

  private NavigableSet<EntityCandidateValue> value;
  
  public EntityCandidateNavigableSetWrapper() {
    value = ImmutableSortedSet.of();
  }
  
  public EntityCandidateNavigableSetWrapper(final Set<? extends EntityCandidateValue> value) {
    Preconditions.checkNotNull(value);
    
    this.value = ImmutableSortedSet.copyOf(value);
  }

  /**
   * @return the value
   */
  @RdfProperty("http://odalic.eu/internal/EntityCandidateNavigableSetWrapper/value")
  @Nullable
  public NavigableSet<EntityCandidateValue> getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(Set<? extends EntityCandidateValue> value) {
    Preconditions.checkNotNull(value);
    
    this.value = ImmutableSortedSet.copyOf(value);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "EntityCandidateNavigableSetWrapper [value=" + value + "]";
  }
}
