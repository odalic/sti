package cz.cuni.mff.xrg.odalic.tasks.annotations;

import java.io.Serializable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.EntityAdapter;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.Prefix;


/**
 * Groups the resource ID and its label in one handy class.
 * 
 * @author Václav Brodec
 */
@XmlJavaTypeAdapter(EntityAdapter.class)
public final class Entity implements Comparable<Entity>, Serializable {

  private static final long serialVersionUID = -3001706805535088480L;

  private final Prefix prefix;
  
  private final String suffix;
  
  private final String label;
  
  /**
   * Creates new entity representation.
   * 
   * @param prefix resource ID prefix
   * @param resource entity resource ID
   * @param label label
   */
  public static Entity fromResourceId(final Prefix prefix, final String resourceId, final String label) {
    Preconditions.checkNotNull(prefix);
    Preconditions.checkNotNull(resourceId);
    Preconditions.checkNotNull(label);
    
    final String prefixed = prefix.getWhat();
    Preconditions.checkArgument(resourceId.startsWith(prefixed));
    
    final String suffix = resourceId.substring(prefixed.length());
    
    return new Entity(prefix, suffix, label);
  }
  
  /**
   * Creates new entity representation (without prefix).
   * 
   * @param resource entity resource ID
   * @param label label
   */
  public static Entity fromResourceId(final String resourceId, final String label) {
    return fromResourceId(Prefix.empty(), resourceId, label);
  }
  
  /**
   * Creates new entity representation.
   * 
   * @param prefix resource ID prefix
   * @param resource entity resource ID
   * @param label label
   */
  public static Entity fromSuffix(final Prefix prefix, final String suffix, final String label) {
    Preconditions.checkNotNull(prefix);
    Preconditions.checkNotNull(suffix);
    Preconditions.checkNotNull(label);
    
    return new Entity(prefix, suffix, label);
  }
  
  private Entity(final Prefix prefix, final String suffix, String label) {
    this.prefix = prefix;
    this.suffix = suffix;
    this.label = label;
  }
  
  /**
   * @return the resource ID prefix
   */
  public Prefix getPrefix() {
    return prefix;
  }

  /**
   * @return the part of the resources ID that follows the {@link #getPrefixed()}.
   */
  public String getSuffix() {
    return suffix;
  }

  /**
   * @return the resource ID with the prefix dereferenced
   */
  public String getResource() {
    return prefix.getWhat() + suffix;
  }
  
  /**
   * @return the label
   */
  public String getLabel() {
    return label;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getResource().hashCode();
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (getClass() != object.getClass()) {
      return false;
    }
    final Entity other = (Entity) object;
    if (!getResource().equals(other.getResource())) {
      return false;
    }
    return true;
  }

  /**
   * Compares the entities by their resource ID lexicographically.
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   * @see java.lang.String#compareTo(String) for the definition of resource ID comparison
   */
  @Override
  public int compareTo(Entity o) {
    return getResource().compareTo(o.getResource());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Entity [prefix=" + prefix + ", suffix=" + suffix + ", label=" + label
        + ", getResource()=" + getResource() + "]";
  }
}
