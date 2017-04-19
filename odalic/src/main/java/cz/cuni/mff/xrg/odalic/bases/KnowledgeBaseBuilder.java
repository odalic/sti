package cz.cuni.mff.xrg.odalic.bases;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.xrg.odalic.groups.Group;
import cz.cuni.mff.xrg.odalic.users.User;


public interface KnowledgeBaseBuilder {

  KnowledgeBaseBuilder reset();

  KnowledgeBase build();

  /**
   * @return the owner
   */
  User getOwner();

  String getName();

  boolean isInsertEnabled();

  String getDescription();

  URL getEndpoint();

  AdvancedBaseType getAdvancedType();

  Set<Group> getSelectedGroups();

  Map<String, String> getProperties();

  /**
   * @return the language tag
   */
  String getLanguageTag();

  TextSearchingMethod getTextSearchingMethod();

  List<String> getSkippedAttributes();

  List<String> getSkippedClasses();

  String getInsertGraph();

  String getUserClassesPrefix();

  String getUserResourcesPrefix();

  Map<String, String> getAdvancedProperties();

  /**
   * @param owner the owner to set
   */
  KnowledgeBaseBuilder setOwner(User owner);

  /**
   * @param name the name to set
   */
  KnowledgeBaseBuilder setName(String name);

  /**
   * @param endpoint the endpoint to set
   */
  KnowledgeBaseBuilder setEndpoint(URL endpoint);

  /**
   * @param description the description to set
   */
  KnowledgeBaseBuilder setDescription(String description);

  /**
   * @param textSearchingMethod the textSearchingMethod to set
   */
  KnowledgeBaseBuilder setTextSearchingMethod(TextSearchingMethod textSearchingMethod);

  /**
   * @param languageTag the languageTag to set
   */
  KnowledgeBaseBuilder setLanguageTag(String languageTag);

  /**
   * @param skippedAttributes the skippedAttributes to set
   */
  KnowledgeBaseBuilder setSkippedAttributes(List<String> skippedAttributes);

  KnowledgeBaseBuilder addSkippedAttribute(String attribute);

  /**
   * @param skippedClasses the skippedClasses to set
   */
  KnowledgeBaseBuilder setSkippedClasses(List<String> skippedClasses);

  KnowledgeBaseBuilder addSkippedClass(String klass);

  /**
   * @param selectedGroups the selectedGroups to set
   */
  KnowledgeBaseBuilder setSelectedGroups(Set<Group> selectedGroups);

  KnowledgeBaseBuilder addSelectedGroup(Group group);

  /**
   * @param insertEnabled the insertEnabled to set
   */
  KnowledgeBaseBuilder setInsertEnabled(boolean insertEnabled);

  /**
   * @param insertGraph the insertGraph to set
   */
  KnowledgeBaseBuilder setInsertGraph(String insertGraph);

  /**
   * @param userClassesPrefix the userClassesPrefix to set
   */
  KnowledgeBaseBuilder setUserClassesPrefix(String userClassesPrefix);

  /**
   * @param userResourcesPrefix the userResourcesPrefix to set
   */
  KnowledgeBaseBuilder setUserResourcesPrefix(String userResourcesPrefix);

  /**
   * @param advancedType the advancedType to set
   */
  KnowledgeBaseBuilder setAdvancedType(AdvancedBaseType advancedType);

  /**
   * @param advancedProperties the advancedProperties to set
   */
  KnowledgeBaseBuilder setAdvancedProperties(Map<String, String> advancedProperties);

  KnowledgeBaseBuilder addAdvancedProperty(String key, String value);
}
