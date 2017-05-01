package cz.cuni.mff.xrg.odalic.bases;

import uk.ac.shef.dcs.kbproxy.ProxyDefinition;

public interface ProxyDefinitionFactory {
  ProxyDefinition create(KnowledgeBase base);
}
