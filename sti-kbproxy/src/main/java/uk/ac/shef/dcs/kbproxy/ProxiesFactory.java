package uk.ac.shef.dcs.kbproxy;

import java.io.IOException;
import java.util.Map;


public interface ProxiesFactory {

  Proxy create(ProxyDefinition definition, Map<String, String> prefixesToUris);

  void dispose(final String name) throws IOException;

}
