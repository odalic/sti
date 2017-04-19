package cz.cuni.mff.xrg.odalic.bases;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.groups.Group;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * Knowledge base configuration builder.
 *
 * @author Václav Brodec
 *
 */
public final class DefaultKnowledgeBaseBuilder implements Serializable, KnowledgeBaseBuilder {

  private static final long serialVersionUID = 2241360833757117714L;

  private User owner;
  
  private String name;
  private URL endpoint;  
  private String description;
  
  private TextSearchingMethod textSearchingMethod;
  private String languageTag;

  private List<String> skippedAttributes;
  private List<String> skippedClasses;
  
  private Set<Group> selectedGroups;
  
  private boolean insertEnabled;
  private String insertGraph;
  private String userClassesPrefix;
  private String userResourcesPrefix;
  
  private AdvancedBaseType advancedType;
  private Map<String, String> advancedProperties;

  /**
   * Creates new knowledge base configuration builder.
   */
  public DefaultKnowledgeBaseBuilder() {
    reset();
  }
  
  @Override
  public KnowledgeBaseBuilder reset() {
    this.owner = null;
    
    this.name = null;
    this.endpoint = null;
    this.description = "";

    this.textSearchingMethod = null;
    this.languageTag = null;
    
    this.insertEnabled = false;
    this.insertGraph = null;
    this.userClassesPrefix = null;
    this.userResourcesPrefix = null;
    
    this.advancedType = null;
    
    this.skippedAttributes = new ArrayList<>();
    this.skippedClasses = new ArrayList<>();
    this.selectedGroups = new HashSet<>();
    this.advancedProperties = new HashMap<>();
    
    return this;
  }

  @Override
  public KnowledgeBase build() {
    return new KnowledgeBase(owner, name, endpoint, description,
        textSearchingMethod, languageTag,
        skippedAttributes, skippedClasses,
        selectedGroups, insertEnabled, insertGraph,
        userClassesPrefix, userResourcesPrefix,
        advancedType, advancedProperties);
  }
  
  /**
   * @return the owner
   */
  @Override
  @Nullable
  public User getOwner() {
    return this.owner;
  }
  
  @Override
  @Nullable
  public String getName() {
    return this.name;
  }

  @Override
  public boolean isInsertEnabled() {
    return this.insertEnabled;
  }

  @Override
  @Nullable
  public String getDescription() {
    return this.description;
  }

  @Override
  @Nullable
  public URL getEndpoint() {
    return this.endpoint;
  }

  @Override
  @Nullable
  public AdvancedBaseType getAdvancedType() {
    return this.advancedType;
  }

  @Override
  public Set<Group> getSelectedGroups() {
    return this.selectedGroups;
  }

  @Override
  public Map<String, String> getProperties() {
    return this.advancedProperties;
  }

  /**
   * @return the language tag
   */
  @Override
  @Nullable
  public String getLanguageTag() {
    return languageTag;
  }
  
  @Override
  @Nullable
  public TextSearchingMethod getTextSearchingMethod() {
    return textSearchingMethod;
  }

  @Override
  public List<String> getSkippedAttributes() {
    return skippedAttributes;
  }

  @Override
  public List<String> getSkippedClasses() {
    return skippedClasses;
  }

  @Override
  @Nullable
  public String getInsertGraph() {
    return insertGraph;
  }

  @Override
  @Nullable
  public String getUserClassesPrefix() {
    return userClassesPrefix;
  }

  @Override
  @Nullable
  public String getUserResourcesPrefix() {
    return userResourcesPrefix;
  }

  @Override
  public Map<String, String> getAdvancedProperties() {
    return advancedProperties;
  }
  
  /**
   * @param owner the owner to set
   */
  @Override
  public KnowledgeBaseBuilder setOwner(@Nullable User owner) {
    this.owner = owner;

    return this;
  }

  /**
   * @param name the name to set
   */
  @Override
  public KnowledgeBaseBuilder setName(@Nullable String name) {
    this.name = name;

    return this;
  }

  /**
   * @param endpoint the endpoint to set
   */
  @Override
  public KnowledgeBaseBuilder setEndpoint(@Nullable URL endpoint) {
    this.endpoint = endpoint;

    return this;
  }

  /**
   * @param description the description to set
   */
  @Override
  public KnowledgeBaseBuilder setDescription(@Nullable String description) {
    this.description = description;

    return this;
  }

