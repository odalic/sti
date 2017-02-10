/**
 * 
 */
package cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.PrefixAdapter;

/**
 * Resource ID prefix.
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(PrefixAdapter.class)
public final class Prefix implements Serializable {

  private static final long serialVersionUID = 1038022885775345729L;

  private final String with;
  
  private final String what;
  
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
    
    Preconditions.checkArgument(!with.isEmpty(), "The with cannot be an empty string!");
    Preconditions.checkArgument(!what.isEmpty(), "The what cannot be an empty string!");
    
    this.with = with;
    this.what = what;
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
