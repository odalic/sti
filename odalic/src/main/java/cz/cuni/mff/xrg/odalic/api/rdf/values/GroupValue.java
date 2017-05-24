package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import cz.cuni.mff.xrg.odalic.groups.Group;

/**
 * Domain class {@link Group} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/Group")
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
  @RdfProperty(value = "http://odalic.eu/internal/Group/id",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getId() {
    return id;
  }

  /**
   * @param id the ID to set
   */
  public void setId(String id) {
    Preconditions.checkNotNull(id);
    
    this.id = id;
  }

  /**
   * @return the label predicates
   */
  @RdfProperty(value = "http://odalic.eu/internal/Group/labelPredicate",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  public List<String> getLabelPredicates() {
    return labelPredicates;
  }

  /**
   * @param labelPredicates the labelPredicates to set
   */
  public void setLabelPredicates(final List<String> labelPredicates) {
    Preconditions.checkNotNull(labelPredicates);
    
    this.labelPredicates = ImmutableList.copyOf(labelPredicates);
  }

  /**
   * @return the descriptionPredicates
   */
  @RdfProperty(value = "http://odalic.eu/internal/Group/descriptionPredicate",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  public List<String> getDescriptionPredicates() {
    return descriptionPredicates;
  }

  /**
   * @param descriptionPredicates the descriptionPredicates to set
   */
  public void setDescriptionPredicates(final List<String> descriptionPredicates) {
    Preconditions.checkNotNull(descriptionPredicates);
    
    this.descriptionPredicates = ImmutableList.copyOf(descriptionPredicates);
  }

  /**
   * @return the instanceOfPredicates
   */
  @RdfProperty(value = "http://odalic.eu/internal/Group/instanceOfPredicate",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  public List<String> getInstanceOfPredicates() {
    return instanceOfPredicates;
  }

  /**
   * @param instanceOfPredicates the instanceOfPredicates to set
   */
  public void setInstanceOfPredicates(final List<String> instanceOfPredicates) {
    Preconditions.checkNotNull(instanceOfPredicates);
    
    this.instanceOfPredicates = ImmutableList.copyOf(instanceOfPredicates);
  }

  /**
   * @return the classTypes
   */
  @RdfProperty(value = "http://odalic.eu/internal/Group/classType",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  public List<String> getClassTypes() {
    return classTypes;
  }

  /**
   * @param classTypes the classTypes to set
   */
  public void setClassTypes(final List<String> classTypes) {
    Preconditions.checkNotNull(classTypes);
    
    this.classTypes = ImmutableList.copyOf(classTypes);
  }

  /**
   * @return the propertyTypes
   */
  @RdfProperty(value = "http://odalic.eu/internal/Group/propertyType",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  public List<String> getPropertyTypes() {
    return propertyTypes;
  }

  /**
   * @param propertyTypes the propertyTypes to set
   */
  public void setPropertyTypes(final List<String> propertyTypes) {
    Preconditions.checkNotNull(propertyTypes);
    
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
