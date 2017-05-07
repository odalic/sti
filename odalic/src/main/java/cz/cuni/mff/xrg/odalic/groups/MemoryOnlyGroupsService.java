/**
 *
 */
package cz.cuni.mff.xrg.odalic.groups;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;
import jersey.repackaged.com.google.common.collect.ImmutableList;

/**
 * Memory-only {@link GroupsService} implementation.
 *
 */
@Component
public final class MemoryOnlyGroupsService implements GroupsService {

  private static final String INITIAL_GROUP_INSTANCE_OF_PREDICATES_PROPERTY_KEY = "kb.structure.predicate.instanceOf";

  private static final String INITIAL_GROUP_PROPERTY_TYPES_PROPERTY_KEY = "kb.structure.type.property";

  private static final String INITIAL_GROUP_LABEL_PREDICATES_PROPERTY_KEY = "kb.structure.predicate.label";

  private static final String INITIAL_GROUP_DESCRIPTION_PREDICATES_PROPERTY_KEY = "kb.structure.predicate.description";

  private static final String PROPERTY_VALUES_SEPRATOR = " ";

  private static final Set<String> GROUP_FILES_EXTENSIONS = ImmutableSet.of("properties");

  private static final String INITIAL_GROUP_CLASS_TYPES_PROPERTY_KEY = "kb.structure.type.class";

  private static final String BASE_PATH_PROPERTY_KEY = "sti.home";

  private static final String INITIAL_GROUPS_PATH_PROPERTY_KEY = "sti.enums";

  private static final Path DEFAULT_INITIAL_GROUPS_PATH = Paths.get("resources", "enums");

  private final Table<String, String, Group> userAndGroupIdsToGroups;
  
  private final Table<String, String, Set<String>> utilizingBases;

  private final Path initialGroupsPath;

  @Autowired
  public MemoryOnlyGroupsService(final PropertiesService propertiesService) {
    this(HashBasedTable.create(), HashBasedTable.create(), initializeInitialGroupsPath(propertiesService));
  }
  
  private static Path initializeInitialGroupsPath(PropertiesService propertiesService) {
    final Path basePath = readApplicationBasePath(propertiesService);
    
    final Properties properties = propertiesService.get();
    
    final String initialGroupsPathValue = properties.getProperty(INITIAL_GROUPS_PATH_PROPERTY_KEY);
    if (initialGroupsPathValue == null) {
      return basePath.resolve(DEFAULT_INITIAL_GROUPS_PATH);
    }
    
    return basePath.resolve(Paths.get(initialGroupsPathValue));
  }
  
  private static Path readApplicationBasePath(final PropertiesService propertiesService) {
    final Path basePath = Paths.get(propertiesService.get().getProperty(BASE_PATH_PROPERTY_KEY));
    Preconditions.checkArgument(basePath != null, "The base path key not found!");
    
    Preconditions.checkArgument(Files.exists(basePath), String.format("The base path %s does not exist!", basePath));
    
    return basePath;
  }

  private MemoryOnlyGroupsService(final Table<String, String, Group> usersAndNamesToGroups,
      final Table<String, String, Set<String>> utilizingBases, final Path initialGroupsPath) {
    Preconditions.checkNotNull(usersAndNamesToGroups);
    Preconditions.checkNotNull(utilizingBases);
    
    this.userAndGroupIdsToGroups = usersAndNamesToGroups;
    this.utilizingBases = utilizingBases;
    this.initialGroupsPath = initialGroupsPath;
  }
  
  @Override
  public SortedSet<Group> getGroups(final String userId) {
    return ImmutableSortedSet.copyOf(this.userAndGroupIdsToGroups.row(userId).values());
  }

  @Override
  public Group getGroup(final String userId, String groupId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(groupId);
    
    final Group group = this.userAndGroupIdsToGroups.get(userId, groupId);
    Preconditions.checkArgument(group != null, "Unknown group!");

    return group;
  }

  @Override
  public void replace(final Group group) {
    Preconditions.checkNotNull(group);

    this.userAndGroupIdsToGroups.put(group.getOwner().getEmail(), group.getId(), group);
  }
  
  @Override
  public Group merge(final Group group) {
    Preconditions.checkNotNull(group);

    final String userId = group.getOwner().getEmail();
    final String groupId = group.getId();
    
    final Group previous = this.userAndGroupIdsToGroups.get(userId, groupId);
    if (previous == null) {
      create(group);
      return group;
    }
    
    final Group merged = previous.merge(group);
    replace(previous.merge(group));
    
    return merged;
  }

  @Override
  public void create(final Group group) {
    Preconditions.checkArgument(!existsGroupWithId(group.getOwner().getEmail(), group.getId()));

    replace(group);
  }
  
  @Override
  public boolean existsGroupWithId(final String userId, final String groupId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(groupId);

    return this.userAndGroupIdsToGroups.contains(userId, groupId);
  }

  @Override
  public void subscribe(final KnowledgeBase base) {
    final User baseOwner = base.getOwner();
    final String baseName = base.getName();
    
    final Set<Group> groups = base.getSelectedGroups();
    
    for (final Group group : groups) {      
      subscribe(baseOwner, baseName, group);
    }
  }

