package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.bases.TextSearchingMethod;
import cz.cuni.mff.xrg.odalic.groups.Group;
import jersey.repackaged.com.google.common.collect.ImmutableList;

/**
 * Domain class {@link KnowledgeBase} adapted for REST API output.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "knowledgeBase")
public final class KnowledgeBaseValue {

  private String name;

  private URL endpoint;
  private String description;

  private TextSearchingMethod textSearchingMethod;
  private String languageTag;

  private List<String> skippedAttributes;
  private List<String> skippedClasses;

  private boolean groupsAutoSelected;
  private Set<String> selectedGroups;

  private boolean insertEnabled;
  private URL insertEndpoint;
  private URI insertGraph;
  private URI userClassesPrefix;
  private URI userResourcesPrefix;
  private URI datatypeProperty;
  private URI objectProperty;

  private String login;
  private String password;

  private String advancedType;
  private Map<String, String> advancedProperties;

  public KnowledgeBaseValue() {
    this.skippedAttributes = ImmutableList.of();
    this.skippedClasses = ImmutableList.of();

    this.groupsAutoSelected = true;
    this.selectedGroups = ImmutableSet.of();
    
    this.advancedProperties = ImmutableMap.of();
  }

  public KnowledgeBaseValue(final KnowledgeBase adaptee) {
    this.name = adaptee.getName();
    this.endpoint = adaptee.getEndpoint();
    this.description = adaptee.getDescription();

    this.textSearchingMethod = adaptee.getTextSearchingMethod();
    this.languageTag = adaptee.getLanguageTag();
    this.skippedAttributes = ImmutableList.copyOf(adaptee.getSkippedAttributes());
    this.skippedClasses = ImmutableList.copyOf(adaptee.getSkippedClasses());

    this.groupsAutoSelected = adaptee.getGroupsAutoSelected();
    this.selectedGroups = adaptee.getSelectedGroups().stream().map(Group::getId)
        .collect(ImmutableSet.toImmutableSet());

    this.insertEnabled = adaptee.isInsertEnabled();
    this.insertEndpoint = adaptee.getInsertEndpoint();
    this.insertGraph = adaptee.getInsertGraph();
    this.userClassesPrefix = adaptee.getUserClassesPrefix();
    this.userResourcesPrefix = adaptee.getUserResourcesPrefix();
    this.datatypeProperty = adaptee.getDatatypeProperty();
    this.objectProperty = adaptee.getObjectProperty();

    this.login = adaptee.getLogin();
    this.password = adaptee.getPassword();

    this.advancedType = adaptee.getAdvancedType().getName();
    this.advancedProperties = ImmutableMap.copyOf(adaptee.getAdvancedProperties());
  }

  /**
   * @return the name
   */
  @XmlElement
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
  @XmlElement
  @Nullable
  public URL getEndpoint() {
    return endpoint;
  }

  /**
   * @param endpoint the endpoint to set
   */
  public void setEndpoint(URL endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * @return the description
   */
  @XmlElement
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
   * @return the {@link TextSearchingMethod}
   */
  @XmlElement
  @Nullable
  public TextSearchingMethod getTextSearchingMethod() {
    return textSearchingMethod;
  }

  /**
   * @param textSearchingMethod the {@link TextSearchingMethod} to set
   */
  public void setTextSearchingMethod(TextSearchingMethod textSearchingMethod) {
    this.textSearchingMethod = textSearchingMethod;
  }

  /**
   * @return the language tag
   */
  @XmlElement
  @Nullable
  public String getLanguageTag() {
    return languageTag;
  }

  /**
   * @param languageTag the language tag to set
   */
  public void setLanguageTag(String languageTag) {
    this.languageTag = languageTag;
  }

  /**
   * @return the skipped attributes
   */
  @XmlElement
  @Nullable
  public List<String> getSkippedAttributes() {
    return skippedAttributes;
  }

  /**
   * @param skippedAttributes the skipped attributes to set
   */
  public void setSkippedAttributes(final List<String> skippedAttributes) {
    Preconditions.checkNotNull(skippedAttributes);

    this.skippedAttributes = ImmutableList.copyOf(skippedAttributes);
  }

  /**
   * @return the skippedClasses
   */
  @XmlElement
  @Nullable
  public List<String> getSkippedClasses() {
    return skippedClasses;
  }

  /**
   * @param skippedClasses the skipped classes to set
   */
  public void setSkippedClasses(final List<String> skippedClasses) {
    Preconditions.checkNotNull(skippedClasses);

    this.skippedClasses = ImmutableList.copyOf(skippedClasses);
  }

  /**
   * @return whether the groups are auto-selected
   */
  @XmlElement
  public boolean getGroupsAutoSelected() {
    return this.groupsAutoSelected;
  }

  /**
   * @param groupsAutoSelected whether the groups are auto-selected
   */
  public void setGroupsAutoSelected(final boolean groupsAutoSelected) {
    this.groupsAutoSelected = groupsAutoSelected;
  }

  /**
   * @return the selectedGroups
   */
  @XmlElement
  @Nullable
  public Set<String> getSelectedGroups() {
    return selectedGroups;
  }

  /**
   * @param selectedGroups the selected {@link Group}s to set
   */
  public void setSelectedGroups(final Set<String> selectedGroups) {
    Preconditions.checkNotNull(selectedGroups);

    this.selectedGroups = ImmutableSet.copyOf(selectedGroups);
  }

  /**
   * @return whether the insertion is enabled
   */
  public boolean isInsertEnabled() {
    return insertEnabled;
  }

  /**
   * @param insertEnabled whether the insertion is enabled
   */
  public void setInsertEnabled(final boolean insertEnabled) {
    this.insertEnabled = insertEnabled;
  }

  /**
   * @return the insert endpoint
   */
  @XmlElement
  @Nullable
  public URL getInsertEndpoint() {
    return insertEndpoint;
  }

  /**
   * @param insertGraph the insert endpoint to set
   */
  public void setInsertEndpoint(@Nullable final URL insertEndpoint) {
    this.insertEndpoint = insertEndpoint;
  }

  /**
   * @return the insert graph
   */
  @XmlElement
  @Nullable
  public URI getInsertGraph() {
    return insertGraph;
  }

  /**
   * @param insertGraph the insert graph to set
   */
  public void setInsertGraph(@Nullable final URI insertGraph) {
    this.insertGraph = insertGraph;
  }

  /**
   * @return the userClassesPrefix
   */
  @XmlElement
  @Nullable
  public URI getUserClassesPrefix() {
    return userClassesPrefix;
  }

  /**
   * @param userClassesPrefix the user classes prefix to set
   */
  public void setUserClassesPrefix(@Nullable final URI userClassesPrefix) {
    this.userClassesPrefix = userClassesPrefix;
  }

  /**
   * @return the user resources prefix
   */
  @XmlElement
  @Nullable
  public URI getUserResourcesPrefix() {
    return userResourcesPrefix;
  }

  /**
   * @param userResourcesPrefix the user resources prefix to set
   */
  public void setUserResourcesPrefix(@Nullable final URI userResourcesPrefix) {
    this.userResourcesPrefix = userResourcesPrefix;
  }

  /**
   * @return the property type used when inserting datatype properties
   */
  @XmlElement
  @Nullable
  public URI getDatatypeProperty() {
    return datatypeProperty;
  }

  /**
   * @param datatypeProperty the property type used when inserting datatype properties
   */
  public void setDatatypeProperty(@Nullable final URI datatypeProperty) {
    this.datatypeProperty = datatypeProperty;
  }

  /**
   * @return the property type used when inserting object properties
   */
  @XmlElement
  @Nullable
  public URI getObjectProperty() {
    return objectProperty;
  }

  /**
   * @param objectProperty the property type used when inserting object properties
   */
  public void setInsertObjectPropertyType(@Nullable final URI objectProperty) {
    this.objectProperty = objectProperty;
  }

  /**
   * @return the login
   */
  @XmlElement
  @Nullable
  public String getLogin() {
    return login;
  }

  /**
   * @param login the login to set
   */
  public void setLogin(final String login) {
    this.login = login;
  }

  /**
   * @return the password
   */
  @XmlElement
  @Nullable
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(final String password) {

    this.password = password;
  }

  /**
   * @return the advanced type
   */
  @XmlElement
  @Nullable
  public String getAdvancedType() {
    return advancedType;
  }

  /**
   * @param advancedType the advanced type to set
   */
  public void setAdvancedType(final String advancedType) {
    Preconditions.checkNotNull(advancedType);

    this.advancedType = advancedType;
  }

  /**
   * @return the advanced properties
   */
  @XmlElement
  @Nullable
  public Map<String, String> getAdvancedProperties() {
    return advancedProperties;
  }

  /**
   * @param advancedProperties the advanced properties to set
   */
  public void setAdvancedProperties(final Map<String, String> advancedProperties) {
    Preconditions.checkNotNull(advancedProperties);

    this.advancedProperties = ImmutableMap.copyOf(advancedProperties);
  }

  @Override
  public String toString() {
    return "KnowledgeBaseValue [name=" + name + ", endpoint=" + endpoint + ", description="
        + description + ", textSearchingMethod=" + textSearchingMethod + ", languageTag="
        + languageTag + ", skippedAttributes=" + skippedAttributes + ", skippedClasses="
        + skippedClasses + ", groupsAutoSelected=" + groupsAutoSelected + ", selectedGroups="
        + selectedGroups + ", insertEnabled=" + insertEnabled + ", insertEndpoint=" + insertEndpoint
        + ", insertGraph=" + insertGraph + ", userClassesPrefix=" + userClassesPrefix
        + ", userResourcesPrefix=" + userResourcesPrefix + ", datatypeProperty=" + datatypeProperty
        + ", objectProperty=" + objectProperty + ", login=" + login
        + ", password="
        + password + ", advancedType=" + advancedType + ", advancedProperties="
        + advancedProperties
        + "]";
  }
}
