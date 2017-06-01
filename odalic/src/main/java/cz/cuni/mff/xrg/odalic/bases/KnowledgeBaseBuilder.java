package cz.cuni.mff.xrg.odalic.bases;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.groups.Group;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * Knowledge base configuration builder.
 *
 * @author Václav Brodec
 *
 */
@Component
public final class KnowledgeBaseBuilder implements Serializable {

  private static final long serialVersionUID = 2241360833757117714L;

  private User owner;

  private String name;
  private URL endpoint;
  private String description;

  private TextSearchingMethod textSearchingMethod;
  private String languageTag;

  private List<String> skippedAttributes;
  private List<String> skippedClasses;

  private boolean groupsAutoSelected;
  private Set<Group> selectedGroups;

  private boolean insertEnabled;
  private URL insertEndpoint;
  private String insertGraph;
  private URI userClassesPrefix;
  private URI userResourcesPrefix;
  private String datatypeProperty;
  private String objectProperty;

  private String login;
  private String password;

  private AdvancedBaseType advancedType;
  private Map<String, String> advancedProperties;

  /**
   * Creates new knowledge base configuration builder.
   */
  public KnowledgeBaseBuilder() {
    reset();
  }

  public KnowledgeBaseBuilder reset() {
    this.owner = null;

    this.name = null;
    this.endpoint = null;
    this.description = "";

    this.textSearchingMethod = null;
    this.languageTag = null;

    this.insertEnabled = false;
    this.insertEndpoint = null;
    this.insertGraph = null;
    this.userClassesPrefix = null;
    this.userResourcesPrefix = null;
    this.datatypeProperty = null;
    this.objectProperty = null;

    this.login = null;
    this.password = null;

    this.advancedType = null;

    this.skippedAttributes = new ArrayList<>();
    this.skippedClasses = new ArrayList<>();
    this.groupsAutoSelected = true;
    this.selectedGroups = new HashSet<>();
    this.advancedProperties = new HashMap<>();

    return this;
  }

  public KnowledgeBase build() {
    return new KnowledgeBase(owner, name, endpoint, description, textSearchingMethod, languageTag,
        skippedAttributes, skippedClasses, groupsAutoSelected, selectedGroups, insertEnabled,
        insertEndpoint, insertGraph, userClassesPrefix, userResourcesPrefix, datatypeProperty,
        objectProperty, login, password, advancedType, advancedProperties);
  }

  /**
   * @return the owner
   */
  @Nullable
  public User getOwner() {
    return this.owner;
  }

  @Nullable
  public String getName() {
    return this.name;
  }

  public boolean isInsertEnabled() {
    return this.insertEnabled;
  }

  public URL getInsertEndpoint() {
    return this.insertEndpoint;
  }

  @Nullable
  public String getDescription() {
    return this.description;
  }

  @Nullable
  public URL getEndpoint() {
    return this.endpoint;
  }

  @Nullable
  public AdvancedBaseType getAdvancedType() {
    return this.advancedType;
  }

  public boolean getGroupsAutoSelected() {
    return this.groupsAutoSelected;
  }

  public Set<Group> getSelectedGroups() {
    return this.selectedGroups;
  }

  public Map<String, String> getProperties() {
    return this.advancedProperties;
  }

  /**
   * @return the language tag
   */
  @Nullable
  public String getLanguageTag() {
    return languageTag;
  }

  @Nullable
  public TextSearchingMethod getTextSearchingMethod() {
    return textSearchingMethod;
  }

  public List<String> getSkippedAttributes() {
    return skippedAttributes;
  }

  public List<String> getSkippedClasses() {
    return skippedClasses;
  }

  @Nullable
  public String getInsertGraph() {
    return insertGraph;
  }

  @Nullable
  public URI getUserClassesPrefix() {
    return userClassesPrefix;
  }

  @Nullable
  public URI getUserResourcesPrefix() {
    return userResourcesPrefix;
  }

  @Nullable
  public String getDatatypeProperty() {
    return datatypeProperty;
  }

  @Nullable
  public String getObjectProperty() {
    return objectProperty;
  }

  @Nullable
  public String getLogin() {
    return login;
  }

  @Nullable
  public String getPassword() {
    return password;
  }

  public Map<String, String> getAdvancedProperties() {
    return advancedProperties;
  }

