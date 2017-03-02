package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;
import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.KBProxyFactory;
import uk.ac.shef.dcs.sti.STIException;

/**
 * Created by Jan
 */
public class DefaultKnowledgeBaseProxyFactory implements KnowledgeBaseProxyFactory {

  private static final String PROPERTY_HOME = "sti.home";
  private static final String PROPERTY_PROXY_PROP_FILE = "sti.kbproxy.propertyfile";
  private static final String PROPERTY_CACHE_FOLDER = "sti.cache.main.dir";

  private static final Logger logger =
      LoggerFactory.getLogger(DefaultKnowledgeBaseProxyFactory.class);

  private Map<String, KBProxy> kbProxies;
  private final Lock initLock = new ReentrantLock();
  private boolean isInitialized = false;

  private final PrefixMappingService prefixService;
  private final String propertyFilePath;
  private Properties properties;

  @Autowired
  public DefaultKnowledgeBaseProxyFactory(final PrefixMappingService prefixService)
      throws STIException, IOException {
    this(prefixService, System.getProperty("cz.cuni.mff.xrg.odalic.sti"));
  }

  public DefaultKnowledgeBaseProxyFactory(final PrefixMappingService prefixService,
      final String propertyFilePath) throws STIException, IOException {
    Preconditions.checkNotNull(prefixService);
    Preconditions.checkNotNull(propertyFilePath);

    this.prefixService = prefixService;
    this.propertyFilePath = propertyFilePath;

    initComponents(prefixService);
  }

  @Override
  public Map<String, KBProxy> getKBProxies() {
    return this.kbProxies;
  }

  @Override
  public PrefixMappingService getPrefixService() {
    return this.prefixService;
  }

  private void initComponents(final PrefixMappingService prefixService)
      throws STIException, IOException {
    this.initLock.lock();
    try {
      if (this.isInitialized) {
        return;
      }

      this.properties = new Properties();
      this.properties.load(new FileInputStream(this.propertyFilePath));

      Map<String, String> prefixMap = null;
      if (prefixService != null) {
        prefixMap = prefixService.getPrefixToUriMap();
      }

      // object to fetch things from KB
      final Collection<KBProxy> kbProxyInstances = initKBProxies(prefixMap);

      for (final KBProxy kbProxy : kbProxyInstances) {
        initKBCache(kbProxy);
      }

      this.kbProxies =
          kbProxyInstances.stream().collect(Collectors.toMap(KBProxy::getName, item -> item));

      this.isInitialized = true;
    } finally {
      this.initLock.unlock();
    }
  }

  private void initKBCache(final KBProxy kbProxy) throws STIException {
    logger.info("Initializing KB cache ...");
    try {
      kbProxy.initializeCaches();
    } catch (final KBProxyException e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing KBProxy cache.", e);
    }
  }

  private Collection<KBProxy> initKBProxies(final Map<String, String> prefixToUriMap)
      throws STIException {
    logger.info("Initializing KBProxy ...");
    try {
      final KBProxyFactory fbf = new KBProxyFactory();
      return fbf.createInstances(this.properties.getProperty(PROPERTY_PROXY_PROP_FILE),
          this.properties.getProperty(PROPERTY_CACHE_FOLDER),
          this.properties.getProperty(PROPERTY_HOME), prefixToUriMap);
    } catch (final Exception e) {
      logger.error("Exception", e.getLocalizedMessage(), e.getStackTrace());
      throw new STIException("Failed initializing KBProxies.", e);
    }
  }
}
