/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
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
import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.users.UserService;

/**
 * A {@link TaskRdfSerializationService} implementation employing {@link RDFMapper}.
 * 
 * @author Václav Brodec
 *
 */
public class TurtleRdfMappingTaskSerializationService implements TaskRdfSerializationService {

  private static final String VERSIONED_SERIALIZED_TASK_URI_SUFFIX_FORMAT = "SerializedTask/V2/%s";

  private final RDFMapper.Builder rdfMapperBuilder;
  private final UserService userService;
  private final FileService fileService;

  public TurtleRdfMappingTaskSerializationService(final RDFMapper.Builder rdfMapperBuilder,
      final UserService userService, final FileService fileService) {
    Preconditions.checkNotNull(rdfMapperBuilder);
    Preconditions.checkNotNull(userService);
    Preconditions.checkNotNull(fileService);

    this.rdfMapperBuilder = rdfMapperBuilder;
    this.userService = userService;
    this.fileService = fileService;
  }

  @Autowired
  public TurtleRdfMappingTaskSerializationService(final UserService userService,
      final FileService fileService) {
    this(RDFMapper.builder().set(MappingOptions.IGNORE_CARDINALITY_VIOLATIONS, false)
        .set(MappingOptions.IGNORE_INVALID_ANNOTATIONS, false), userService, fileService);
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

  private RDFMapper buildMapper(final URI baseUri) {
    return rdfMapperBuilder.namespace("", baseUri.resolve("SerializedTask/Node/").toString()).build();
  }

  private static String format(final Model model) {
    final StringWriter stringWriter = new StringWriter();

    Rio.write(model, stringWriter, RDFFormat.TURTLE);

    return stringWriter.toString();
  }

  private static void setRootSubjectIdentifier(final URI baseUri, final TaskValue taskValue) {
    taskValue.id(getRootSubjectIri(baseUri));
  }

  private static IRI getRootSubjectIri(final URI baseUri) {
    return SimpleValueFactory.getInstance()
        .createIRI(baseUri
            .resolve(String.format(VERSIONED_SERIALIZED_TASK_URI_SUFFIX_FORMAT, UUID.randomUUID()))
            .toString());
  }

  private static TaskValue toProxy(final Task task) {
    final TaskValue taskValue = new TaskValue();
    taskValue.setDescription(task.getDescription());
    taskValue.setConfiguration(new ConfigurationValue(task.getConfiguration()));
    return taskValue;
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
    final Configuration configuration = initializeConfiguration(configurationValue, input);

    final Task task = fromProxies(userId, taskId, taskValue, configuration);

    return task;
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

  private Task fromProxies(final String userId, final String taskId, final TaskValue taskValue,
      final Configuration configuration) {
    return new Task(userService.getUser(userId), taskId,
        taskValue.getDescription() == null ? "" : taskValue.getDescription(), configuration);
  }

  private TaskValue fromModel(final URI baseUri, final Model model) {
    return buildMapper(baseUri).readValue(model, TaskValue.class, getRootResource(model));
  }

  private static Resource getRootResource(final Model model) {
    final Model rootModel = model.filter((Resource) null, (IRI) null, getRootObject());
    Preconditions.checkArgument(!rootModel.isEmpty(), "Missing root resource!");

    return rootModel.iterator().next().getSubject();
  }

  private static IRI getRootObject() {
    return SimpleValueFactory.getInstance().createIRI("http://odalic.eu/internal/Task");
  }

  private static Configuration initializeConfiguration(final ConfigurationValue configurationValue,
      final File input) {
    return new Configuration(input,
        configurationValue.getUsedBases().stream().map(e -> e.toKnowledgeBase()).collect(
            ImmutableSet.toImmutableSet()),
        configurationValue.getPrimaryBase().toKnowledgeBase(),
        configurationValue.getFeedback().toFeedback(), configurationValue.getRowsLimit(),
        configurationValue.isStatistical());
  }

  private File getInput(final String userId, final ConfigurationValue configurationValue) {
    return fileService.getById(userId, configurationValue.getInput());
  }
}
