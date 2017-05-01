package uk.ac.shef.dcs.kbproxy.sparql;

import java.util.Map;

import uk.ac.shef.dcs.kbproxy.ProxyCoreFactory;
import uk.ac.shef.dcs.kbproxy.ProxyDefinition;
import uk.ac.shef.dcs.kbproxy.ProxyCore;

public final class SparqlProxyCoreFactory implements ProxyCoreFactory {

  public ProxyCore create(final ProxyDefinition definition, final Map<String, String> prefixesToUris) throws ClassCastException {
    final SparqlProxyDefinition castDefinition = SparqlProxyDefinition.class.cast(definition);
    
    return new SparqlProxyCore(castDefinition, prefixesToUris);
  }
  
}
