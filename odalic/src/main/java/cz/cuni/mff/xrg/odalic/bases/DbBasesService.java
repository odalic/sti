package cz.cuni.mff.xrg.odalic.bases;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import cz.cuni.mff.xrg.odalic.bases.proxies.KnowledgeBaseProxiesService;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.users.User;
import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;
import cz.cuni.mff.xrg.odalic.util.storage.DbService;

/**
 * This {@link BasesService} implementation persists the files in {@link DB}-backed maps.
 *
 * @author Václav Brodec
 */
@Component
public final class DbBasesService implements BasesService {

  private static final String BASE_PATH_PROPERTY_KEY = "sti.home";
  
  private static final String KNOWLEDGE_BASE_NAME_PROPERTY_KEY = "kb.name";

  private static final String ADVANCED_PROPERTIES_PATH_PROPERTY_KEY = "kb.advancedPropertiesPath";

  private static final String ADVANCED_TYPE_PROPERTY_KEY = "kb.advancedType";

  private static final String STOPLIST_PATH_PROPERTY_KEY = "kb.stopListFile";
  
  private static final String LANGUAGE_TAG_PROPERTY_KEY = "kb.languageSuffix";

  private static final String USER_RESOURCES_PREFIX_PROPERTY_KEY = "kb.insert.prefix.data";

  private static final String USER_CLASSES_PREFIX_PROPERTY_KEY = "kb.insert.prefix.schema";

  private static final String INSERT_GRAPH_PROPERTY_KEY = "kb.insert.graph";

  private static final String INSERT_SUPPORTED_PROEPRTY_KEY = "kb.insert.supported";

  private static final String ENDPOINT_PROPERTY_KEY = "kb.endpoint";
  
  private static final String USE_BIF_CONTAINS_PROPERTY_KEY = "kb.useBifContains";

  private static final String FULLTEXT_ENABLED_PROPERTY_KEY = "kb.fulltextEnabled";
  
  private static final String STOPLIST_CLASS_SECTION_START = "!invalid_clazz";

  private static final String STOPLIT_ATTRIBUTE_SECTION_START = "!invalid_attribute";

  private static final String STOPLIST_COMMENT_LINE_START = "#";
  
  private static final String LANGUAGE_TAG_SEPARATOR = "@";

  private static final Set<String> BASE_FILES_EXTENSIONS = ImmutableSet.of("properties");

  private static final Path DEFAULT_INITIAL_BASES_RELATIVE_PATH = Paths.get("config");
  
  private final KnowledgeBaseProxiesService knowledgeBaseProxiesService;

  private final GroupsService groupsService;

  private final AdvancedBaseTypesService advancedBaseTypesService;
  
  private final DB db;
  
  private final Path basePath;
  
  private final Path initialBasesPath;
  
  private final BTreeMap<Object[], KnowledgeBase> userAndBaseIdsToBases;

  private final BTreeMap<Object[], Boolean> utilizingTasks;

