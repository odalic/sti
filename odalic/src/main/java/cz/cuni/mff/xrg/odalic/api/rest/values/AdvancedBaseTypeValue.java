package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.bases.AdvancedBaseType;

/**
 * Domain class {@link AdvancedBaseType} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "advancedBaseType")
public final class AdvancedBaseTypeValue implements Serializable {

  private static final long serialVersionUID = -7968455903789693405L;
  
  private String name;

  private Set<String> keys;
  private Map<String, String> keysToDefaultValues;
  private Map<String, String> keysToComments;
  
  public AdvancedBaseTypeValue() {
    this.keys = ImmutableSet.of();
    this.keysToDefaultValues = ImmutableMap.of();
    this.keysToComments = ImmutableMap.of();
  }
  
  public AdvancedBaseTypeValue(final AdvancedBaseType adaptee) {
    this.name = adaptee.getName();
    
    this.keys = ImmutableSet.copyOf(adaptee.getKeys());
    this.keysToDefaultValues = ImmutableMap.copyOf(adaptee.getKeysToDefaultValues());
    this.keysToComments = ImmutableMap.copyOf(adaptee.getKeysToComments());
  }

  /**
   * @return the name
   */
  @XmlElement
  @Nullable
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(final String name) {
    Preconditions.checkNotNull(name);
    
    this.name = name;
  }

  /**
   * @return the keys
   */
  @XmlElement
  @Nullable
  public Set<String> getKeys() {
    return keys;
  }

  /**
   * @param keys the keys to set
   */
  public void setKeys(final Set<String> keys) {
    Preconditions.checkNotNull(keys);
    
    this.keys = ImmutableSet.copyOf(keys);
  }

  /**
   * @return the keysToDefaultValues
   */
  @XmlElement
  @Nullable
  public Map<String, String> getKeysToDefaultValues() {
    return keysToDefaultValues;
  }

  /**
   * @param keysToDefaultValues the keysToDefaultValues to set
   */
  public void setKeysToDefaultValues(final Map<String, String> keysToDefaultValues) {
    Preconditions.checkNotNull(keysToDefaultValues);
    
    this.keysToDefaultValues = ImmutableMap.copyOf(keysToDefaultValues);
  }

  /**
   * @return the keysToComments
   */
  @XmlElement
  @Nullable
  public Map<String, String> getKeysToComments() {
    return keysToComments;
  }

  /**
   * @param keysToComments the keysToComments to set
   */
  public void setKeysToComments(final Map<String, String> keysToComments) {
    Preconditions.checkNotNull(keysToComments);
    
    this.keysToComments = ImmutableMap.copyOf(keysToComments);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AdvancedBaseTypeValue [name=" + name + ", keys=" + keys + ", keysToDefaultValues="
        + keysToDefaultValues + ", keysToComments=" + keysToComments + "]";
  }
}
