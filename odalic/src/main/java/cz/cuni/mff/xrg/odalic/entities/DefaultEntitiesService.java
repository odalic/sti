/**
 * 
 */
package cz.cuni.mff.xrg.odalic.entities;

import com.google.common.base.Preconditions;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.executions.KnowledgeBaseProxyFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.sti.STIException;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default {@link EntitiesService} implementation.
 *
 */
public final class DefaultEntitiesService implements EntitiesService {

  private final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory;
  private final EntitiesFactory entitiesFactory;

  @Autowired
  public DefaultEntitiesService(final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory,
      final EntitiesFactory entitiesFactory) {
    Preconditions.checkNotNull(knowledgeBaseProxyFactory);
    Preconditions.checkNotNull(entitiesFactory);

    this.knowledgeBaseProxyFactory = knowledgeBaseProxyFactory;
    this.entitiesFactory = entitiesFactory;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.entities.EntitiesService#search(cz.cuni.mff.xrg.odalic.tasks.annotations
   * .KnowledgeBase, java.lang.String, int)
   */
  @Override
  public NavigableSet<Entity> searchResources(KnowledgeBase base, String query, int limit)
      throws IllegalArgumentException, KBProxyException, STIException, IOException {
    KBProxy kbProxy = getKBProxy(base);

    List<uk.ac.shef.dcs.kbproxy.model.Entity> searchResult =
        kbProxy.findResourceByFulltext(query, limit);

    return searchResult.stream().map(entity -> entitiesFactory.create(entity.getId(), entity.getLabel()))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * cz.cuni.mff.xrg.odalic.entities.EntitiesService#search(cz.cuni.mff.xrg.odalic.tasks.annotations
   * .KnowledgeBase, java.lang.String, int)
   */
  @Override
  public NavigableSet<Entity> searchClasses(KnowledgeBase base, String query, int limit)
      throws IllegalArgumentException, KBProxyException, STIException, IOException {
    final KBProxy kbProxy = getKBProxy(base);

    List<uk.ac.shef.dcs.kbproxy.model.Entity> searchResult =
        kbProxy.findClassByFulltext(query, limit);

    return searchResult.stream().map(entity -> entitiesFactory.create(entity.getId(), entity.getLabel()))
        .collect(Collectors.toCollection(TreeSet::new));
  }


  @Override
  public NavigableSet<Entity> searchProperties(KnowledgeBase base, String query, int limit,
      URI domain, URI range)
      throws IllegalArgumentException, KBProxyException, STIException, IOException {
    final KBProxy kbProxy = getKBProxy(base);

    // TODO: Find only properties, restricted by the domain (the domains of found properties must be
    // sub-type of the provided domain, the same for ranges). Null means no restriction.
    // Ignoring the domain and range for now.
    List<uk.ac.shef.dcs.kbproxy.model.Entity> searchResult =
        kbProxy.findPredicateByFulltext(query, limit, domain, range);

    return searchResult.stream().map(entity -> entitiesFactory.create(entity.getId(), entity.getLabel()))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.entities.EntitiesService#propose(cz.cuni.mff.xrg.odalic.entities.
   * ClassProposal)
   */
  @Override
  public Entity propose(KnowledgeBase base, ClassProposal proposal)
      throws KBProxyException, STIException, IOException {
    KBProxy kbProxy = getKBProxy(base);

    String superClassUri = getEntityValue(proposal.getSuperClass());

    uk.ac.shef.dcs.kbproxy.model.Entity entity = kbProxy.insertClass(proposal.getSuffix(),
        proposal.getLabel(), proposal.getAlternativeLabels(), superClassUri);

    return entitiesFactory.create(entity.getId(), entity.getLabel());
  }

  @Override
  public Entity propose(KnowledgeBase base, PropertyProposal proposal)
      throws KBProxyException, STIException, IOException {
    KBProxy kbProxy = getKBProxy(base);

    String superPropertyUri = getEntityValue(proposal.getSuperProperty());
    String rangeUri = getEntityValue(proposal.getRange());
    String domainUri = getEntityValue(proposal.getDomain());

    uk.ac.shef.dcs.kbproxy.model.Entity entity =
        kbProxy.insertProperty(proposal.getSuffix(), proposal.getLabel(),
            proposal.getAlternativeLabels(), superPropertyUri, rangeUri, domainUri);

    return entitiesFactory.create(entity.getId(), entity.getLabel());
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.entities.EntitiesService#propose(cz.cuni.mff.xrg.odalic.entities.
   * ResourceProposal)
   */
  @Override
  public Entity propose(KnowledgeBase base, ResourceProposal proposal)
      throws KBProxyException, STIException, IOException {
    KBProxy kbProxy = getKBProxy(base);

    Collection<String> classes = null;
    if (proposal.getClasses() != null) {
      classes =
          proposal.getClasses().stream().map(Entity::getResource).collect(Collectors.toList());
    }
    uk.ac.shef.dcs.kbproxy.model.Entity entity = kbProxy.insertConcept(proposal.getSuffix(),
        proposal.getLabel(), proposal.getAlternativeLabels(), classes);

    return entitiesFactory.create(entity.getId(), entity.getLabel());
  }

  private KBProxy getKBProxy(KnowledgeBase base)
      throws KBProxyException, STIException, IOException {
    KBProxy kbProxy = knowledgeBaseProxyFactory.getKBProxies().get(base.getName());

    if (kbProxy == null) {
      throw new IllegalArgumentException(
          "Knowledge base named \"" + base.getName() + "\" was not found.");
    }

    return kbProxy;
  }

  private String getEntityValue(Entity property) {
    if (property != null) {
      return property.getResource();
    } else {
      return null;
    }
  }
}
