package cz.cuni.mff.xrg.odalic.groups;

import java.io.Serializable;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cz.cuni.mff.xrg.odalic.api.rest.adapters.GroupAdapter;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * Predicates and class group.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(GroupAdapter.class)
public final class Group implements Serializable {

  private static final long serialVersionUID = -4616072016202865174L;
  
  private final User owner; 
  private final String id;

  private final List<String> labelPredicates;
  private final List<String> descriptionPredicates;
  private final List<String> instanceOfPredicates;
  private final List<String> classTypes;
  private final List<String> propertyTypes;
  
  public Group(final User owner, final String id, final List<? extends String> labelPredicates,
      final List<? extends String> descriptionPredicates, final List<? extends String> instanceOfPredicates,
          final List<? extends String> classTypes, final List<? extends String> propertyTypes) {
    Preconditions.checkNotNull(owner);
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(labelPredicates);
    Preconditions.checkNotNull(descriptionPredicates);
    Preconditions.checkNotNull(instanceOfPredicates);
    Preconditions.checkNotNull(classTypes);
    Preconditions.checkNotNull(propertyTypes);
    
    Preconditions.checkArgument(!id.isEmpty(), "The name is empty!");
    Preconditions.checkArgument(!labelPredicates.isEmpty(), "No label predicates provided!");
    Preconditions.checkArgument(!descriptionPredicates.isEmpty(), "No description predicates provided!");
    Preconditions.checkArgument(!instanceOfPredicates.isEmpty(), "No instance predicates provided!");
    Preconditions.checkArgument(!classTypes.isEmpty(), "No class predicates provided!");
    Preconditions.checkArgument(!propertyTypes.isEmpty(), "No property predicates provided!");

    this.owner = owner;
    this.id = id;
    this.labelPredicates = ImmutableList.copyOf(labelPredicates);
    this.descriptionPredicates = ImmutableList.copyOf(descriptionPredicates);
    this.instanceOfPredicates = ImmutableList.copyOf(instanceOfPredicates);
    this.classTypes = ImmutableList.copyOf(classTypes);
    this.propertyTypes = ImmutableList.copyOf(propertyTypes);
  }

  /**
   * @return the owner
   */
  public User getOwner() {
    return owner;
  }

  public String getId() {
    return id;
  }

  public List<String> getLabelPredicates() {
    return labelPredicates;
  }

  public List<String> getDescriptionPredicates() {
    return descriptionPredicates;
  }

  public List<String> getInstanceOfPredicates() {
    return instanceOfPredicates;
  }

  public List<String> getClassTypes() {
    return classTypes;
  }

  public List<String> getPropertyTypes() {
    return propertyTypes;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((owner == null) ? 0 : owner.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    Group other = (Group) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (owner == null) {
      if (other.owner != null) {
        return false;
      }
    } else if (!owner.equals(other.owner)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Group [owner=" + owner + ", id=" + id + ", labelPredicates=" + labelPredicates
        + ", descriptionPredicates=" + descriptionPredicates + ", instanceOfPredicates="
        + instanceOfPredicates + ", classTypes=" + classTypes + ", propertyTypes=" + propertyTypes
        + "]";
  }
}