  @Autowired
  @SuppressWarnings("unchecked")
  public DbBasesService(final KnowledgeBaseProxiesService knowledgeBaseProxiesService,
      final GroupsService groupsService, final AdvancedBaseTypesService advancedBaseTypesService, final PropertiesService propertiesService, final DbService dbService) {
    Preconditions.checkNotNull(knowledgeBaseProxiesService);
    Preconditions.checkNotNull(groupsService);
    Preconditions.checkNotNull(advancedBaseTypesService);
    Preconditions.checkNotNull(propertiesService);
    Preconditions.checkNotNull(dbService);

    this.db = dbService.getDb();

    this.knowledgeBaseProxiesService = knowledgeBaseProxiesService;
    this.groupsService = groupsService;
    this.advancedBaseTypesService = advancedBaseTypesService;
    
    this.basePath = readApplicationBasePath(propertiesService);
    this.initialBasesPath = initializeInitialBasesPath(propertiesService);
    
    this.userAndBaseIdsToBases = this.db.treeMap("userAndBaseIdsToBases")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
    this.utilizingTasks = this.db.treeMap("utilizingTasks")
        .keySerializer(
            new SerializerArrayTuple(Serializer.STRING, Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.BOOLEAN).createOrOpen();
  }
  
  private static Path initializeInitialBasesPath(PropertiesService propertiesService) {
    final Path basePath = readApplicationBasePath(propertiesService);
    
    return basePath.resolve(DEFAULT_INITIAL_BASES_RELATIVE_PATH);
  }
  
  private static Path readApplicationBasePath(final PropertiesService propertiesService) {
    final Path basePath = Paths.get(propertiesService.get().getProperty(BASE_PATH_PROPERTY_KEY));
    Preconditions.checkArgument(basePath != null, "The base path key not found!");
    
    Preconditions.checkArgument(Files.exists(basePath), String.format("The base path %s does not exist!", basePath));
    
    return basePath;
  }

  @Override
  public NavigableSet<KnowledgeBase> getBases(final String userId) {
    Preconditions.checkNotNull(userId);

    return ImmutableSortedSet.copyOf(this.userAndBaseIdsToBases.prefixSubMap(new Object[] {userId}).values());
  }

  @Override
  public NavigableSet<KnowledgeBase> getInsertSupportingBases(final String userId) {
    Preconditions.checkNotNull(userId);

    return this.userAndBaseIdsToBases.prefixSubMap(new Object[] {userId}).values().stream().filter(e -> e.isInsertEnabled())
        .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
  }

  @Override
  public void create(final KnowledgeBase base) {
    Preconditions.checkArgument(!existsBaseWithId(base.getOwner().getEmail(), base.getName()));

    replace(base);
  }

  @Override
  public void replace(final KnowledgeBase base) {
    Preconditions.checkNotNull(base);

    final String userId = base.getOwner().getEmail();
    final String baseId = base.getName();

    final KnowledgeBase previous = this.userAndBaseIdsToBases.put(new Object[] {userId, baseId}, base);
    
    try {
      if (previous != null) {
        this.groupsService.unsubscribe(previous);
      }
  
      this.knowledgeBaseProxiesService.set(base);
      this.groupsService.subscribe(base);
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }

  @Override
  public KnowledgeBase merge(final KnowledgeBase base) {
    Preconditions.checkNotNull(base);

    final String userId = base.getOwner().getEmail();
    final String baseId = base.getName();

    final KnowledgeBase previous = this.userAndBaseIdsToBases.get(new Object[] {userId, baseId});
    if (previous == null) {
      create(base);
      return base;
    }

    return previous;
  }

  @Override
  public boolean existsBaseWithId(final String userId, final String baseId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(baseId);

    return this.userAndBaseIdsToBases.containsKey(new Object[]{userId, baseId});
  }

  @Override
  public KnowledgeBase getByName(String userId, String name) throws IllegalArgumentException {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(name);

    final KnowledgeBase base = this.userAndBaseIdsToBases.get(new Object[] {userId, name});
    Preconditions.checkArgument(base != null, "Unknown base!");

    return base;
  }

  @Override
  public KnowledgeBase verifyBaseExistenceByName(String userId, String name) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(name);

    return this.userAndBaseIdsToBases.get(new Object[] {userId, name});
  }

  @Override
  public void deleteAll(final String userId) {
    Preconditions.checkNotNull(userId);

    try {
      final Map<Object[], KnowledgeBase> baseIdsToBases = this.userAndBaseIdsToBases.prefixSubMap(new Object[] {userId});
      baseIdsToBases.entrySet().stream().forEach(e -> this.groupsService
          .unsubscribe(e.getValue()));
      baseIdsToBases.clear();
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }
  
  @Override
  public void deleteById(String userId, String name) throws IOException {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(name);

    checkUtilization(userId, name);

    try {
      final KnowledgeBase base = this.userAndBaseIdsToBases.remove(new Object[] {userId, name});
      Preconditions.checkArgument(base != null);

      this.knowledgeBaseProxiesService.delete(base);
      this.groupsService.unsubscribe(base);
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }

  private void checkUtilization(final String userId, final String name)
      throws IllegalStateException {
    final Set<String> utilizingTaskIds =
        this.utilizingTasks.prefixSubMap(new Object[] {userId, name}).keySet().stream()
            .map(e -> (String) e[2]).collect(ImmutableSet.toImmutableSet());

    if (!utilizingTaskIds.isEmpty()) {
      final String jointUtilizingTasksIds = String.join(", ", utilizingTaskIds);
      throw new IllegalStateException(
          String.format("Some tasks (%s) still refer to this base!", jointUtilizingTasksIds));
    }
  }

  @Override
  public void subscribe(final Task task) {
    final User taskOwner = task.getOwner();
    final String taskId = task.getId();

    final Set<KnowledgeBase> bases = task.getConfiguration().getUsedBases();

    try {
      for (final KnowledgeBase base : bases) {
        subscribe(taskOwner, taskId, base);
      }
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }

  private void subscribe(final User taskOwner, final String taskId, final KnowledgeBase base) {
    final User owner = base.getOwner();
    Preconditions.checkArgument(owner.equals(taskOwner),
        "The owner of the base is not the same as the owner of the task!");

    final String userId = owner.getEmail();
    final String baseName = base.getName();

    final Object[] userIdBaseName = new Object[] {userId, baseName};

    Preconditions.checkArgument(this.userAndBaseIdsToBases.get(userIdBaseName).equals(base),
        "The base is not registered!");

    final Boolean previous =
        this.utilizingTasks.put(new Object[] {userId, baseName, taskId}, true);
    Preconditions.checkArgument(previous == null,
        "The task has already been subcscribed to the base!");
  }

  @Override
  public void unsubscribe(final Task task) {
    final User taskOwner = task.getOwner();
    final String taskId = task.getId();

    final Set<KnowledgeBase> bases = task.getConfiguration().getUsedBases();

    try {
      for (final KnowledgeBase base : bases) {
        unsubscribe(taskOwner, taskId, base);
      }
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }
    
    this.db.commit();
  }

  private void unsubscribe(final User taskOwner, final String taskId, final KnowledgeBase base) {
    final User owner = base.getOwner();
    Preconditions.checkArgument(owner.equals(taskOwner),
        "The owner of the base is not the same as the owner of the task!");

    final String userId = owner.getEmail();
    final String baseName = base.getName();

    Preconditions.checkArgument(
        this.userAndBaseIdsToBases.get(new Object[] {userId, baseName}).equals(base),
        "The base is not registered!");

    final Boolean removed = this.utilizingTasks.remove(new Object[] {userId, baseName, taskId});
    Preconditions.checkArgument(removed != null, "The task is not subcscribed to the base!");

    Preconditions.checkArgument(removed, "The task is not subcscribed to the base!");
  }
  
  @Override
  public void initializeDefaults(User owner) throws IOException {
    Preconditions.checkNotNull(owner);

    final Iterator<File> basePropertiesFileIterator =
        FileUtils.iterateFiles(this.initialBasesPath.toFile(),
            BASE_FILES_EXTENSIONS.toArray(new String[BASE_FILES_EXTENSIONS.size()]), false);
    while (basePropertiesFileIterator.hasNext()) {
      initializeFromPropertiesFile(owner, basePropertiesFileIterator.next());
    }
  }

  private void initializeFromPropertiesFile(final User owner, final File propertiesFile)
      throws IOException {
    final Properties baseProperties = new Properties();
    baseProperties.load(new FileInputStream(propertiesFile));

    if (!isBaseProperties(baseProperties)) {
      return;
    }

    final KnowledgeBaseBuilder baseBuilder = new KnowledgeBaseBuilder();

    baseBuilder.setName(baseProperties.getProperty(KNOWLEDGE_BASE_NAME_PROPERTY_KEY));
    baseBuilder.setOwner(owner);

    baseBuilder.setDescription(baseProperties.getProperty(KNOWLEDGE_BASE_NAME_PROPERTY_KEY,
        "Initially present knowledge base definition"));

    final String advancedTypeId = baseProperties.getProperty(ADVANCED_TYPE_PROPERTY_KEY);
    if (advancedTypeId == null) {
      baseBuilder.setAdvancedType(this.advancedBaseTypesService.getDefault());
    } else {
      baseBuilder.setAdvancedType(this.advancedBaseTypesService.getType(advancedTypeId));
    }
    baseBuilder.setAdvancedProperties(extractAdvancedProperties(baseProperties));

    final URL endpointUrl = new URL(baseProperties.getProperty(ENDPOINT_PROPERTY_KEY));
    baseBuilder.setEndpoint(endpointUrl);

    baseBuilder
        .setInsertEnabled(Boolean.parseBoolean(baseProperties.getProperty(INSERT_SUPPORTED_PROEPRTY_KEY)));

    final String insertGraphValue = baseProperties.getProperty(INSERT_GRAPH_PROPERTY_KEY);
    if (insertGraphValue != null) {
      baseBuilder.setInsertGraph(URI.create(insertGraphValue));
    }

    final String userClassesPrefixValue = baseProperties.getProperty(USER_CLASSES_PREFIX_PROPERTY_KEY);
    if (userClassesPrefixValue != null) {
      baseBuilder.setUserClassesPrefix(URI.create(userClassesPrefixValue));
    }

    final String userResourcesPrefixValue = baseProperties.getProperty(USER_RESOURCES_PREFIX_PROPERTY_KEY);
    if (userResourcesPrefixValue != null) {
      baseBuilder.setUserResourcesPrefix(URI.create(userResourcesPrefixValue));
    }

    final String languageTagValue = baseProperties.getProperty(LANGUAGE_TAG_PROPERTY_KEY);
    final String languageTag = languageTagValue.startsWith(LANGUAGE_TAG_SEPARATOR) ? languageTagValue.substring(1, languageTagValue.length()) : languageTagValue;
    baseBuilder.setLanguageTag(languageTag);

    baseBuilder.setGroupsAutoSelected(true);
    baseBuilder.setSelectedGroups(ImmutableSet.of());

    baseBuilder.setTextSearchingMethod(extractTextSearchingMethod(baseProperties));

    setSkipped(baseBuilder, baseProperties);

    create(baseBuilder.build());
  }

  private void setSkipped(final KnowledgeBaseBuilder baseBuilder, final Properties baseProperties) throws IOException {
    final String stoplistPathValue = baseProperties.getProperty(STOPLIST_PATH_PROPERTY_KEY);
    if (stoplistPathValue == null) {
      return;
    }

    final Path stoplistPath = this.basePath.resolve(stoplistPathValue);

    parseStoplist(baseBuilder, stoplistPath);
  }

  private static void parseStoplist(final KnowledgeBaseBuilder baseBuilder, final Path stoplistPath)
      throws IOException {
    boolean attributesReading = false;
    boolean classesReading = false;

    final LineIterator stoplistLinesIterator =
        FileUtils.lineIterator(stoplistPath.toFile(), StandardCharsets.UTF_8.name());
    try {
      while (stoplistLinesIterator.hasNext()) {
        final String line = stoplistLinesIterator.next();
        if (line.startsWith(STOPLIST_COMMENT_LINE_START)) {
          continue; // Comment.
        }

        if (line.startsWith(STOPLIT_ATTRIBUTE_SECTION_START)) {
          attributesReading = true;
          classesReading = false;

          continue;
        } else if (line.startsWith(STOPLIST_CLASS_SECTION_START)) {
          attributesReading = false;
          classesReading = true;

          continue;
        }

        if (attributesReading) {
          baseBuilder.addSkippedAttribute(line);
        } else if (classesReading) {
          baseBuilder.addSkippedClass(line);
        }
      }
    } finally {
      stoplistLinesIterator.close();
    }
  }

  private static TextSearchingMethod extractTextSearchingMethod(final Properties properties) {
    final boolean fulltextEnabled =
        Boolean.parseBoolean(properties.getProperty(FULLTEXT_ENABLED_PROPERTY_KEY));
    final boolean useBifContaines =
        Boolean.parseBoolean(properties.getProperty(USE_BIF_CONTAINS_PROPERTY_KEY));

    if (fulltextEnabled) {
      if (useBifContaines) {
        return TextSearchingMethod.FULLTEXT;
      } else {
        return TextSearchingMethod.SUBSTRING;
      }
    } else {
      return TextSearchingMethod.EXACT;
    }
  }

  private Map<String, String> extractAdvancedProperties(final Properties baseProperties)
      throws IOException {
    final String advacedPropertiesPathValue =
        baseProperties.getProperty(ADVANCED_PROPERTIES_PATH_PROPERTY_KEY);
    if (advacedPropertiesPathValue == null) {
      return ImmutableMap.of();
    }

    final Path advancedPropertiesPath = this.initialBasesPath.resolve(advacedPropertiesPathValue);

    final Properties advancedProperties = new Properties();
    try (final InputStream advancedPropertiesInputStream =
        new FileInputStream(advancedPropertiesPath.toFile())) {
      advancedProperties.load(advancedPropertiesInputStream);
    }

    return Maps.fromProperties(advancedProperties);
  }

  private boolean isBaseProperties(final Properties properties) {
    return properties.containsKey(KNOWLEDGE_BASE_NAME_PROPERTY_KEY);
  }
}