  private void subscribe(final User baseOwner, final String baseName, final Group group) {
    final User owner = group.getOwner();
    Preconditions.checkArgument(owner.equals(baseOwner),
        "The owner of the group is not the same as the owner of the base!");
    
    final String userId = owner.getEmail();
    final String groupId = group.getId();      
    
    Preconditions.checkArgument(this.userAndGroupIdsToGroups.get(userId, groupId).equals(group),
        "The group is not registered!");
 
    final Set<String> bases = this.utilizingBases.get(userId, groupId);
 
    final boolean inserted;
    if (bases == null) {
      this.utilizingBases.put(userId, groupId, Sets.newHashSet(baseName));
      inserted = true;
    } else {
      inserted = bases.add(baseName);
    }
 
      Preconditions.checkArgument(inserted, "The base has already been subcscribed to the group!");
  }

  @Override
  public void unsubscribe(final KnowledgeBase base) {
    final User baseOwner = base.getOwner();
    final String baseName = base.getName();
    
    final Set<Group> groups = base.getSelectedGroups();
    
    for (final Group group : groups) {      
      unsubscribe(baseOwner, baseName, group);
    }
  }

  private void unsubscribe(final User baseOwner, final String baseName, final Group group) {
    final User owner = group.getOwner();
    Preconditions.checkArgument(owner.equals(baseOwner),
        "The owner of the group is not the same as the owner of the base!");
    
    final String userId = owner.getEmail();
    final String groupId = group.getId();      
    
    Preconditions.checkArgument(this.userAndGroupIdsToGroups.get(userId, groupId).equals(group),
        "The group is not registered!");
 
    final Set<String> bases = this.utilizingBases.get(userId, groupId);
 
    final boolean removed;
    if (bases == null) {
      removed = false;
    } else {
      removed = bases.remove(baseName);
 
      if (bases.isEmpty()) {
        this.utilizingBases.remove(userId, groupId);
      }
    }
 
    Preconditions.checkArgument(removed, "The base is not subcscribed to the group!");
  }

  @Override
  public Group verifyGroupExistenceById(final String userId, final String groupId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(groupId);

    return this.userAndGroupIdsToGroups.get(userId, groupId);
  }

  @Override
  public Set<Group> detectUsed(final String userId, final URL endpoint) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(endpoint);
    
    // TODO: Implement used groups detection.
    return ImmutableSet.of();
  }

  @Override
  public void deleteAll(final String userId) {
    Preconditions.checkNotNull(userId);

    final Map<String, Group> groupIdsToGroups = this.userAndGroupIdsToGroups.row(userId);
    groupIdsToGroups.clear();
  }
  
  @Override
  public void deleteById(String userId, String groupId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(groupId);

    checkUtilization(userId, groupId);

    final Group group = this.userAndGroupIdsToGroups.remove(userId, groupId);
    Preconditions.checkArgument(group != null);
  }
  
  private void checkUtilization(final String userId, final String groupId)
      throws IllegalStateException {
    final Set<String> utilizingBaseIds = this.utilizingBases.get(userId, groupId);
    if (utilizingBaseIds == null) {
      return;
    }

    final String jointUtilizingBaseIds = String.join(", ", utilizingBaseIds);
    Preconditions.checkState(utilizingBaseIds.isEmpty(),
        String.format("Some bases definition (%s) still refer to this group!", jointUtilizingBaseIds));
  }

  @Override
  public void initializeDefaults(final User owner) throws IOException {
    Preconditions.checkNotNull(owner);
    
    final Iterator<File> groupPropertiesFileIterator = FileUtils.iterateFiles(this.initialGroupsPath.toFile(), GROUP_FILES_EXTENSIONS.toArray(new String[GROUP_FILES_EXTENSIONS.size()]), false);
    while (groupPropertiesFileIterator.hasNext()) {
      initializeFromPropertiesFile(owner, groupPropertiesFileIterator.next());
    }
  }

  private void initializeFromPropertiesFile(final User owner, final File propertiesFile) throws IOException {
    final Properties groupProperties = new Properties();
    groupProperties.load(new FileInputStream(propertiesFile));
    
    final GroupBuilder groupBuilder = new DefaultGroupBuilder();
    
    groupBuilder.setId(extractId(propertiesFile));
    
    groupBuilder.setClassTypes(extractValues(INITIAL_GROUP_CLASS_TYPES_PROPERTY_KEY, groupProperties));
    groupBuilder.setDescriptionPredicates(extractValues(INITIAL_GROUP_DESCRIPTION_PREDICATES_PROPERTY_KEY, groupProperties));
    groupBuilder.setInstanceOfPredicates(extractValues(INITIAL_GROUP_INSTANCE_OF_PREDICATES_PROPERTY_KEY, groupProperties));
    groupBuilder.setLabelPredicates(extractValues(INITIAL_GROUP_LABEL_PREDICATES_PROPERTY_KEY, groupProperties));
    groupBuilder.setPropertyTypes(extractValues(INITIAL_GROUP_PROPERTY_TYPES_PROPERTY_KEY, groupProperties));
    
    final Group group = groupBuilder.build();
    this.userAndGroupIdsToGroups.put(owner.getEmail(), group.getId(), group);
  }

  private static List<String> extractValues(final String propertyKey,
      final Properties properties) {
    final String rawValues = properties.getProperty(propertyKey);
    if (rawValues == null) {
      return ImmutableList.of();
    }
    
    return ImmutableList.copyOf(rawValues.split(PROPERTY_VALUES_SEPRATOR));
  }

  private static String extractId(File propertiesFile) {
    return FilenameUtils.removeExtension(propertiesFile.getName());
  }
}