  /**
   * @param owner the owner to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setOwner(@Nullable User owner) {
    this.owner = owner;

    return this;
  }

  /**
   * @param name the name to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setName(@Nullable String name) {
    this.name = name;

    return this;
  }

  /**
   * @param endpoint the endpoint to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setEndpoint(@Nullable URL endpoint) {
    this.endpoint = endpoint;

    return this;
  }

  /**
   * @param description the description to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setDescription(@Nullable String description) {
    this.description = description;

    return this;
  }

  /**
   * @param textSearchingMethod the textSearchingMethod to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setTextSearchingMethod(
      @Nullable TextSearchingMethod textSearchingMethod) {
    this.textSearchingMethod = textSearchingMethod;

    return this;
  }

  /**
   * @param languageTag the languageTag to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setLanguageTag(@Nullable String languageTag) {
    this.languageTag = languageTag;

    return this;
  }

  /**
   * @param skippedAttributes the skippedAttributes to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setSkippedAttributes(List<String> skippedAttributes) {
    this.skippedAttributes = new ArrayList<>(skippedAttributes);

    return this;
  }

  public KnowledgeBaseBuilder addSkippedAttribute(final String attribute) {
    Preconditions.checkNotNull(attribute, "The attribute cannot be null!");

    this.skippedAttributes.add(attribute);

    return this;
  }

  /**
   * @param skippedClasses the skippedClasses to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setSkippedClasses(List<String> skippedClasses) {
    this.skippedClasses = new ArrayList<>(skippedClasses);

    return this;
  }

  public KnowledgeBaseBuilder addSkippedClass(final String klass) {
    Preconditions.checkNotNull(klass, "The klass cannot be null!");

    this.skippedClasses.add(klass);

    return this;
  }

  /**
   * @param groupsAutoSelected whether the selected groups are ignored and the actually used ones
   *        are auto-detected
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setGroupsAutoSelected(final boolean groupsAutoSelected) {
    this.groupsAutoSelected = groupsAutoSelected;

    return this;
  }

  /**
   * @param selectedGroups the selectedGroups to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setSelectedGroups(Set<Group> selectedGroups) {
    this.selectedGroups = new HashSet<>(selectedGroups);

    return this;
  }

  public KnowledgeBaseBuilder addSelectedGroup(final Group group) {
    Preconditions.checkNotNull(group, "The group cannot be null!");

    this.selectedGroups.add(group);

    return this;
  }

  /**
   * @param insertEnabled the insertEnabled to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setInsertEnabled(boolean insertEnabled) {
    this.insertEnabled = insertEnabled;

    return this;
  }

  /**
   * @param insertEndpoint insert end-point URL
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setInsertEndpoint(final URL insertEndpoint) {
    this.insertEndpoint = insertEndpoint;

    return this;
  }

  /**
   * @param insertGraph the insertGraph to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setInsertGraph(@Nullable String insertGraph) {
    this.insertGraph = insertGraph;

    return this;
  }

  /**
   * @param userClassesPrefix the userClassesPrefix to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setUserClassesPrefix(@Nullable URI userClassesPrefix) {
    this.userClassesPrefix = userClassesPrefix;

    return this;
  }

  /**
   * @param userResourcesPrefix the userResourcesPrefix to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setUserResourcesPrefix(@Nullable URI userResourcesPrefix) {
    this.userResourcesPrefix = userResourcesPrefix;

    return this;
  }

  /**
   * @param datatypeProperty the datatype property type to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setDatatypeProperty(@Nullable String datatypeProperty) {
    this.datatypeProperty = datatypeProperty;

    return this;
  }

  /**
   * @param objectProperty the object property type to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setInsertObjectPropertyType(@Nullable String objectProperty) {
    this.objectProperty = objectProperty;

    return this;
  }

  /**
   * @param login the login to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setLogin(@Nullable String login) {
    this.login = login;

    return this;
  }

  /**
   * @param password the password to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setPassword(@Nullable String password) {
    this.password = password;

    return this;
  }

  /**
   * @param advancedType the advancedType to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setAdvancedType(@Nullable AdvancedBaseType advancedType) {
    this.advancedType = advancedType;

    return this;
  }

  /**
   * @param advancedProperties the advancedProperties to set
   * 
   * @return the builder
   */
  public KnowledgeBaseBuilder setAdvancedProperties(Map<String, String> advancedProperties) {
    this.advancedProperties = new HashMap<>(advancedProperties);

    return this;
  }

  public KnowledgeBaseBuilder addAdvancedProperty(final String key, final String value) {
    Preconditions.checkNotNull(key, "The key cannot be null!");
    Preconditions.checkNotNull(value, "The value cannot be null!");

    this.advancedProperties.put(key, value);

    return this;
  }

  @Override
  public String toString() {
    return "KnowledgeBaseBuilder [owner=" + owner + ", name=" + name + ", endpoint=" + endpoint
        + ", description=" + description + ", textSearchingMethod=" + textSearchingMethod
        + ", languageTag=" + languageTag + ", skippedAttributes=" + skippedAttributes
        + ", skippedClasses=" + skippedClasses + ", groupsAutoSelected=" + groupsAutoSelected
        + ", selectedGroups=" + selectedGroups + ", insertEnabled=" + insertEnabled
        + ", insertEndpoint=" + insertEndpoint + ", insertGraph=" + insertGraph
        + ", userClassesPrefix=" + userClassesPrefix + ", userResourcesPrefix="
        + userResourcesPrefix + ", datatypeProperty=" + datatypeProperty + ", objectProperty="
        + objectProperty + ", login=" + login + ", password=" + password + ", advancedType="
        + advancedType + ", advancedProperties=" + advancedProperties + "]";
  }
}
