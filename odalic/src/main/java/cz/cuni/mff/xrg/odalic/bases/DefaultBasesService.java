/**
 *
 */
package cz.cuni.mff.xrg.odalic.bases;

import java.util.Collection;
import java.util.Map;
import java.util.NavigableSet;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.executions.KnowledgeBaseProxyFactory;
import uk.ac.shef.dcs.kbproxy.KBProxy;

/**
 * Default {@link BasesService} implementation.
 *
 */
public final class DefaultBasesService implements BasesService {

  private final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory;

  @Autowired
  public DefaultBasesService(final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory) {
    Preconditions.checkNotNull(knowledgeBaseProxyFactory);

    this.knowledgeBaseProxyFactory = knowledgeBaseProxyFactory;
  }

  private NavigableSet<KnowledgeBase> build(final Stream<? extends KBProxy> stream) {
    final ImmutableSortedSet.Builder<KnowledgeBase> builder = ImmutableSortedSet.naturalOrder();
    builder.addAll(mapToDomain(stream).iterator());

    return builder.build();
  }

  @Override
  public NavigableSet<KnowledgeBase> getBases() {
    return build(getProxiesStream());
  }

  @Override
  public NavigableSet<KnowledgeBase> getInsertSupportingBases() {
    return build(getInsertSupportingStream());
  }

  private Stream<KBProxy> getInsertSupportingStream() {
    return getProxiesStream().filter(proxy -> proxy.isInsertSupported());
  }

  private Stream<KBProxy> getProxiesStream() {
    final Map<String, KBProxy> map = this.knowledgeBaseProxyFactory.getKBProxies();
    final Collection<KBProxy> proxies = map.values();

    return proxies.stream();
  }

  private Stream<KnowledgeBase> mapToDomain(final Stream<? extends KBProxy> stream) {
    return stream.map(proxy -> new KnowledgeBase(proxy.getName()));
  }
}
