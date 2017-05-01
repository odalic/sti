package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import cz.cuni.mff.xrg.odalic.bases.proxies.KnowledgeBaseProxiesService;

/**
 * JUnit test for CSV export
 * 
 * @author Josef Janoušek
 *
 */
@ContextConfiguration(locations = {"classpath:src/test/resources/applicationContext.xml"})
public class SpringPrototypeTest {

  @SuppressWarnings("unused")
  @Autowired
  private KnowledgeBaseProxiesService knowledgeBaseProxiesService;
  
  @Before
  public void beforeTest() throws URISyntaxException, IOException {
  }
  
  @After
  public void afterTest() {
  }

  @Test
  public void testSomething() {
  }
}
