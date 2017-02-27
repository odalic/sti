package uk.ac.shef.dcs.util;

import java.io.Serializable;

/**
 * <p>
 * Basic extendible pair class. Serves as replacement for the previously used class that was not a
 * part of public API.
 * </p>
 *
 * <p>
 * Although pair classes are generally frowned upon, this is necessary concession to re-factor the
 * original STI code.
 * </p>
 *
 * @author Václav Brodec
 *
 * @param <K> key
 * @param <V> value
 */
public class Pair<K, V> implements Serializable {

  private static final long serialVersionUID = 6136777445824073673L;

  private final K key;
  private final V value;

  public Pair(final K key, final V value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Pair)) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    final Pair other = (Pair) obj;
    if (this.key == null) {
      if (other.key != null) {
        return false;
      }
    } else if (!this.key.equals(other.key)) {
      return false;
    }
    if (this.value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!this.value.equals(other.value)) {
      return false;
    }
    return true;
  }

  public K getKey() {
    return this.key;
  }

  public V getValue() {
    return this.value;
  }

  @Override
  public int hashCode() {
    // The name's hash code is multiplied by a prime number to make sure there is difference in the
    // hash code for two pairs where the key and values is only switched.
    return (this.key.hashCode() * 13) + (this.value == null ? 0 : this.value.hashCode());
  }

  @Override
  public String toString() {
    return this.key + "=" + this.value;
  }
}

