package cz.cuni.mff.xrg.odalic.entities;

import java.net.URI;
import java.util.NavigableSet;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import uk.ac.shef.dcs.kbproxy.KBProxyException;

/**
 * Provides basic capabilities of entities management.
 *
 * @author Václav Brodec
 *
 */
public interface EntitiesService {
  /**
   * Propose a new class to the primary base.
   *
   * @param base used knowledge base
   * @param proposal class proposal
   * @return created class
   * @throws IllegalArgumentException when the class is already defined or some part of the proposal
   *         is invalid
   * @throws KBProxyException when an exception happens within the base proxy
   */
  Entity propose(KnowledgeBase base, ClassProposal proposal) throws KBProxyException;

  /**
   * Propose a new property to the primary base.
   *
   * @param base used knowledge base
   * @param proposal property proposal
   * @return created entity
   * @throws IllegalArgumentException when the entity is already defined or some part of the
   *         proposal is invalid
   * @throws KBProxyException when an exception happens within the base proxy
   */
  Entity propose(KnowledgeBase base, PropertyProposal proposal) throws KBProxyException;


  /**
   * Propose a new entity to the primary base.
   *
   * @param base used knowledge base
   * @param proposal resource proposal
   * @return created entity
   * @throws IllegalArgumentException when the entity is already defined or some part of the
   *         proposal is invalid
   * @throws KBProxyException when an exception happens within the base proxy
   */
  Entity propose(KnowledgeBase base, ResourceProposal proposal) throws KBProxyException;

  /**
   * Searches for classes conforming to the query.
   *
   * @param base used knowledge base
   * @param query search query
   * @param limit maximum results count
   * @return found classes
   * @throws KBProxyException when an exception happens within the base proxy
   */
  NavigableSet<Entity> searchClasses(KnowledgeBase base, String query, int limit)
      throws KBProxyException;

  /**
   * Searches for properties conforming to the query.
   *
   * @param base used knowledge base
   * @param query search query
   * @param limit maximum results count
   * @param domain domain restriction of the properties
   * @param range range restriction of the properties
   * @return found properties
   * @throws KBProxyException when an exception happens within the base proxy
   */
  NavigableSet<Entity> searchProperties(KnowledgeBase base, String query, int limit, URI domain,
      URI range) throws KBProxyException;

  /**
   * Searches for resources conforming to the query.
   *
   * @param base used knowledge base
   * @param query search query
   * @param limit maximum results count
   * @return found resources
   * @throws KBProxyException when an exception happens within the base proxy
   */
  NavigableSet<Entity> searchResources(KnowledgeBase base, String query, int limit)
      throws KBProxyException;
}
