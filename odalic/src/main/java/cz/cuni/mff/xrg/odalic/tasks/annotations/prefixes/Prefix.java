/**
 * 
 */
package cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * Resource ID prefix.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
public final class Prefix {

  private final String with;
  
  private final String what;
  
  /**
   * Creates an empty prefix. Both the {@link Prefix#getWith()} and {@link #getWhat()} are empty strings.
   */
  public static Prefix empty() {
    return new Prefix("", "");
  }
  
  /**
   * Creates a prefix.
   * 
   * @param with text to substitute with
   * @param what text to be substituted
   */
  public static Prefix create(final String with, final String what) {
    return new Prefix(with, what);
  }
  
  private Prefix(final String with, final String what) {
    Preconditions.checkNotNull(with, "The with cannot be null!");
    Preconditions.checkNotNull(what, "The what cannot be null!");
    
    this.with = with;
    this.what = what;
  }
  
  /**
   * Empty prefix substituting empty string.
   */
  public Prefix() {
    this("", "");
  }

  /**
   * @return the substitution
   */
  public String getWith() {
    return with;
  }

  /**
   * @return the substituted text
   */
  public String getWhat() {
    return what;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + what.hashCode();
    result = prime * result + with.hashCode();
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (getClass() != object.getClass()) {
      return false;
    }
    final Prefix other = (Prefix) object;
    if (!what.equals(other.what)) {
      return false;
    }
    if (!with.equals(other.with)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Prefix [with=" + with + ", what=" + what + "]";
  }
}
