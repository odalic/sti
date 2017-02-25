package cz.cuni.mff.xrg.odalic.tasks.executions;

import java.util.Map;

import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;
import uk.ac.shef.dcs.kbproxy.KBProxy;

/**
 * Created by Jan
 */
public interface KnowledgeBaseProxyFactory {
  /**
   * Lazily initializes the KB searches.
   *
   * @return the KB search implementations
   */
  Map<String, KBProxy> getKBProxies();

  PrefixMappingService getPrefixService();
}
