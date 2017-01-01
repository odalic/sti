package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
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

import cz.cuni.mff.xrg.odalic.api.rest.values.ConfigurationValue;
import cz.cuni.mff.xrg.odalic.api.rest.values.TaskValue;
import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * JUnit test for creating tasks with test files
 * 
 * @author Josef Janoušek
 *
 */
@RunWith(value = Parameterized.class)
public class TaskCreateTest {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TaskCreateTest.class);

  private static Client client;
  private static WebTarget target;

  private File file;
  private Format format;
  private int rowsLimit;

  public TaskCreateTest(File file, Format format, int rowsLimit) {

    this.file = file;
    this.format = format;
    this.rowsLimit = rowsLimit;
  }

  @Parameters
  public static Collection<Object[]> data() {

    String directory = "d:\\Documents\\Odalic\\TestFiles";

    return Arrays.asList(new Object[][]{
      {new File(directory, "Books-v2.csv"),
        new Format(StandardCharsets.UTF_8, ';', true, true, false, null, null, null),
        10}
    });
  }

  @BeforeClass
  public static void beforeClass() {

    client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
    target = client.target("http://localhost:8080/odalic");
  }

  @Test
  public void TestFileCreateTask() {

    // File settings
    FileDataBodyPart filePart = new FileDataBodyPart("input", file);
    FormDataMultiPart multipart = new FormDataMultiPart();
    multipart.bodyPart(filePart);

    Response fileResponse = target.path("files").path(file.getName()).request()
        .put(Entity.entity(multipart, multipart.getMediaType()));
    fileResponse.close();

    // Format settings
    Response formatResponse = target.path("files").path(file.getName()).path("format").request()
        .put(Entity.json(format));
    formatResponse.close();

    // Task settings
    ConfigurationValue configuration = new ConfigurationValue();
    configuration.setInput(file.getName());
    configuration.setFeedback(new Feedback());
    configuration.setPrimaryBase(new KnowledgeBase("DBpedia"));
    configuration.setRowsLimit(rowsLimit);

    TaskValue task = new TaskValue();
    task.setId(file.getName() + "_task");
    task.setCreated(new Date());
    task.setDescription(file.getName() + " task description");
    task.setConfiguration(configuration);

    Response taskResponse = target.path("tasks").path(task.getId()).request()
        .put(Entity.json(task));
    taskResponse.close();
  }

  @AfterClass
  public static void afterClass() {

    client.close();
  }
}
