package cz.cuni.mff.xrg.odalic.groups;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.users.User;

/**
 * Predicates and class group builder.
 * 
 * @author Václav Brodec
 *
 */
public final class DefaultGroupBuilder implements Serializable, GroupBuilder {

  private static final long serialVersionUID = -4735471844859238689L;
  
  private User owner; 
  private String id;

  private List<String> labelPredicates;
  private List<String> descriptionPredicates;
  private List<String> instanceOfPredicates;
  private List<String> classTypes;
  private List<String> propertyTypes;
  
  public DefaultGroupBuilder() {
    reset();
  }
  
  @Override
  public GroupBuilder reset() {
    this.owner = null;
    this.id = null;
    
    this.labelPredicates = new ArrayList<>();
    this.descriptionPredicates = new ArrayList<>();
    this.instanceOfPredicates = new ArrayList<>();
    this.classTypes = new ArrayList<>();
    this.propertyTypes = new ArrayList<>();
    
    return this;
  }
  
  @Override
  public Group build() {
    return new Group(this.owner, this.id, this.labelPredicates, this.descriptionPredicates, this.instanceOfPredicates, this.classTypes, this.propertyTypes);
  }

  /**
   * @return the owner
   */
  @Override
  @Nullable
  public User getOwner() {
    return owner;
  }

  @Override
  @Nullable
  public String getId() {
    return id;
  }

  @Override
  public List<String> getLabelPredicates() {
    return labelPredicates;
  }

  @Override
  public List<String> getDescriptionPredicates() {
    return descriptionPredicates;
  }

  @Override
  public List<String> getInstanceOfPredicates() {
    return instanceOfPredicates;
  }

  @Override
  public List<String> getClassTypes() {
    return classTypes;
  }

  @Override
  public List<String> getPropertyTypes() {
    return propertyTypes;
  }

  /**
   * @param owner the owner to set
   */
  @Override
  public GroupBuilder setOwner(@Nullable User owner) {
    this.owner = owner;

    return this;
  }

  /**
   * @param id the id to set
   */
  @Override
  public GroupBuilder setId(@Nullable String id) {
    this.id = id;

    return this;
  }

  /**
   * @param labelPredicates the labelPredicates to set
   */
  @Override
  public GroupBuilder setLabelPredicates(List<? extends String> labelPredicates) {
    this.labelPredicates = new ArrayList<>(labelPredicates);

    return this;
  }
  
  @Override
  public GroupBuilder addLabelPredicate(final String labelPredicate) {
    Preconditions.checkNotNull(labelPredicate);
    
    this.labelPredicates.add(labelPredicate);
    
    return this;
  }
  
  /**
   * @param descriptionPredicates the descriptionPredicates to set
   */
  @Override
  public GroupBuilder setDescriptionPredicates(List<? extends String> descriptionPredicates) {
    this.descriptionPredicates = new ArrayList<>(descriptionPredicates);

    return this;
  }
  
  @Override
  public GroupBuilder addDescriptionPredicate(final String descriptionPredicate) {
    Preconditions.checkNotNull(descriptionPredicate);
    
    this.descriptionPredicates.add(descriptionPredicate);
    
    return this;
  }

  /**
   * @param instanceOfPredicates the instanceOfPredicates to set
   */
  @Override
  public GroupBuilder setInstanceOfPredicates(List<? extends String> instanceOfPredicates) {
    this.instanceOfPredicates = new ArrayList<>(instanceOfPredicates);

    return this;
  }
  
  @Override
  public GroupBuilder addInstancePredicate(final String instancePredicate) {
    Preconditions.checkNotNull(instancePredicate);
    
    this.instanceOfPredicates.add(instancePredicate);
    
    return this;
  }

  /**
   * @param classTypes the classTypes to set
   */
  @Override
  public GroupBuilder setClassTypes(List<? extends String> classTypes) {
    this.classTypes = new ArrayList<>(classTypes);

    return this;
  }
  
  @Override
  public GroupBuilder addClassType(final String classType) {
    Preconditions.checkNotNull(classType);
    
    this.classTypes.add(classType);
    
    return this;
  }

  /**
   * @param propertyTypes the propertyTypes to set
   */
  @Override
  public GroupBuilder setPropertyTypes(List<String> propertyTypes) {
    this.propertyTypes = new ArrayList<>(propertyTypes);
    
    return this;
  }
  
  @Override
  public GroupBuilder addPropertyType(final String propertyType) {
    Preconditions.checkNotNull(propertyType);
    
    this.classTypes.add(propertyType);
    
    return this;
  }

  @Override
  public String toString() {
    return "DefaultGroupBuilder [owner=" + owner + ", id=" + id + ", labelPredicates=" + labelPredicates
        + ", descriptionPredicates=" + descriptionPredicates + ", instanceOfPredicates="
        + instanceOfPredicates + ", classTypes=" + classTypes + ", propertyTypes=" + propertyTypes
        + "]";
  }
}
