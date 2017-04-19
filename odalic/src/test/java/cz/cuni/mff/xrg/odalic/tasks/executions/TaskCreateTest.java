package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.api.rest.values.ConfigurationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.CredentialsValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.TaskValue;
import cz.cuni.mff.xrg.odalic.bases.DefaultKnowledgeBaseBuilder;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBaseBuilder;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.groups.DefaultGroupBuilder;
import cz.cuni.mff.xrg.odalic.groups.GroupBuilder;
import cz.cuni.mff.xrg.odalic.users.Credentials;
import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * JUnit test for creating tasks with test files
 * 
 * @author Josef Janoušek
 *
 */
@RunWith(value = Parameterized.class)
public class TaskCreateTest {

  private static final Logger log = LoggerFactory.getLogger(TaskCreateTest.class);

  private static Client client;
  private static WebTarget target;
  private static String token;
  private static boolean run = true;

  private File file;
  private Format format;
  private int rowsLimit;
  private boolean statistical;

  public TaskCreateTest(File file, Format format, int rowsLimit, boolean statistical) {

    this.file = file;
    this.format = format;
    this.rowsLimit = rowsLimit;
    this.statistical = statistical;
  }

  @Parameters
  public static Collection<Object[]> data() throws URISyntaxException {

    return Arrays.asList(new Object[][] {{new File(
        TaskCreateTest.class.getClassLoader().getResource("book-input.csv").toURI()),
        new Format(StandardCharsets.ISO_8859_1, ';', true, '"', null, null), 10, false}});
  }

  @BeforeClass
  public static void beforeClass() {

    client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
    target = client.target("http://localhost:8080/odalic");

    try {
      Response userResponse = target.path("users").path("authentications").request().post(
          Entity.json(new CredentialsValue(new Credentials("odalic@email.cz", "admin"))));
      token = "Bearer " + ((HashMap<?,?>)userResponse.readEntity(HashMap.class).get("payload"))
          .get("token");
      userResponse.close();
    } catch (ProcessingException e) {
      log.info("Server is not running, so test was stopped: " + e.getMessage());
      run = false;
    }
  }

  @Test
  public void TestFileCreateTask() {

    if (!run) {
      return;
    }

    // File settings
    FileDataBodyPart filePart = new FileDataBodyPart("input", file);
    FormDataMultiPart multipart = new FormDataMultiPart();
    multipart.bodyPart(filePart);

    Response fileResponse = target.path("files").path(file.getName()).request()
        .header(HttpHeaders.AUTHORIZATION, token)
        .put(Entity.entity(multipart, multipart.getMediaType()));
    fileResponse.close();

    // Format settings
    Response formatResponse = target.path("files").path(file.getName()).path("format")
        .request().header(HttpHeaders.AUTHORIZATION, token).put(Entity.json(format));
    formatResponse.close();

    // Task settings
    ConfigurationValue configuration = new ConfigurationValue();
    configuration.setInput(file.getName());
    configuration.setFeedback(CoreExecutionBatch.createFeedback(true));
    configuration.setUsedBases(ImmutableSet.of(getDummyBase("DBpedia"),
        getDummyBase("DBpedia Clone"), getDummyBase("German DBpedia")));
    configuration.setPrimaryBase(getDummyBase("DBpedia"));
    configuration.setRowsLimit(rowsLimit);
    configuration.setStatistical(statistical);

    TaskValue task = new TaskValue();
    task.setId(file.getName() + "_task");
    task.setCreated(new Date());
    task.setDescription(file.getName() + " task description");
    task.setConfiguration(configuration);

    Response taskResponse = target.path("tasks").path(task.getId()).request()
        .header(HttpHeaders.AUTHORIZATION, token).put(Entity.json(task));
    taskResponse.close();
  }

  @AfterClass
  public static void afterClass() {

    client.close();
  }
  
  private static KnowledgeBase getDummyBase(final String name) {    
    final User owner = new User("dummy@dummy.com", "dummyHash", Role.ADMINISTRATOR);
    
    final GroupBuilder groupBuilder = new DefaultGroupBuilder();
    groupBuilder.setId("DummyGroup");
    groupBuilder.setOwner(owner);
    
    final KnowledgeBaseBuilder builder = new DefaultKnowledgeBaseBuilder();
    
    builder.setName(name);
    builder.setOwner(owner);
    
    try {
      builder.setEndpoint(new URL("http://dummy.com"));
    } catch (final MalformedURLException e) {
      throw new RuntimeException(e);
    }
    
    builder.addSelectedGroup(groupBuilder.build());
        
    return builder.build();
  }
}
