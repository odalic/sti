package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.util.Map;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;
import uk.ac.shef.dcs.kbproxy.KnowledgeBaseProxy;

/**
 * Created by Jan
 */
public interface KnowledgeBaseProxiesProvider {
  /**
   * Provides the proxies.
   *
   * @return the KB proxies implementations map from their names
   */
  Map<String, KnowledgeBaseProxy> getKBProxies();
  
  void set(final KnowledgeBase base);

  PrefixMappingService getPrefixService();
}
