package cz.cuni.mff.xrg.odalic.tasks.executions;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.KBProxyFactory;
import uk.ac.shef.dcs.sti.STIException;

import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;

/**
 * Created by Jan
 */
public class DefaultKnowledgeBaseProxyFactory implements KnowledgeBaseProxyFactory {

  private static final String PROPERTY_HOME = "sti.home";
  private static final String PROPERTY_PROXY_PROP_FILE = "sti.kbproxy.propertyfile";
  private static final String PROPERTY_CACHE_FOLDER = "sti.cache.main.dir";

  private static final Logger logger = LoggerFactory.getLogger(DefaultKnowledgeBaseProxyFactory.class);

  private Map<String, KBProxy> kbProxies;
  private Lock initLock = new ReentrantLock();
  private boolean isInitialized = false;

  private final PrefixMappingService prefixService;
  private final String propertyFilePath;
  private Properties properties;

  public DefaultKnowledgeBaseProxyFactory(PrefixMappingService prefixService, String propertyFilePath) throws STIException, IOException {
    Preconditions.checkNotNull(prefixService);
    Preconditions.checkNotNull(propertyFilePath);

    this.prefixService = prefixService;
    this.propertyFilePath = propertyFilePath;
    
    initComponents(prefixService);
  }

  @Autowired
  public DefaultKnowledgeBaseProxyFactory(PrefixMappingService prefixService) throws STIException, IOException {
    this(prefixService, System.getProperty("cz.cuni.mff.xrg.odalic.sti"));
  }

  @Override
  public Map<String, KBProxy> getKBProxies() {
    return kbProxies;
  }

  private void initComponents(PrefixMappingService prefixService) throws STIException, IOException {
    initLock.lock();
    try {
      if (isInitialized) {
        return;
      }

      properties = new Properties();
      properties.load(new FileInputStream(propertyFilePath));

      Map<String, String> prefixMap = null;
      if (prefixService != null) {
        prefixMap = prefixService.getPrefixToUriMap();
      }

      // object to fetch things from KB
      Collection<KBProxy> kbProxyInstances = initKBProxies(prefixMap);

      for (KBProxy kbProxy : kbProxyInstances){
        initKBCache(kbProxy);
      }

      kbProxies = kbProxyInstances.stream().collect(Collectors.toMap(KBProxy::getName, item -> item));

      isInitialized = true;
    } finally {
      initLock.unlock();
    }
  }

  private Collection<KBProxy> initKBProxies(Map<String, String> prefixToUriMap) throws STIException {
    logger.info("Initializing KBProxy ...");
    try {
      KBProxyFactory fbf = new KBProxyFactory();
      return fbf.createInstances(
          properties.getProperty(PROPERTY_PROXY_PROP_FILE),
          properties.getProperty(PROPERTY_CACHE_FOLDER),
          properties.getProperty(PROPERTY_HOME),
          prefixToUriMap);
    } catch (Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing KBProxies.", e);
    }
  }

  private void initKBCache(KBProxy kbProxy) throws STIException {
    logger.info("Initializing KB cache ...");
    try {
      kbProxy.initializeCaches();
    }
    catch (KBProxyException e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing KBProxy cache.", e);
    }
  }

  @Override
  public PrefixMappingService getPrefixService() {
    return prefixService;
  }
}
