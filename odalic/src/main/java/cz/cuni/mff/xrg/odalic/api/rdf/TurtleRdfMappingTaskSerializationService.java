/**
 *
 */
package cz.cuni.mff.xrg.odalic.api.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.BadRequestException;

import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import com.complexible.pinto.MappingOptions;
import com.complexible.pinto.RDFMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rdf.values.ConfigurationValue;
import cz.cuni.mff.xrg.odalic.api.rdf.values.TaskValue;
import cz.cuni.mff.xrg.odalic.bases.BasesService;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * <p>
 * A {@link TaskSerializationService} implementation employing {@link RDFMapper}.
 * </p>
 * 
 * <p>
 * Apart from the equivalent classes for the REST API values, the adapters are not available, so the
 * conversion from mapped "values" to domain objects and back is unfortunately strictly manual.
 * </p>
 *
 * @author Václav Brodec
 *
 */
public class TurtleRdfMappingTaskSerializationService implements TaskSerializationService {

  private static final String VERSIONED_SERIALIZED_TASK_URI_SUFFIX_FORMAT = "SerializedTask/V4/%s";

  private static String format(final Model model) {
    final StringWriter stringWriter = new StringWriter();

    Rio.write(model, stringWriter, RDFFormat.TURTLE);

    return stringWriter.toString();
  }

  private static IRI getRootObject() {
    return SimpleValueFactory.getInstance().createIRI("http://odalic.eu/internal/Task");
  }

  private static Resource getRootResource(final Model model) {
    final Model rootModel = model.filter((Resource) null, (IRI) null, getRootObject());
    Preconditions.checkArgument(!rootModel.isEmpty(), "Missing root resource!");

    return rootModel.iterator().next().getSubject();
  }

  private static IRI getRootSubjectIri(final URI baseUri) {
    return SimpleValueFactory.getInstance()
        .createIRI(baseUri
            .resolve(String.format(VERSIONED_SERIALIZED_TASK_URI_SUFFIX_FORMAT, UUID.randomUUID()))
            .toString());
  }

  private static Model parse(final InputStream taskStream) throws IOException {
    final Model model;
    try {
      model = Rio.parse(taskStream, "http://odalic.eu/internal/", RDFFormat.TURTLE);
    } catch (final RDFParseException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return model;
  }

  private static void setRootSubjectIdentifier(final URI baseUri, final TaskValue taskValue) {
    taskValue.id(getRootSubjectIri(baseUri));
  }

  private TaskValue toProxy(final Task task) {
    final String userId = task.getOwner().getEmail();

    final Set<KnowledgeBase> usedBases = task.getConfiguration().getUsedBases().stream()
        .map(e -> this.basesService.getByName(userId, e)).collect(ImmutableSet.toImmutableSet());

    return new TaskValue(task, usedBases);
  }

  private final RDFMapper.Builder rdfMapperBuilder;

  private final UserService userService;

  private final FileService fileService;

  private final KnowledgeBaseSerializationService knowledgeBaseSerializationService;

  private final BasesService basesService;

  public TurtleRdfMappingTaskSerializationService(final RDFMapper.Builder rdfMapperBuilder,
      final UserService userService, final FileService fileService,
      final KnowledgeBaseSerializationService knowledgeBaseSerializationService,
      final BasesService basesService) {
    Preconditions.checkNotNull(rdfMapperBuilder);
    Preconditions.checkNotNull(userService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(knowledgeBaseSerializationService);
    Preconditions.checkNotNull(basesService);

    this.rdfMapperBuilder = rdfMapperBuilder;
    this.userService = userService;
    this.fileService = fileService;
    this.knowledgeBaseSerializationService = knowledgeBaseSerializationService;
    this.basesService = basesService;
  }

  @Autowired
  public TurtleRdfMappingTaskSerializationService(final UserService userService,
      final FileService fileService,
      final KnowledgeBaseSerializationService knowledgeBaseSerializationService,
      final BasesService basesService) {
    this(
        RDFMapper.builder().set(MappingOptions.IGNORE_CARDINALITY_VIOLATIONS, false)
            .set(MappingOptions.IGNORE_INVALID_ANNOTATIONS, false),
        userService, fileService, knowledgeBaseSerializationService, basesService);
  }

  private RDFMapper buildMapper(final URI baseUri) {
    return this.rdfMapperBuilder.namespace("", baseUri.resolve("SerializedTask/Node/").toString())
        .build();
  }

  @Override
  public Task deserialize(final InputStream taskStream, final String userId, final String taskId,
      final URI baseUri) throws IOException {
    final Model model = parse(taskStream);

    final TaskValue taskValue;
    try {
      taskValue = fromModel(baseUri, model);
    } catch (final IllegalArgumentException e) {
      throw new BadRequestException(e.getMessage(), e);
    }

    final ConfigurationValue configurationValue = taskValue.getConfiguration();
    if (configurationValue == null) {
      throw new BadRequestException("No configuration provided!");
    }

    final File input = getInput(userId, configurationValue);
    final Configuration configuration = initializeConfiguration(userId, configurationValue, input);

    final Task task = fromProxies(userId, taskId, taskValue, configuration);

    return task;
  }

  private TaskValue fromModel(final URI baseUri, final Model model) {
    return buildMapper(baseUri).readValue(model, TaskValue.class, getRootResource(model));
  }

  private Task fromProxies(final String userId, final String taskId, final TaskValue taskValue,
      final Configuration configuration) {
    return new Task(this.userService.getUser(userId), taskId,
        taskValue.getDescription() == null ? "" : taskValue.getDescription(), configuration);
  }

  private File getInput(final String userId, final ConfigurationValue configurationValue) {
    return this.fileService.getById(userId, configurationValue.getInput());
  }

  private Configuration initializeConfiguration(final String userId,
      final ConfigurationValue configurationValue, final File input) {
    final Set<KnowledgeBase> usedBases = extractUsedBases(userId, configurationValue);

    final String primaryBaseName = configurationValue.getPrimaryBase();
    final KnowledgeBase primaryBase = this.basesService.getByName(userId, primaryBaseName);

    Preconditions.checkArgument(usedBases.contains(primaryBase),
        "The primary base not among the used ones!");

    return new Configuration(input,
        usedBases.stream().map(e -> e.getName()).collect(ImmutableSet.toImmutableSet()),
        primaryBase.getName(), configurationValue.getFeedback().toFeedback(),
        configurationValue.getRowsLimit(), configurationValue.isStatistical());
  }

  private Set<KnowledgeBase> extractUsedBases(final String userId,
      final ConfigurationValue configurationValue) {
    final Set<KnowledgeBase> usedBases = configurationValue.getUsedBases().stream()
        .map(e -> this.knowledgeBaseSerializationService.deserialize(userId, e))
        .collect(ImmutableSet.toImmutableSet());

    for (final KnowledgeBase usedBase : usedBases) {
      this.basesService.merge(usedBase);
    }

    return usedBases;
  }

  @Override
  public String serialize(final Task task, final URI baseUri) {
    Preconditions.checkNotNull(task);
    Preconditions.checkNotNull(baseUri);

    final TaskValue taskValue = toProxy(task);

    setRootSubjectIdentifier(baseUri, taskValue);

    final Model model = toModel(taskValue, baseUri);
    return format(model);
  }

  private Model toModel(final TaskValue proxy, final URI baseUri) {
    return buildMapper(baseUri).writeValue(proxy);
  }
}
