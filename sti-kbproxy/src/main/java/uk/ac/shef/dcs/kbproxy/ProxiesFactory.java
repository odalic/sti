package uk.ac.shef.dcs.kbproxy;

import java.io.IOException;
import java.util.Map;

import cz.cuni.mff.xrg.odalic.util.logging.PerformanceLogger;


public interface ProxiesFactory {

  Proxy create(ProxyDefinition definition, Map<String, String> prefixesToUris);

  void dispose(final String name) throws IOException;

}
