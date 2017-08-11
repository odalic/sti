package uk.ac.shef.dcs.kbproxy.sparql;

import java.io.Serializable;
import java.util.Map;

import uk.ac.shef.dcs.kbproxy.ProxyCoreFactory;
import uk.ac.shef.dcs.kbproxy.ProxyDefinition;
import uk.ac.shef.dcs.kbproxy.ProxyCore;

import cz.cuni.mff.xrg.odalic.util.logging.PerformanceLogger;

public final class SparqlProxyCoreFactory implements ProxyCoreFactory, Serializable {

  private static final long serialVersionUID = -2225350547844439817L;

  public ProxyCore create(final ProxyDefinition definition, final Map<String, String> prefixesToUris, final PerformanceLogger performanceLogger) throws ClassCastException {
    final SparqlProxyDefinition castDefinition = SparqlProxyDefinition.class.cast(definition);
    
    return new SparqlProxyCore(castDefinition, prefixesToUris, performanceLogger);
  }
  
}
