package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import cz.cuni.mff.xrg.odalic.bases.proxies.KnowledgeBaseProxiesService;

/**
 * JUnit prototype test with Spring
 * 
 * @author Josef Janoušek
 *
 */
@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml"})
public class SpringPrototypeTest {

  private static final Logger log = LoggerFactory.getLogger(SpringPrototypeTest.class);

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  @Lazy
  private KnowledgeBaseProxiesService knowledgeBaseProxiesService;

  @BeforeClass
  public static void beforeClass() {
    System.setProperty("cz.cuni.mff.xrg.odalic.sti", Paths.get("").toAbsolutePath()
        .resolveSibling("config").resolve("sti.properties").toString());
  }

  @Before
  public void beforeTest() {
  }

  @After
  public void afterTest() {
  }

  @Test
  public void testSomething() {

    try {
      log.info("Result: " + (knowledgeBaseProxiesService.getPrefixService() == null));
    } catch (Exception e) {
      log.info("KnowledgeBaseProxyFactory is not available, so test was stopped: ", e.getMessage());
    }
  }
}
