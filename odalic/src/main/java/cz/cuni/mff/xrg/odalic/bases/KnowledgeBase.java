package cz.cuni.mff.xrg.odalic.bases;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.KnowledgeBaseAdapter;
import cz.cuni.mff.xrg.odalic.groups.Group;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * Knowledge base configuration.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(KnowledgeBaseAdapter.class)
public final class KnowledgeBase implements Serializable, Comparable<KnowledgeBase> {

  private static final long serialVersionUID = 2241360833757117714L;
  
  private final User owner;

  private final String name;
  private final URL endpoint;
  private final String description;

  private final TextSearchingMethod textSearchingMethod;
  private final String languageTag;

  private final List<String> skippedAttributes;
  private final List<String> skippedClasses;

  private final boolean groupsAutoSelected;
  private final Set<Group> selectedGroups;

  private final boolean insertEnabled;
  private final URL insertEndpoint;
  private final String insertGraph;
  private final URI userClassesPrefix;
  private final URI userResourcesPrefix;
  private final String datatypeProperty;
  private final String objectProperty;

  private final String login;
  private final String password;

  private final AdvancedBaseType advancedType;
  private final Map<String, String> advancedProperties;

  /**
   * Creates new knowledge base configuration.
   * 
   * @param owner owner
   * @param name knowledge base name
   * @param endpoint end-point URL
   * @param description knowledge base description
   * @param textSearchingMethod text searching method
   * @param languageTag language tag
   * @param skippedAttributes skipped attributes
   * @param skippedClasses skipped classes
   * @param groupsAutoSelected whether the used groups are determined automatically
   * @param selectedGroups the groups selected for use
   * @param insertEnabled whether the base supports insertion of new concepts
   * @param insertEndpoint insert endpoint
   * @param insertGraph insert graph
   * @param userClassesPrefix custom classes and properties prefixes
   * @param userResourcesPrefix custom resource prefixes
   * @param datatypeProperty type used when inserting data properties
   * @param objectTypeProperty type used when inserting object properties
   * @param login login
   * @param password password
   * @param advancedType knowledge base type, affects the applicable properties
   * @param advancedProperties advanced configuration properties
   */
  public KnowledgeBase(final User owner, final String name, final URL endpoint,
      final String description, final TextSearchingMethod textSearchingMethod,
      final String languageTag, final List<? extends String> skippedAttributes,
      final List<? extends String> skippedClasses, final boolean groupsAutoSelected,
      final Set<? extends Group> selectedGroups, final boolean insertEnabled,
      final URL insertEndpoint, final String insertGraph,
      final URI userClassesPrefix, final URI userResourcesPrefix,
      final String datatypeProperty, final String objectTypeProperty, @Nullable final String login,
      @Nullable final String password, final AdvancedBaseType advancedType,
      final Map<? extends String, ? extends String> advancedProperties) {
    Preconditions.checkNotNull(owner, "The owner cannot be null!");
    Preconditions.checkNotNull(name, "The name cannot be null!");
    Preconditions.checkNotNull(endpoint, "The endpoint cannot be null!");
    Preconditions.checkNotNull(description, "The description cannot be null!");
    Preconditions.checkNotNull(textSearchingMethod, "The textSearchingMethod cannot be null!");
    Preconditions.checkNotNull(languageTag, "The languageTag cannot be null!");
    Preconditions.checkNotNull(skippedAttributes, "The skippedAttributes cannot be null!");
    Preconditions.checkNotNull(skippedClasses, "The skippedClasses cannot be null!");
    Preconditions.checkNotNull(selectedGroups, "The selectedGroups cannot be null!");
    Preconditions.checkNotNull(advancedType, "The advancedType cannot be null!");
    Preconditions.checkNotNull(advancedProperties, "The advancedProperties cannot be null!");

    Preconditions.checkArgument(!name.isEmpty(), "The name is empty!");
    Preconditions.checkArgument(groupsAutoSelected || !selectedGroups.isEmpty(),
        "Groups to be selected manually but none selected!");
    Preconditions.checkArgument(groupsAutoSelected || checkMandatoryPredicatesPresent(selectedGroups), "At least one label and type predicate must be present in the selected groups!");

    this.owner = owner;

    this.name = name;
    this.endpoint = endpoint;
    this.description = description;

    this.textSearchingMethod = textSearchingMethod;
    this.languageTag = languageTag;
    this.skippedAttributes = ImmutableList.copyOf(skippedAttributes);
    this.skippedClasses = ImmutableList.copyOf(skippedClasses);

    this.groupsAutoSelected = groupsAutoSelected;
    this.selectedGroups = ImmutableSet.copyOf(selectedGroups);

    this.insertEnabled = insertEnabled;
    this.insertEndpoint = insertEndpoint;
    this.insertGraph = insertGraph;
    this.userClassesPrefix = userClassesPrefix;
    this.userResourcesPrefix = userResourcesPrefix;
    this.datatypeProperty = datatypeProperty;
    this.objectProperty = objectTypeProperty;

    this.login = login;
    this.password = password;

    this.advancedType = advancedType;
    this.advancedProperties = ImmutableMap.copyOf(advancedProperties);
  }

