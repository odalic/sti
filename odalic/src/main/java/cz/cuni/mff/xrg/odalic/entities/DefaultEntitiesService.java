/**
 *
 */
package cz.cuni.mff.xrg.odalic.entities;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.executions.KnowledgeBaseProxyFactory;
import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;

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

  private String getEntityValue(final Entity property) {
    if (property != null) {
      return property.getResource();
    } else {
      return null;
    }
  }

  private KBProxy getKBProxy(final KnowledgeBase base) {
    final KBProxy kbProxy = this.knowledgeBaseProxyFactory.getKBProxies().get(base.getName());

    if (kbProxy == null) {
      throw new IllegalArgumentException(
          "Knowledge base named \"" + base.getName() + "\" was not found.");
    }

    return kbProxy;
  }

  @Override
  public Entity propose(final KnowledgeBase base, final ClassProposal proposal)
      throws KBProxyException {
    final KBProxy kbProxy = getKBProxy(base);

    final String superClassUri = getEntityValue(proposal.getSuperClass());

    final uk.ac.shef.dcs.kbproxy.model.Entity entity = kbProxy.insertClass(proposal.getSuffix(),
        proposal.getLabel(), proposal.getAlternativeLabels(), superClassUri);

    return this.entitiesFactory.create(entity.getId(), entity.getLabel());
  }

  @Override
  public Entity propose(final KnowledgeBase base, final PropertyProposal proposal)
      throws KBProxyException {
    final KBProxy kbProxy = getKBProxy(base);

    final String superPropertyUri = getEntityValue(proposal.getSuperProperty());

    final uk.ac.shef.dcs.kbproxy.model.Entity entity = kbProxy.insertProperty(proposal.getSuffix(),
        proposal.getLabel(), proposal.getAlternativeLabels(), superPropertyUri,
        proposal.getDomain(), proposal.getRange());

    return this.entitiesFactory.create(entity.getId(), entity.getLabel());
  }

  @Override
  public Entity propose(final KnowledgeBase base, final ResourceProposal proposal)
      throws KBProxyException {
    final KBProxy kbProxy = getKBProxy(base);

    Collection<String> classes = null;
    if (proposal.getClasses() != null) {
      classes =
          proposal.getClasses().stream().map(Entity::getResource).collect(Collectors.toList());
    }
    final uk.ac.shef.dcs.kbproxy.model.Entity entity = kbProxy.insertConcept(proposal.getSuffix(),
        proposal.getLabel(), proposal.getAlternativeLabels(), classes);

    return this.entitiesFactory.create(entity.getId(), entity.getLabel());
  }

  @Override
  public NavigableSet<Entity> searchClasses(final KnowledgeBase base, final String query,
      final int limit) throws KBProxyException {
    final KBProxy kbProxy = getKBProxy(base);

    final List<uk.ac.shef.dcs.kbproxy.model.Entity> searchResult =
        kbProxy.findClassByFulltext(query, limit);

    return searchResult.stream()
        .map(entity -> this.entitiesFactory.create(entity.getId(), entity.getLabel()))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  @Override
  public NavigableSet<Entity> searchProperties(final KnowledgeBase base, final String query,
      final int limit, final URI domain, final URI range) throws KBProxyException {
    final KBProxy kbProxy = getKBProxy(base);

    // TODO: Find only properties, restricted by the domain (the domains of found properties must be
    // sub-type of the provided domain, the same for ranges). Null means no restriction.
    // Ignoring the domain and range for now.
    final List<uk.ac.shef.dcs.kbproxy.model.Entity> searchResult =
        kbProxy.findPredicateByFulltext(query, limit, domain, range);

    return searchResult.stream()
        .map(entity -> this.entitiesFactory.create(entity.getId(), entity.getLabel()))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  @Override
  public NavigableSet<Entity> searchResources(final KnowledgeBase base, final String query,
      final int limit) throws KBProxyException {
    final KBProxy kbProxy = getKBProxy(base);

    final List<uk.ac.shef.dcs.kbproxy.model.Entity> searchResult =
        kbProxy.findResourceByFulltext(query, limit);

    return searchResult.stream()
        .map(entity -> this.entitiesFactory.create(entity.getId(), entity.getLabel()))
        .collect(Collectors.toCollection(TreeSet::new));
  }
}
