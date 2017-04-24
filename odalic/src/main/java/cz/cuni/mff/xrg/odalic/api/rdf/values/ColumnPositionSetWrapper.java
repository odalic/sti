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
 * @author Josef Janoušek
 *
 */
@RdfsClass("http://odalic.eu/internal/ColumnPositionSetWrapper")
public class ColumnPositionSetWrapper {

  private Set<ColumnPositionValue> value;

  public ColumnPositionSetWrapper() {
    this.value = ImmutableSet.of();
  }

  public ColumnPositionSetWrapper(final Set<? extends ColumnPositionValue> value) {
    Preconditions.checkNotNull(value);

    this.value = ImmutableSortedSet.copyOf(value);
  }

  /**
   * @return the value
   */
  @RdfProperty("http://odalic.eu/internal/ColumnPositionSetWrapper/value")
  public Set<ColumnPositionValue> getValue() {
    return this.value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final Set<? extends ColumnPositionValue> value) {
    Preconditions.checkNotNull(value);

    this.value = ImmutableSortedSet.copyOf(value);
  }

  @Override
  public String toString() {
    return "ColumnPositionSetWrapper [value=" + this.value + "]";
  }
}
