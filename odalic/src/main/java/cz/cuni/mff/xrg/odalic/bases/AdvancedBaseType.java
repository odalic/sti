/**
 * 
 */
package cz.cuni.mff.xrg.odalic.bases;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import cz.cuni.mff.xrg.odalic.api.rest.adapters.AdvancedBaseTypeAdapter;

/**
 * Knowledge base type determining the advanced keys.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(AdvancedBaseTypeAdapter.class)
public final class AdvancedBaseType implements Serializable {

  private static final long serialVersionUID = -5918981181938425624L;

  private final String name;

  private final Set<String> keys;

  private final Map<String, String> keysToDefaultValues;

  private final Map<String, String> keysToComments;

  public AdvancedBaseType(String name, Set<String> keys, Map<String, String> keysToDefaultValues,
      Map<String, String> keysToComments) {
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(keys);
    Preconditions.checkNotNull(keysToDefaultValues);
    Preconditions.checkNotNull(keysToComments);

    Preconditions.checkArgument(!name.isEmpty(), "The advanced base type name cannot be empty!");
    Preconditions.checkArgument(keys.containsAll(keysToDefaultValues.keySet()),
        "The key set of the map from keys to default values must be a subset of the keys!");
    Preconditions.checkArgument(keys.containsAll(keysToComments.keySet()),
        "The key set of the map from keys to comments must be a subset of the keys!");

    this.name = name;
    this.keys = ImmutableSet.copyOf(keys);
    this.keysToDefaultValues = ImmutableMap.copyOf(keysToDefaultValues);
    this.keysToComments = ImmutableMap.copyOf(keysToComments);
  }

  public String getName() {
    return name;
  }

  public Set<String> getKeys() {
    return keys;
  }

  public Map<String, String> getKeysToDefaultValues() {
    return keysToDefaultValues;
  }

  public Map<String, String> getKeysToComments() {
    return keysToComments;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AdvancedBaseType other = (AdvancedBaseType) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "AdvancedBaseType [name=" + name + ", keys=" + keys + ", keysToDefaultValues="
        + keysToDefaultValues + ", keysToComments=" + keysToComments + "]";
  }
}
