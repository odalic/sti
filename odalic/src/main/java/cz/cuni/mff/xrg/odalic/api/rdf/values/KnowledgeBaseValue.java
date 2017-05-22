package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.openrdf.model.Resource;

import com.complexible.pinto.Identifiable;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;

/**
 * Domain class {@link KnowledgeBase} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/KnowledgeBase")
public final class KnowledgeBaseValue implements Serializable, Identifiable {

  private static final long serialVersionUID = -2252292215974968470L;

  private String name;
  
  private String endpoint;  
  private String description;
  
  private String textSearchingMethod;
  private String languageTag;

  private List<String> skippedAttributes;
  private List<String> skippedClasses;
  
  private boolean groupsAutoSelected;
  private Set<GroupValue> selectedGroups;
  
  private boolean insertEnabled;
  private String insertEndpoint;
  private String insertGraph;
  private String userClassesPrefix;
  private String userResourcesPrefix;
  
  private String login;
  private String password;
  
  private String advancedType;
  private Set<AdvancedPropertyEntry> advancedProperties;
  
  private Resource identifiableResource;

  public KnowledgeBaseValue() {
    this.skippedAttributes = ImmutableList.of();
    this.skippedClasses = ImmutableList.of();
    this.selectedGroups = ImmutableSet.of();
    this.advancedProperties = ImmutableSet.of();
  }

  public KnowledgeBaseValue(final KnowledgeBase adaptee) {
    this.name = adaptee.getName();
    this.endpoint = adaptee.getEndpoint().toString();
    this.description = adaptee.getDescription();
    this.textSearchingMethod = adaptee.getTextSearchingMethod().toString();
    this.languageTag = adaptee.getLanguageTag();
    this.skippedAttributes = ImmutableList.copyOf(adaptee.getSkippedAttributes());
    this.skippedClasses = ImmutableList.copyOf(adaptee.getSkippedClasses());
    this.groupsAutoSelected = adaptee.getGroupsAutoSelected();
    this.selectedGroups = adaptee.getSelectedGroups().stream().map(e -> new GroupValue(e)).collect(ImmutableSet.toImmutableSet());
    this.insertEnabled = adaptee.isInsertEnabled();
    this.insertEndpoint = adaptee.getInsertEndpoint() == null ? null : adaptee.getInsertEndpoint().toString();
    this.insertGraph = adaptee.getInsertGraph() == null ? null : adaptee.getInsertGraph().toString();
    this.userClassesPrefix = adaptee.getUserClassesPrefix() == null ? null : adaptee.getUserClassesPrefix().toString();
    this.userResourcesPrefix = adaptee.getUserResourcesPrefix() == null ? null : adaptee.getUserResourcesPrefix().toString();
    this.login = adaptee.getLogin();
    this.password = adaptee.getPassword();
    this.advancedType = adaptee.getAdvancedType().getName();
    this.advancedProperties = adaptee.getAdvancedProperties().entrySet().stream().map(e -> new AdvancedPropertyEntry(e.getKey(), e.getValue())).collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Resource id() {
    return this.identifiableResource;
  }

  @Override
  public void id(final Resource resource) {
    this.identifiableResource = resource;
  }
  
  /**
   * @return the name
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/name",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getName() {
    return this.name;
  }

  /**
   * @param name the name to set
   */
  public void setName(final String name) {
    Preconditions.checkNotNull(name);

    this.name = name;
  }

  /**
   * @return the endpoint
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/endpoint",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getEndpoint() {
    return endpoint;
  }

  /**
   * @param endpoint the endpoint to set
   */
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * @return the description
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/description",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the textSearchingMethod
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/textSearchingMethod",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getTextSearchingMethod() {
    return textSearchingMethod;
  }

  /**
   * @param textSearchingMethod the textSearchingMethod to set
   */
  public void setTextSearchingMethod(String textSearchingMethod) {
    this.textSearchingMethod = textSearchingMethod;
  }

  /**
   * @return the languageTag
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/languageTag",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getLanguageTag() {
    return languageTag;
  }

  /**
   * @param languageTag the languageTag to set
   */
  public void setLanguageTag(String languageTag) {
    this.languageTag = languageTag;
  }

  /**
   * @return the skippedAttributes
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/skippedAttribute",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  public List<String> getSkippedAttributes() {
    return skippedAttributes;
  }

  /**
   * @param skippedAttributes the skippedAttributes to set
   */
  public void setSkippedAttributes(List<String> skippedAttributes) {
    this.skippedAttributes = skippedAttributes;
  }

  /**
   * @return the skippedClasses
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/skippedClass",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  public List<String> getSkippedClasses() {
    return skippedClasses;
  }

  /**
   * @param skippedClasses the skippedClasses to set
   */
  public void setSkippedClasses(List<String> skippedClasses) {
    this.skippedClasses = skippedClasses;
  }

  /**
   * @return whether the groups are auto-selected
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/groupsAutoSelected",
      datatype = "http://www.w3.org/2001/XMLSchema#boolean")
  public boolean getGroupsAutoSelected() {
    return this.groupsAutoSelected;
  }

  /**
   * @param groupsAutoSelected whether the groups are auto-selected
   */
  public void setGroupsAutoSelected(boolean groupsAutoSelected) {
    this.groupsAutoSelected = groupsAutoSelected;
  }
  
  /**
   * @return the selectedGroups
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/selectedGroup",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  public Set<GroupValue> getSelectedGroups() {
    return selectedGroups;
  }

  /**
   * @param selectedGroups the selectedGroups to set
   */
  public void setSelectedGroups(Set<GroupValue> selectedGroups) {
    this.selectedGroups = selectedGroups;
  }

  /**
   * @return the insertEnabled
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/insertEnabled",
      datatype = "http://www.w3.org/2001/XMLSchema#boolean")
  public boolean isInsertEnabled() {
    return insertEnabled;
  }

  /**
   * @param insertEnabled whtether the insert is enabled
   */
  public void setInsertEnabled(boolean insertEnabled) {
    this.insertEnabled = insertEnabled;
  }
  
  /**
   * @return the insert endpoint
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/insertEndpoint",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getInsertEndpoint() {
    return insertEndpoint;
  }

  /**
   * @param insertGraph the insert graph to set
   */
  public void setInsertEndpoint(String insertEndpoint) {
    this.insertEndpoint = insertEndpoint;
  }

  /**
   * @return the insertGraph
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/insertGraph",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getInsertGraph() {
    return insertGraph;
  }

  /**
   * @param insertGraph the insertGraph to set
   */
  public void setInsertGraph(String insertGraph) {
    this.insertGraph = insertGraph;
  }

  /**
   * @return the userClassesPrefix
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/userClassesPrefix",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getUserClassesPrefix() {
    return userClassesPrefix;
  }

  /**
   * @param userClassesPrefix the userClassesPrefix to set
   */
  public void setUserClassesPrefix(String userClassesPrefix) {
    this.userClassesPrefix = userClassesPrefix;
  }

  /**
   * @return the userResourcesPrefix
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/userResourcesPrefix",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getUserResourcesPrefix() {
    return userResourcesPrefix;
  }

  /**
   * @param userResourcesPrefix the userResourcesPrefix to set
   */
  public void setUserResourcesPrefix(String userResourcesPrefix) {
    this.userResourcesPrefix = userResourcesPrefix;
  }

  /**
   * @return the login
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/login",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getLogin() {
    return login;
  }

  /**
   * @param login the login to set
   */
  public void setLogin(String login) {
    this.login = login;
  }
  
  /**
   * @return the password
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/password",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getPassword() {
    return password;
  }

  /**
   * @param login the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }
  
  /**
   * @return the advancedType
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/advancedType",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @Nullable
  public String getAdvancedType() {
    return advancedType;
  }

  /**
   * @param advancedType the advancedType to set
   */
  public void setAdvancedType(String advancedType) {
    this.advancedType = advancedType;
  }

  /**
   * @return the advancedProperties
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/advancedProperty")
  public Set<AdvancedPropertyEntry> getAdvancedProperties() {
    return advancedProperties;
  }

  /**
   * @param advancedProperties the advancedProperties to set
   */
  public void setAdvancedProperties(Set<AdvancedPropertyEntry> advancedProperties) {
    this.advancedProperties = advancedProperties;
  }

  @Override
  public String toString() {
    return "KnowledgeBaseValue [name=" + name + ", endpoint=" + endpoint + ", description="
        + description + ", textSearchingMethod=" + textSearchingMethod + ", languageTag="
        + languageTag + ", skippedAttributes=" + skippedAttributes + ", skippedClasses="
        + skippedClasses + ", groupsAutoSelected=" + groupsAutoSelected + ", selectedGroups="
        + selectedGroups + ", insertEnabled=" + insertEnabled + ", insertEndpoint=" + insertEndpoint
        + ", insertGraph=" + insertGraph + ", userClassesPrefix=" + userClassesPrefix
        + ", userResourcesPrefix=" + userResourcesPrefix + ", login=" + login + ", password="
        + password + ", advancedType=" + advancedType + ", advancedProperties=" + advancedProperties
        + ", identifiableResource=" + identifiableResource + "]";
  }
}