  private static boolean checkMandatoryPredicatesPresent(Set<? extends Group> groups) {
    boolean labelPredicatePresent = false;
    boolean typePredicatePresent = false;
    
    for (final Group group : groups) {
      if (!group.getLabelPredicates().isEmpty()) {
        labelPredicatePresent = true;
      }
      if (!group.getInstanceOfPredicates().isEmpty()) {
        typePredicatePresent = true;
      }
      
      if (labelPredicatePresent && typePredicatePresent) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * @return the owner
   */
  public User getOwner() {
    return this.owner;
  }

  public String getName() {
    return this.name;
  }

  public boolean isInsertEnabled() {
    return this.insertEnabled;
  }

  public URL getInsertEndpoint() {
    return this.insertEndpoint;
  }

  public String getDescription() {
    return this.description;
  }

  public URL getEndpoint() {
    return this.endpoint;
  }

  public AdvancedBaseType getAdvancedType() {
    return this.advancedType;
  }

  public boolean getGroupsAutoSelected() {
    return this.groupsAutoSelected;
  }

  public Set<Group> getSelectedGroups() {
    return this.selectedGroups;
  }

  @Nullable
  public String getLogin() {
    return this.login;
  }

  @Nullable
  public String getPassword() {
    return this.password;
  }

  public Map<String, String> getProperties() {
    return this.advancedProperties;
  }

  /**
   * @return the language tag
   */
  public String getLanguageTag() {
    return languageTag;
  }


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

  public Map<String, String> getAdvancedProperties() {
    return advancedProperties;
  }

  @XmlTransient
  public String getQualifiedName() {
    return this.owner.getEmail() + "_" + this.name;
  }

  /**
   * Compares the names.
   *
   * @param other other base
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final KnowledgeBase other) {
    return this.name.compareTo(other.name);
  }

  /**
   * Compares for equality (only other knowledge base instance with the same name passes, for now).
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final KnowledgeBase other = (KnowledgeBase) obj;
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  /**
   * Computes hash code (for now) based on the name.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "KnowledgeBase [owner=" + owner + ", name=" + name + ", endpoint=" + endpoint
        + ", description=" + description + ", textSearchingMethod=" + textSearchingMethod
        + ", languageTag=" + languageTag + ", skippedAttributes=" + skippedAttributes
        + ", skippedClasses=" + skippedClasses + ", groupsAutoSelected=" + groupsAutoSelected
        + ", selectedGroups=" + selectedGroups + ", insertEnabled=" + insertEnabled
        + ", insertEndpoint=" + insertEndpoint + ",insertGraph=" + insertGraph + ", userClassesPrefix=" + userClassesPrefix+ ", userResourcesPrefix="
        +  userResourcesPrefix + ", datatypeProperty="
        + datatypeProperty + ", objectProperty=" + objectProperty
        + ",login=" + login
        + ", password= "+ password+ ", advancedType=" + advancedType
        + ",advancedProperties=" + advancedProperties + "]";
  }
}
