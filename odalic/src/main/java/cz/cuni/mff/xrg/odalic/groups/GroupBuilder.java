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
   */
  GroupBuilder setOwner(User owner);

  /**
   * @param id the id to set
   */
  GroupBuilder setId(String id);

  /**
   * @param labelPredicates the labelPredicates to set
   */
  GroupBuilder setLabelPredicates(List<? extends String> labelPredicates);

  GroupBuilder addLabelPredicate(String labelPredicate);

  /**
   * @param descriptionPredicates the descriptionPredicates to set
   */
  GroupBuilder setDescriptionPredicates(List<? extends String> descriptionPredicates);

  GroupBuilder addDescriptionPredicate(String descriptionPredicate);

  /**
   * @param instanceOfPredicates the instanceOfPredicates to set
   */
  GroupBuilder setInstanceOfPredicates(List<? extends String> instanceOfPredicates);

  GroupBuilder addInstancePredicate(String instancePredicate);

  /**
   * @param classTypes the classTypes to set
   */
  GroupBuilder setClassTypes(List<? extends String> classTypes);

  GroupBuilder addClassType(String classType);

  /**
   * @param propertyTypes the propertyTypes to set
   */
  GroupBuilder setPropertyTypes(List<String> propertyTypes);

  GroupBuilder addPropertyType(String propertyType);

}
