/**
 *
 */
package cz.cuni.mff.xrg.odalic.util;

/**
 * Reference wrapper. It may find use in cases when pass by reference semantics are needed.
 *
 * @author Václav Brodec
 */
public final class ReferenceWrapper<T> {

  public static <U> ReferenceWrapper<U> empty() {
    return new ReferenceWrapper<U>();
  }

  public static <U> ReferenceWrapper<U> wrap(final U reference) {
    return new ReferenceWrapper<U>(reference);
  }

  private T reference;

  /**
   * Creates an empty wrapper.
   */
  public ReferenceWrapper() {}

  /**
   * Wraps the reference.
   *
   * @param reference wrapped reference
   */
  public ReferenceWrapper(final T reference) {
    this.reference = reference;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    final ReferenceWrapper other = (ReferenceWrapper) obj;
    return this.reference == other.reference;
  }

  /**
   * @return the reference
   */
  public T getReference() {
    return this.reference;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + System.identityHashCode(this.reference);
    return result;
  }

  /**
   * Indicates non-{@code null} reference presence.
   *
   * @return true when the contained reference is {@code null}, false otherwise
   */
  public boolean isEmpty() {
    return this.reference == null;
  }

  /**
   * @param reference the reference to set
   */
  public void setReference(final T reference) {
    this.reference = reference;
  }
}