  /**
   * @param textSearchingMethod the textSearchingMethod to set
   */
  @Override
  public KnowledgeBaseBuilder setTextSearchingMethod(@Nullable TextSearchingMethod textSearchingMethod) {
    this.textSearchingMethod = textSearchingMethod;

    return this;
  }

  /**
   * @param languageTag the languageTag to set
   */
  @Override
  public KnowledgeBaseBuilder setLanguageTag(@Nullable String languageTag) {
    this.languageTag = languageTag;

    return this;
  }

  /**
   * @param skippedAttributes the skippedAttributes to set
   */
  @Override
  public KnowledgeBaseBuilder setSkippedAttributes(List<String> skippedAttributes) {
    this.skippedAttributes = new ArrayList<>(skippedAttributes);

    return this;
  }
  
  @Override
  public KnowledgeBaseBuilder addSkippedAttribute(final String attribute) {
    Preconditions.checkNotNull(attribute);
    
    this.skippedAttributes.add(attribute);
    
    return this;
  }
  
  /**
   * @param skippedClasses the skippedClasses to set
   */
  @Override
  public KnowledgeBaseBuilder setSkippedClasses(List<String> skippedClasses) {
    this.skippedClasses = new ArrayList<>(skippedClasses);

    return this;
  }
  
  @Override
  public KnowledgeBaseBuilder addSkippedClass(final String klass) {
    Preconditions.checkNotNull(klass);
    
    this.skippedClasses.add(klass);
    
    return this;
  }

  /**
   * @param selectedGroups the selectedGroups to set
   */
  @Override
  public KnowledgeBaseBuilder setSelectedGroups(Set<Group> selectedGroups) {
    this.selectedGroups = new HashSet<>(selectedGroups);

    return this;
  }
  
  @Override
  public KnowledgeBaseBuilder addSelectedGroup(final Group group) {
    Preconditions.checkNotNull(group);
    
    this.selectedGroups.add(group);
    
    return this;
  }

  /**
   * @param insertEnabled the insertEnabled to set
   */
  @Override
  public KnowledgeBaseBuilder setInsertEnabled(boolean insertEnabled) {
    this.insertEnabled = insertEnabled;

    return this;
  }

  /**
   * @param insertGraph the insertGraph to set
   */
  @Override
  public KnowledgeBaseBuilder setInsertGraph(@Nullable String insertGraph) {
    this.insertGraph = insertGraph;

    return this;
  }

  /**
   * @param userClassesPrefix the userClassesPrefix to set
   */
  @Override
  public KnowledgeBaseBuilder setUserClassesPrefix(@Nullable String userClassesPrefix) {
    this.userClassesPrefix = userClassesPrefix;

    return this;
  }

  /**
   * @param userResourcesPrefix the userResourcesPrefix to set
   */
  @Override
  public KnowledgeBaseBuilder setUserResourcesPrefix(@Nullable String userResourcesPrefix) {
    this.userResourcesPrefix = userResourcesPrefix;
    
    return this;
  }

  /**
   * @param advancedType the advancedType to set
   */
  @Override
  public KnowledgeBaseBuilder setAdvancedType(@Nullable AdvancedBaseType advancedType) {
    this.advancedType = advancedType;
    
    return this;
  }

  /**
   * @param advancedProperties the advancedProperties to set
   */
  @Override
  public KnowledgeBaseBuilder setAdvancedProperties(Map<String, String> advancedProperties) {
    this.advancedProperties = new HashMap<>(advancedProperties);

    return this;
  }
  
  @Override
  public KnowledgeBaseBuilder addAdvancedProperty(final String key, final String value) {
    Preconditions.checkNotNull(key);
    Preconditions.checkNotNull(value);
    
    this.advancedProperties.put(key, value);
    
    return this;
  }

  @Override
  public String toString() {
    return "DefaultKnowledgeBaseBuilder [owner=" + owner + ", name=" + name + ", endpoint="
        + endpoint + ", description=" + description + ", textSearchingMethod=" + textSearchingMethod
        + ", languageTag=" + languageTag + ", skippedAttributes=" + skippedAttributes
        + ", skippedClasses=" + skippedClasses + ", selectedGroups=" + selectedGroups
        + ", insertEnabled=" + insertEnabled + ", insertGraph=" + insertGraph
        + ", userClassesPrefix=" + userClassesPrefix + ", userResourcesPrefix="
        + userResourcesPrefix + ", advancedType=" + advancedType + ", advancedProperties="
        + advancedProperties + "]";
  }
}
