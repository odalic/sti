package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import cz.cuni.mff.xrg.odalic.groups.Group;

/**
 * Domain class {@link Group} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "group")
public final class GroupValue implements Serializable {

  private static final long serialVersionUID = -7968455903789693405L;
  
  private String id;

  private List<String> labelPredicates;
  private List<String> descriptionPredicates;
  private List<String> instanceOfPredicates;
  private List<String> classTypes;
  private List<String> propertyTypes;
  
  public GroupValue() {
    this.labelPredicates = ImmutableList.of();
    this.descriptionPredicates = ImmutableList.of();
    this.instanceOfPredicates = ImmutableList.of();
    this.classTypes = ImmutableList.of();
    this.propertyTypes = ImmutableList.of();
  }
  
  public GroupValue(final Group adaptee) {
    this.id = adaptee.getId();
    
    this.labelPredicates = ImmutableList.copyOf(adaptee.getLabelPredicates());
    this.descriptionPredicates = ImmutableList.copyOf(adaptee.getDescriptionPredicates());
    this.instanceOfPredicates = ImmutableList.copyOf(adaptee.getInstanceOfPredicates());
    this.classTypes = ImmutableList.copyOf(adaptee.getClassTypes());
    this.propertyTypes = ImmutableList.copyOf(adaptee.getPropertyTypes());
  }

  /**
   * @return the ID
   */
  @XmlElement
  @Nullable
  public String getId() {
    return id;
  }

  /**
   * @param id the ID to set
   */
  public void setId(String id) {
    Preconditions.checkNotNull(id, "The id cannot be null!");
    
    this.id = id;
  }

  /**
   * @return the label predicates
   */
  @XmlElement
  @Nullable
  public List<String> getLabelPredicates() {
    return labelPredicates;
  }

  /**
   * @param labelPredicates the labelPredicates to set
   */
  public void setLabelPredicates(final List<String> labelPredicates) {
    Preconditions.checkNotNull(labelPredicates, "The labelPredicates cannot be null!");
    
    this.labelPredicates = ImmutableList.copyOf(labelPredicates);
  }

  /**
   * @return the descriptionPredicates
   */
  @XmlElement
  @Nullable
  public List<String> getDescriptionPredicates() {
    return descriptionPredicates;
  }

  /**
   * @param descriptionPredicates the descriptionPredicates to set
   */
  public void setDescriptionPredicates(final List<String> descriptionPredicates) {
    Preconditions.checkNotNull(descriptionPredicates, "The descriptionPredicates cannot be null!");
    
    this.descriptionPredicates = ImmutableList.copyOf(descriptionPredicates);
  }

  /**
   * @return the instanceOfPredicates
   */
  @XmlElement
  @Nullable
  public List<String> getInstanceOfPredicates() {
    return instanceOfPredicates;
  }

  /**
   * @param instanceOfPredicates the instanceOfPredicates to set
   */
  public void setInstanceOfPredicates(final List<String> instanceOfPredicates) {
    Preconditions.checkNotNull(instanceOfPredicates, "The instanceOfPredicates cannot be null!");
    
    this.instanceOfPredicates = ImmutableList.copyOf(instanceOfPredicates);
  }

  /**
   * @return the classTypes
   */
  @XmlElement
  @Nullable
  public List<String> getClassTypes() {
    return classTypes;
  }

  /**
   * @param classTypes the classTypes to set
   */
  public void setClassTypes(final List<String> classTypes) {
    Preconditions.checkNotNull(classTypes, "The classTypes cannot be null!");
    
    this.classTypes = ImmutableList.copyOf(classTypes);
  }

  /**
   * @return the propertyTypes
   */
  @XmlElement
  @Nullable
  public List<String> getPropertyTypes() {
    return propertyTypes;
  }

  /**
   * @param propertyTypes the propertyTypes to set
   */
  public void setPropertyTypes(final List<String> propertyTypes) {
    Preconditions.checkNotNull(propertyTypes, "The propertyTypes cannot be null!");
    
    this.propertyTypes = ImmutableList.copyOf(propertyTypes);
  }

  @Override
  public String toString() {
    return "GroupValue [id=" + id + ", labelPredicates=" + labelPredicates
        + ", descriptionPredicates=" + descriptionPredicates + ", instanceOfPredicates="
        + instanceOfPredicates + ", classTypes=" + classTypes + ", propertyTypes=" + propertyTypes
        + "]";
  }
}
