package uk.ac.shef.dcs.kbproxy.sparql.pp;

import uk.ac.shef.dcs.kbproxy.ProxyCore;
import uk.ac.shef.dcs.kbproxy.ProxyCoreFactory;
import uk.ac.shef.dcs.kbproxy.ProxyDefinition;

import java.io.Serializable;
import java.util.Map;

public final class PPProxyCoreFactory implements ProxyCoreFactory, Serializable {

  private static final long serialVersionUID = -2225350547844439817L;

  public ProxyCore create(final ProxyDefinition definition, final Map<String, String> prefixesToUris) throws ClassCastException {
    final PPProxyDefinition castDefinition = PPProxyDefinition.class.cast(definition);
    
    return new PPProxyCore(castDefinition, prefixesToUris);
  }
  
}
