package cz.cuni.mff.xrg.odalic.bases;

import java.util.Set;

import uk.ac.shef.dcs.kbproxy.ProxyDefinition;
import cz.cuni.mff.xrg.odalic.groups.Group;

public interface ProxyDefinitionFactory {
  ProxyDefinition create(KnowledgeBase base, Set<? extends Group> availableGroups);
}
