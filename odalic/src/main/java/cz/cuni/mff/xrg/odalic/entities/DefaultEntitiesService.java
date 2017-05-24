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
import com.google.common.collect.ImmutableSet;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.bases.proxies.KnowledgeBaseProxiesService;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import uk.ac.shef.dcs.kbproxy.ProxyException;
import uk.ac.shef.dcs.kbproxy.Proxy;

/**
 * Default {@link EntitiesService} implementation.
 *
 */
public final class DefaultEntitiesService implements EntitiesService {

  private final KnowledgeBaseProxiesService knowledgeBaseProxyFactory;
  private final EntitiesFactory entitiesFactory;

  @Autowired
  public DefaultEntitiesService(final KnowledgeBaseProxiesService knowledgeBaseProxyFactory,
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

  private Proxy getKBProxy(final KnowledgeBase base) {
    final Proxy kbProxy = this.knowledgeBaseProxyFactory.toProxies(ImmutableSet.of(base)).values().iterator().next();

    if (kbProxy == null) {
      throw new IllegalArgumentException(
          "Knowledge base named \"" + base.getName() + "\" was not found.");
    }

    return kbProxy;
  }

  @Override
  public Entity propose(final KnowledgeBase base, final ClassProposal proposal)
      throws ProxyException {
    final Proxy kbProxy = getKBProxy(base);

    final String superClassUri = getEntityValue(proposal.getSuperClass());

    final uk.ac.shef.dcs.kbproxy.model.Entity entity = kbProxy.insertClass(proposal.getSuffix(),
        proposal.getLabel(), proposal.getAlternativeLabels(), superClassUri);

    return this.entitiesFactory.create(entity.getId(), entity.getLabel());
  }

  @Override
  public Entity propose(final KnowledgeBase base, final PropertyProposal proposal)
      throws ProxyException {
    final Proxy kbProxy = getKBProxy(base);

    final String superPropertyUri = getEntityValue(proposal.getSuperProperty());

    final uk.ac.shef.dcs.kbproxy.model.Entity entity = kbProxy.insertProperty(proposal.getSuffix(),
        proposal.getLabel(), proposal.getAlternativeLabels(), superPropertyUri,
        proposal.getDomain(), proposal.getRange(), convertPropertyType(proposal.getType()));

    return this.entitiesFactory.create(entity.getId(), entity.getLabel());
  }

  private uk.ac.shef.dcs.kbproxy.model.PropertyType convertPropertyType(PropertyType type) {
    switch (type) {
      case DATA:
        return uk.ac.shef.dcs.kbproxy.model.PropertyType.Data;
      case OBJECT:
      default:
        return uk.ac.shef.dcs.kbproxy.model.PropertyType.Object;
    }
  }

  @Override
  public Entity propose(final KnowledgeBase base, final ResourceProposal proposal)
      throws ProxyException {
    final Proxy kbProxy = getKBProxy(base);

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
      final int limit) throws ProxyException {
    final Proxy kbProxy = getKBProxy(base);

    final List<uk.ac.shef.dcs.kbproxy.model.Entity> searchResult =
        kbProxy.findClassByFulltext(query, limit);

    return searchResult.stream()
        .map(entity -> this.entitiesFactory.create(entity.getId(), entity.getLabel()))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  @Override
  public NavigableSet<Entity> searchProperties(final KnowledgeBase base, final String query,
      final int limit, final URI domain, final URI range) throws ProxyException {
    final Proxy kbProxy = getKBProxy(base);

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
      final int limit) throws ProxyException {
    final Proxy kbProxy = getKBProxy(base);

    final List<uk.ac.shef.dcs.kbproxy.model.Entity> searchResult =
        kbProxy.findResourceByFulltext(query, limit);

    return searchResult.stream()
        .map(entity -> this.entitiesFactory.create(entity.getId(), entity.getLabel()))
        .collect(Collectors.toCollection(TreeSet::new));
  }
}
