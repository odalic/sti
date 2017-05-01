package uk.ac.shef.dcs.kbproxy;

import java.util.Map;


public interface ProxiesFactory {

  Proxy createInstance(String id, ProxyDefinition definition, Map<String, String> prefixesToUris);

}
