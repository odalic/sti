/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.Prefix;

/**
 * Domain class {@link Prefix} adapted for REST API.
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "prefix")
public final class PrefixValue {

  private String with;
  
  private String what;
  
  public PrefixValue() {}
  
  public PrefixValue(final Prefix adaptee) {
    this.with = adaptee.getWith();
    this.what = adaptee.getWhat();
  }

  /**
   * @return the substitution
   */
  @XmlElement
  @Nullable
  public String getWith() {
    return with;
  }

  /**
   * @param with the with to set
   */
  public void setWith(String with) {
    Preconditions.checkNotNull(with);
    
    this.with = with;
  }

  /**
   * @return the substituted text
   */
  @XmlElement
  @Nullable
  public String getWhat() {
    return what;
  }

  /**
   * @param what the what to set
   */
  public void setWhat(String what) {
    Preconditions.checkNotNull(what);
    
    this.what = what;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "PrefixValue [with=" + with + ", what=" + what + "]";
  }
}
