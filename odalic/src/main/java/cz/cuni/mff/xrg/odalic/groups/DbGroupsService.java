/**
 *
 */
package cz.cuni.mff.xrg.odalic.groups;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;
import cz.cuni.mff.xrg.odalic.util.storage.DbService;
import jersey.repackaged.com.google.common.collect.ImmutableList;

/**
 * This {@link GroupsService} implementation persists the files in {@link DB}-backed maps.
 *
 * @author Václav Brodec
 */
@Component
public final class DbGroupsService implements GroupsService {

  private static final String INITIAL_GROUP_TYPE_PREDICATES_PROPERTY_KEY = "kb.structure.predicate.type";

  private static final String INITIAL_GROUP_PROPERTY_TYPES_PROPERTY_KEY = "kb.structure.type.property";

  private static final String INITIAL_GROUP_LABEL_PREDICATES_PROPERTY_KEY = "kb.structure.predicate.label";

  private static final String INITIAL_GROUP_DESCRIPTION_PREDICATES_PROPERTY_KEY = "kb.structure.predicate.description";

  private static final String PROPERTY_VALUES_SEPRATOR = " ";

  private static final Set<String> GROUP_FILES_EXTENSIONS = ImmutableSet.of("properties");

  private static final String INITIAL_GROUP_CLASS_TYPES_PROPERTY_KEY = "kb.structure.type.class";

  private static final String BASE_PATH_PROPERTY_KEY = "sti.home";

  private static final String INITIAL_GROUPS_PATH_PROPERTY_KEY = "sti.enums";

  private static final Path DEFAULT_INITIAL_GROUPS_PATH = Paths.get("config", "enums");
  
  private final Path initialGroupsPath;
  
  private final DB db;

  private final BTreeMap<Object[], Group> userAndGroupIdsToGroups;

  private final BTreeMap<Object[], Boolean> utilizingBases;

