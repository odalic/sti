package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(locations = {"classpath:src/test/resources/applicationContext.xml"})
public class SpringPrototypeTest {

  private static final Logger log = LoggerFactory.getLogger(SpringPrototypeTest.class);

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  private KnowledgeBaseProxiesService knowledgeBaseProxiesService;

  @Before
  public void beforeTest() {
  }

  @After
  public void afterTest() {
  }

  @Test
  @Ignore
  public void testSomething() {
    log.info("Result: " + (knowledgeBaseProxiesService == null));
  }
}
