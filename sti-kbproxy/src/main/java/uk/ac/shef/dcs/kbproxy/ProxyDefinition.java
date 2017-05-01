package uk.ac.shef.dcs.kbproxy;

import java.net.URI;
import java.util.Set;

public interface ProxyDefinition {

  String getStructureDomain();

  String getStructureRange();

  Set<String> getStoppedClasses();

  Set<String> getStoppedAttributes();

  URI getInsertPrefixSchema();

  URI getInsertPrefixData();
}