  @Autowired
  @SuppressWarnings("unchecked")
  public DbGroupsService(final DbService dbService, final PropertiesService propertiesService) {
    Preconditions.checkNotNull(dbService, "The dbService cannot be null!");

    this.initialGroupsPath = initializeInitialGroupsPath(propertiesService);
    
    this.db = dbService.getDb();

    this.userAndGroupIdsToGroups = this.db.treeMap("userAndGroupIdsToGroups")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
    this.utilizingBases = this.db.treeMap("utilizingBases")
        .keySerializer(
            new SerializerArrayTuple(Serializer.STRING, Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.BOOLEAN).createOrOpen();
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
  
  @Override
  public SortedSet<Group> getGroups(final String userId) {
    return ImmutableSortedSet
        .copyOf(this.userAndGroupIdsToGroups.prefixSubMap(new Object[] {userId}).values());
  }

  @Override
  public Group getGroup(final String userId, String groupId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(groupId, "The groupId cannot be null!");

    final Group group = this.userAndGroupIdsToGroups.get(new Object[] {userId, groupId});
    Preconditions.checkArgument(group != null, "Unknown group!");

    return group;
  }

  @Override
  public void replace(final Group group) {
    Preconditions.checkNotNull(group, "The group cannot be null!");

    this.userAndGroupIdsToGroups.put(new Object[] {group.getOwner().getEmail(), group.getId()},
        group);

    this.db.commit();
  }

  @Override
  public void create(final Group group) {
    Preconditions.checkArgument(!existsGroupWithId(group.getOwner().getEmail(), group.getId()));

    replace(group);
  }

  @Override
  public boolean existsGroupWithId(final String userId, final String groupId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(groupId, "The groupId cannot be null!");

    return this.userAndGroupIdsToGroups.containsKey(new Object[] {userId, groupId});
  }

  @Override
  public void subscribe(final KnowledgeBase base) {
    final User baseOwner = base.getOwner();
    final String baseName = base.getName();

    final Set<Group> groups = base.getSelectedGroups();

    try {
      for (final Group group : groups) {
        subscribe(baseOwner, baseName, group);
      }
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }

  private void subscribe(final User baseOwner, final String baseName, final Group group) {
    final User owner = group.getOwner();
    Preconditions.checkArgument(owner.equals(baseOwner),
        "The owner of the group is not the same as the owner of the base!");

    final String userId = owner.getEmail();
    final String groupId = group.getId();

    final Object[] userGroupId = new Object[] {userId, groupId};

    Preconditions.checkArgument(this.userAndGroupIdsToGroups.get(userGroupId).equals(group),
        "The group is not registered!");

    final Boolean previous =
        this.utilizingBases.put(new Object[] {userId, groupId, baseName}, true);
    Preconditions.checkArgument(previous == null,
        "The base has already been subcscribed to the group!");
  }

  @Override
  public void unsubscribe(final KnowledgeBase base) {
    final User baseOwner = base.getOwner();
    final String baseName = base.getName();

    final Set<Group> groups = base.getSelectedGroups();

    try {
      for (final Group group : groups) {
        unsubscribe(baseOwner, baseName, group);
      }
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
  }

  private void unsubscribe(final User baseOwner, final String baseName, final Group group) {
    final User owner = group.getOwner();
    Preconditions.checkArgument(owner.equals(baseOwner),
        "The owner of the group is not the same as the owner of the base!");

    final String userId = owner.getEmail();
    final String groupId = group.getId();

    Preconditions.checkArgument(
        this.userAndGroupIdsToGroups.get(new Object[] {userId, groupId}).equals(group),
        "The group is not registered!");

    final Boolean removed = this.utilizingBases.remove(new Object[] {userId, groupId, baseName});
    Preconditions.checkArgument(removed != null, "The base is not subcscribed to the group!");

    Preconditions.checkArgument(removed, "The base is not subcscribed to the group!");
  }

  @Override
  public Group verifyGroupExistenceById(final String userId, final String groupId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(groupId, "The groupId cannot be null!");

    return this.userAndGroupIdsToGroups.get(new Object[] {userId, groupId});
  }

  @Override
  public void deleteAll(final String userId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");

    try {
      final Map<Object[], Group> groupIdsToGroups = this.userAndGroupIdsToGroups.prefixSubMap(new Object[] {userId});
      groupIdsToGroups.clear();
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }
  
  @Override
  public void deleteById(String userId, String groupId) {
    Preconditions.checkNotNull(userId, "The userId cannot be null!");
    Preconditions.checkNotNull(groupId, "The groupId cannot be null!");

    checkUtilization(userId, groupId);

    final Group group = this.userAndGroupIdsToGroups.remove(new Object[] {userId, groupId});
    Preconditions.checkArgument(group != null);
    
    this.db.commit();
  }

  private void checkUtilization(final String userId, final String groupId)
      throws IllegalStateException {
    final Set<String> utilizingBaseIds =
        this.utilizingBases.prefixSubMap(new Object[] {userId, groupId}).keySet().stream()
            .map(e -> (String) e[2]).collect(ImmutableSet.toImmutableSet());

    if (!utilizingBaseIds.isEmpty()) {
      final String jointUtilizingBaseIds = String.join(", ", utilizingBaseIds);
      throw new IllegalStateException(
          String.format("Some bases (%s) still refer to this group!", jointUtilizingBaseIds));
    }
  }

  @Override
  public Group merge(Group group) {
    Preconditions.checkNotNull(group, "The group cannot be null!");

    final String userId = group.getOwner().getEmail();
    final String groupId = group.getId();
    
    final Group previous = this.userAndGroupIdsToGroups.get(new Object[] {userId, groupId});
    if (previous == null) {
      create(group);
      return group;
    }
    
    final Group merged = previous.merge(group);
    replace(merged);
    
    return merged;
  }
  
  @Override
  public void initializeDefaults(final User owner) throws IOException {
    Preconditions.checkNotNull(owner, "The owner cannot be null!");
    
    final Iterator<File> groupPropertiesFileIterator = FileUtils.iterateFiles(this.initialGroupsPath.toFile(), GROUP_FILES_EXTENSIONS.toArray(new String[GROUP_FILES_EXTENSIONS.size()]), false);
    try {
      while (groupPropertiesFileIterator.hasNext()) {
        initializeFromPropertiesFile(owner, groupPropertiesFileIterator.next());
      }
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }

  private void initializeFromPropertiesFile(final User owner, final File propertiesFile) throws IOException {
    final Properties groupProperties = new Properties();
    groupProperties.load(new FileInputStream(propertiesFile));
    
    final GroupBuilder groupBuilder = new DefaultGroupBuilder();
    
    groupBuilder.setId(extractId(propertiesFile));
    groupBuilder.setOwner(owner);
    
    groupBuilder.setClassTypes(extractValues(INITIAL_GROUP_CLASS_TYPES_PROPERTY_KEY, groupProperties));
    groupBuilder.setDescriptionPredicates(extractValues(INITIAL_GROUP_DESCRIPTION_PREDICATES_PROPERTY_KEY, groupProperties));
    groupBuilder.setInstanceOfPredicates(extractValues(INITIAL_GROUP_TYPE_PREDICATES_PROPERTY_KEY, groupProperties));
    groupBuilder.setLabelPredicates(extractValues(INITIAL_GROUP_LABEL_PREDICATES_PROPERTY_KEY, groupProperties));
    groupBuilder.setPropertyTypes(extractValues(INITIAL_GROUP_PROPERTY_TYPES_PROPERTY_KEY, groupProperties));
    
    final Group group = groupBuilder.build();
    this.userAndGroupIdsToGroups.put(new Object[] { owner.getEmail(), group.getId()}, group);
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
