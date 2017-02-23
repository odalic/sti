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

  private final RDFMapper rdfMapper;
  private final UserService userService;
  private final FileService fileService;

  public TurtleRdfMappingTaskSerializationService(final RDFMapper rdfMapper,
      final UserService userService, final FileService fileService) {
    Preconditions.checkNotNull(rdfMapper);
    Preconditions.checkNotNull(userService);
    Preconditions.checkNotNull(fileService);

    this.rdfMapper = rdfMapper;
    this.userService = userService;
    this.fileService = fileService;
  }

  @Autowired
  public TurtleRdfMappingTaskSerializationService(final UserService userService,
      final FileService fileService) {
    this(
        RDFMapper.builder().set(MappingOptions.IGNORE_CARDINALITY_VIOLATIONS, false)
            .set(MappingOptions.IGNORE_INVALID_ANNOTATIONS, false).build(),
        userService, fileService);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.api.rdf.TaskRdfSerializationService#serialize(cz.cuni.mff.xrg.odalic.
   * tasks.Task, java.net.URI)
   */
  @Override
  public String serialize(final Task task, final URI baseUri) {
    Preconditions.checkNotNull(task);
    Preconditions.checkNotNull(baseUri);

    final TaskValue taskValue = toProxy(task);

    setRootSubjectIdentifier(baseUri, taskValue);

    final Model model = toModel(taskValue);
    return format(model);
  }

  private Model toModel(final TaskValue proxy) {
    return rdfMapper.writeValue(proxy);
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.api.rdf.TaskRdfSerializationService#deserialize(java.io.InputStream,
   * java.lang.String, java.lang.String, java.net.URI)
   */
  @Override
  public Task deserialize(final InputStream taskStream, final String userId, final String taskId,
      final URI baseUri) throws IOException {
    final Model model = parse(taskStream);

    final TaskValue taskValue = fromModel(baseUri, model);

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
    return rdfMapper.readValue(model, TaskValue.class, getRootSubjectIri(baseUri));
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
