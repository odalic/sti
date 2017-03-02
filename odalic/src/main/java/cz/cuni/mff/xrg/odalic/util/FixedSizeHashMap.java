package cz.cuni.mff.xrg.odalic.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

public final class FixedSizeHashMap<K, V> extends LinkedHashMap<K, V> {

  private static final long serialVersionUID = -1652004527283525789L;
  private int maximumSize;


  public FixedSizeHashMap(final int maximumSize) {
    super();

    initialize(maximumSize);
  }

  public FixedSizeHashMap(final int maximumSize, final int initialCapacity) {
    super(initialCapacity);

    initialize(maximumSize);
  }

  public FixedSizeHashMap(final int maximumSize, final int initialCapacity,
      final float loadFactor) {
    super(initialCapacity, loadFactor);

    initialize(maximumSize);
  }

  public FixedSizeHashMap(final int maximumSize, final int initialCapacity, final float loadFactor,
      final boolean accessOrder) {
    super(initialCapacity, loadFactor, accessOrder);

    initialize(maximumSize);
  }

  public FixedSizeHashMap(final int maximumSize, final Map<? extends K, ? extends V> m) {
    super(m);

    initialize(maximumSize);
  }

  private void initialize(final int maximumSize) {
    Preconditions.checkArgument(maximumSize >= 1, "The maximum size must be at least one!");

    this.maximumSize = maximumSize;
  }

  @Override
  protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
    return size() > this.maximumSize;
  }
}
