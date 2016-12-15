/**
 * 
 */
package cz.cuni.mff.xrg.odalic.bases;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.executions.KnowledgeBaseProxyFactory;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.sti.STIException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableSet;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default {@link BasesService} implementation.
 *
 */
public final class DefaultBasesService implements BasesService {

  private final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory;

  @Autowired
  public DefaultBasesService(KnowledgeBaseProxyFactory knowledgeBaseProxyFactory) {
    Preconditions.checkNotNull(knowledgeBaseProxyFactory);

    this.knowledgeBaseProxyFactory = knowledgeBaseProxyFactory;
  }

  @Override
  public NavigableSet<KnowledgeBase> getBases() throws STIException, IOException {
    return build(getProxiesStream());
  }

  private NavigableSet<KnowledgeBase> build(final Stream<? extends KBProxy> stream) {
    final ImmutableSortedSet.Builder<KnowledgeBase> builder = ImmutableSortedSet.naturalOrder();
    builder.addAll(mapToDomain(stream).iterator());
    
    return builder.build();
  }

  private Stream<KnowledgeBase> mapToDomain(final Stream<? extends KBProxy> stream) {
    return stream.map(proxy -> new KnowledgeBase(proxy.getName()));
  }

  private Stream<KBProxy> getProxiesStream() throws STIException, IOException {
    final Map<String, KBProxy> map = this.knowledgeBaseProxyFactory.getKBProxies();
    final Collection<KBProxy> proxies = map.values();

    return proxies.stream();
  }

  @Override
  public NavigableSet<KnowledgeBase> getInsertSupportingBases() throws STIException, IOException {
    return build(getInsertSupportingStream());
  }

  private Stream<KBProxy> getInsertSupportingStream() throws STIException, IOException {
    return getProxiesStream().filter(proxy -> proxy.isInsertSupported());
  }
}
