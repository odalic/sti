package uk.ac.shef.dcs.kbproxy;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.kbproxy.model.PropertyType;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import cz.cuni.mff.xrg.odalic.util.logging.PerformanceLogger;

class PerformanceLoggingProxyCore implements ProxyCore {
  private final ProxyCore core;
  private final PerformanceLogger logger;

  public PerformanceLoggingProxyCore(final ProxyCore core, final PerformanceLogger logger) {
    this.core = core;
    this.logger = logger;
  }

  @Override
  public void closeConnection() throws ProxyException {
    logger.doThrowableMethod("ProxyCore - closeConnection", core::closeConnection);
  }

  @Override
  public void commitChanges() throws ProxyException {
    logger.doThrowableMethod("ProxyCore - commitChanges", core::commitChanges);
  }

  @Override
  public List<Attribute> findAttributesOfClazz(String clazzId) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findAttributesOfClazz", () -> core.findAttributesOfClazz(clazzId));
  }

  @Override
  public List<Attribute> findAttributesOfEntities(Entity ec) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findAttributesOfEntities", () -> core.findAttributesOfEntities(ec));
  }

  @Override
  public List<Attribute> findAttributesOfProperty(String propertyId) throws ProxyException {
    return logger.doThrowableFunction("findAttributesOfProperty", () -> core.findAttributesOfProperty(propertyId));
  }

  @Override
  public List<Entity> findClassByFulltext(String pattern, int limit) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findClassByFulltext", () -> core.findClassByFulltext(pattern, limit));
  }

  @Override
  public List<Entity> findEntityCandidates(String content) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findEntityCandidates", () -> core.findEntityCandidates(content));
  }

  @Override
  public List<Entity> findEntityCandidates(String content, ProxyCore dependenciesProxy) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findEntityCandidates", () -> core.findEntityCandidates(content, dependenciesProxy));
  }

  @Override
  public List<Entity> findEntityCandidatesOfTypes(String content, String... types) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findEntityCandidatesOfTypes", () -> core.findEntityCandidatesOfTypes(content, types));
  }

  @Override
  public List<Entity> findEntityCandidatesOfTypes(String content, ProxyCore dependenciesProxy, String... types) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findEntityCandidatesOfTypes", () -> core.findEntityCandidatesOfTypes(content, dependenciesProxy, types));
  }

  @Override
  public Double findEntityClazzSimilarity(String entity_id, String clazz_url) {
    return logger.doFunction("ProxyCore - findEntityClazzSimilarity", () -> core.findEntityClazzSimilarity(entity_id, clazz_url));
  }

  @Override
  public Double findGranularityOfClazz(String clazz) {
    return logger.doFunction("ProxyCore - findGranularityOfClazz", () -> core.findGranularityOfClazz(clazz));
  }

  @Override
  public List<Entity> findPredicateByFulltext(String pattern, int limit, URI domain, URI range) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findPredicateByFulltext", () -> core.findPredicateByFulltext(pattern, limit, domain, range));
  }

  @Override
  public List<Entity> findResourceByFulltext(String pattern, int limit) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findResourceByFulltext", () -> core.findResourceByFulltext(pattern, limit));
  }

  @Override
  public String getName() {
    return logger.doFunction("ProxyCore - getName", core::getName);
  }

  @Override
  public List<String> getPropertyDomains(String uri) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - getPropertyDomains", () -> core.getPropertyDomains(uri));
  }

  @Override
  public List<String> getPropertyRanges(String uri) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - getPropertyRanges", () -> core.getPropertyRanges(uri));
  }

  @Override
  public Entity insertClass(URI uri, String label, Collection<String> alternativeLabels, String superClass) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - insertClass", () -> core.insertClass(uri, label, alternativeLabels, superClass));
  }

  @Override
  public Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels, Collection<String> classes) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - insertConcept", () -> core.insertConcept(uri, label, alternativeLabels, classes));
  }

  @Override
  public Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels, String superProperty, String domain, String range, PropertyType type) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - insertProperty", () -> core.insertProperty(uri, label, alternativeLabels, superProperty, domain, range, type));
  }

  @Override
  public boolean isInsertSupported() {
    return logger.doFunction("ProxyCore - isInsertSupported", core::isInsertSupported);
  }

  @Override
  public Entity loadEntity(String uri) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - loadEntity", () -> core.loadEntity(uri));
  }

  @Override
  public Entity loadEntity(String uri, ProxyCore dependenciesProxy) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - loadEntity", () -> core.loadEntity(uri, dependenciesProxy));
  }

  @Override
  public String getResourceLabel(String uri) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - getResourceLabel", () -> core.getResourceLabel(uri));
  }

  @Override
  public List<Attribute> findAttributes(String resourceId) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findAttributes", () -> core.findAttributes(resourceId));
  }

  @Override
  public List<Attribute> findAttributesOfClazz(String clazzId, ProxyCore dependenciesProxy) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findAttributesOfClazz", () -> core.findAttributesOfClazz(clazzId, dependenciesProxy));
  }

  @Override
  public List<Attribute> findAttributesOfEntities(Entity ec, ProxyCore dependenciesProxy) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findAttributesOfEntities", () -> core.findAttributesOfEntities(ec, dependenciesProxy));
  }

  @Override
  public List<Attribute> findAttributesOfProperty(String propertyId, ProxyCore dependenciesProxy) throws ProxyException {
    return logger.doThrowableFunction("ProxyCore - findAttributesOfProperty", () -> core.findAttributesOfProperty(propertyId, dependenciesProxy));
  }

  @Override
  public ProxyDefinition getDefinition() {
    return logger.doFunction("ProxyCore - getDefinition", core::getDefinition);
  }
}
