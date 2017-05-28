package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.util.Map;
import java.util.Set;

import com.complexible.pinto.RDFMapper;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Works around the limitation of {@link RDFMapper} in mapping of nested {@link Map}s.
 *
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/EntityCandidateSetWrapper")
public class EntityCandidateSetWrapper {

  private Set<EntityCandidateValue> value;

  public EntityCandidateSetWrapper() {
    this.value = ImmutableSet.of();
  }

  public EntityCandidateSetWrapper(final Set<? extends EntityCandidateValue> value) {
    Preconditions.checkNotNull(value, "The value cannot be null!");

    this.value = ImmutableSortedSet.copyOf(value);
  }

  /**
   * @return the value
   */
  @RdfProperty("http://odalic.eu/internal/EntityCandidateSetWrapper/value")
  public Set<EntityCandidateValue> getValue() {
    return this.value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final Set<? extends EntityCandidateValue> value) {
    Preconditions.checkNotNull(value, "The value cannot be null!");

    this.value = ImmutableSortedSet.copyOf(value);
  }

  @Override
  public String toString() {
    return "EntityCandidateSetWrapper [value=" + this.value + "]";
  }
}
