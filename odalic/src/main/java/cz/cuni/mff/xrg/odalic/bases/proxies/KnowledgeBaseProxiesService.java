package cz.cuni.mff.xrg.odalic.bases.proxies;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;
import uk.ac.shef.dcs.kbproxy.Proxy;

/**
 * Created by Jan
 */
public interface KnowledgeBaseProxiesService {
  /**
   * Provides the proxies.
   * @param bases 
   *
   * @return the proxies
   */
  Table<String, String, Proxy> toProxies(Set<? extends KnowledgeBase> bases);
  
  void set(final KnowledgeBase base);

  PrefixMappingService getPrefixService();

  void delete(KnowledgeBase base) throws IOException;
}
