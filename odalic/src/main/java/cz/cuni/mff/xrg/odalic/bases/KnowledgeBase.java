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
  private final URI insertGraph;
  private final URI userClassesPrefix;
  private final URI userResourcesPrefix;
  
  private final AdvancedBaseType advancedType;
  private final Map<String, String> advancedProperties;

  /**
   * Creates new knowledge base configuration.
   * 
   * @param owner owner
   * @param name knowledge base name
   * @param endpoint end-point URL
   * @param description knowledge base description
   * @param insertEnabled whether the base supports insertion of new concepts
   * @param groupsAutoSelected whether the used groups are determined automatically
   * @param selectedGroups the groups selected for use
   * @param advancedType knowledge base type, affects the applicable properties
   * @param advancedProperties advanced configuration properties
   */
  public KnowledgeBase(final User owner, final String name, final URL endpoint, final String description,
      final TextSearchingMethod textSearchingMethod, final String languageTag,
      final List<? extends String> skippedAttributes, final List<? extends String> skippedClasses,
      final boolean groupsAutoSelected, final Set<? extends Group> selectedGroups, final boolean insertEnabled, final URI insertGraph,
      final URI userClassesPrefix, final URI userResourcesPrefix,
      final AdvancedBaseType advancedType, final Map<? extends String, ? extends String> advancedProperties) {
    Preconditions.checkNotNull(owner);
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(endpoint);
    Preconditions.checkNotNull(description);
    Preconditions.checkNotNull(textSearchingMethod);
    Preconditions.checkNotNull(languageTag);
    Preconditions.checkNotNull(skippedAttributes);
    Preconditions.checkNotNull(skippedClasses);
    Preconditions.checkNotNull(selectedGroups);
    Preconditions.checkNotNull(advancedType);
    Preconditions.checkNotNull(advancedProperties);

    Preconditions.checkArgument(!name.isEmpty(), "The name is empty!");
    Preconditions.checkArgument(groupsAutoSelected || !selectedGroups.isEmpty(), "Groups to be selected manually but none selected!");

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
    this.insertGraph = insertGraph;
    this.userClassesPrefix = userClassesPrefix;
    this.userResourcesPrefix = userResourcesPrefix;
    
    this.advancedType = advancedType;
    this.advancedProperties = ImmutableMap.copyOf(advancedProperties);
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
  public URI getInsertGraph() {
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "KnowledgeBase [owner=" + owner + ", name=" + name + ", endpoint=" + endpoint
        + ", description=" + description + ", textSearchingMethod=" + textSearchingMethod
        + ", languageTag=" + languageTag + ", skippedAttributes=" + skippedAttributes
        + ", skippedClasses=" + skippedClasses + ", groupsAutoSelected=" + groupsAutoSelected
        + ", selectedGroups=" + selectedGroups + ", insertEnabled=" + insertEnabled
        + ", insertGraph=" + insertGraph + ", userClassesPrefix=" + userClassesPrefix
        + ", userResourcesPrefix=" + userResourcesPrefix + ", advancedType=" + advancedType
        + ", advancedProperties=" + advancedProperties + "]";
  }
}
