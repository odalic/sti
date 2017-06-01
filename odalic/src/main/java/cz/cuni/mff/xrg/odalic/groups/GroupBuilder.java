package cz.cuni.mff.xrg.odalic.groups;

import java.util.List;

import cz.cuni.mff.xrg.odalic.users.User;


public interface GroupBuilder {

  GroupBuilder reset();

  Group build();

  /**
   * @return the owner
   */
  User getOwner();

  String getId();

  List<String> getLabelPredicates();

  List<String> getDescriptionPredicates();

  List<String> getInstanceOfPredicates();

  List<String> getClassTypes();

  List<String> getPropertyTypes();

  /**
   * @param owner the owner to set
   * 
   * @return the builder
   */
  GroupBuilder setOwner(User owner);

  /**
   * @param id the id to set
   * 
   * @return the builder
   */
  GroupBuilder setId(String id);

  /**
   * @param labelPredicates the labelPredicates to set
   * 
   * @return the builder
   */
  GroupBuilder setLabelPredicates(List<? extends String> labelPredicates);

  GroupBuilder addLabelPredicate(String labelPredicate);

  /**
   * @param descriptionPredicates the descriptionPredicates to set
   * 
   * @return the builder
   */
  GroupBuilder setDescriptionPredicates(List<? extends String> descriptionPredicates);

  GroupBuilder addDescriptionPredicate(String descriptionPredicate);

  /**
   * @param instanceOfPredicates the instanceOfPredicates to set
   * 
   * @return the builder
   */
  GroupBuilder setInstanceOfPredicates(List<? extends String> instanceOfPredicates);

  GroupBuilder addInstancePredicate(String instancePredicate);

  /**
   * @param classTypes the classTypes to set
   * 
   * @return the builder
   */
  GroupBuilder setClassTypes(List<? extends String> classTypes);

  GroupBuilder addClassType(String classType);

  /**
   * @param propertyTypes the propertyTypes to set
   * 
   * @return the builder
   */
  GroupBuilder setPropertyTypes(List<String> propertyTypes);

  GroupBuilder addPropertyType(String propertyType);

}
