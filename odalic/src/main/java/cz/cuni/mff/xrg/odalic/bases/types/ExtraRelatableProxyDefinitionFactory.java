package cz.cuni.mff.xrg.odalic.bases.types;

import java.util.Set;

import org.springframework.stereotype.Component;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.bases.ProxyDefinitionFactory;
import cz.cuni.mff.xrg.odalic.groups.Group;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlProxyDefinition;

@Component
public final class ExtraRelatableProxyDefinitionFactory
    implements ProxyDefinitionFactory {

  @Override
  public SparqlProxyDefinition create(final KnowledgeBase base, final Set<? extends Group> availableGroups) {
    throw new UnsupportedOperationException("This knowledge base definition does not define a proxy!");
  }
}
