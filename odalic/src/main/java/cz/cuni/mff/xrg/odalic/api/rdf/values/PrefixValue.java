/**
 * 
 */
package cz.cuni.mff.xrg.odalic.api.rdf.values;

import javax.annotation.Nullable;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.Prefix;

/**
 * Domain class {@link Prefix} adapted for RDF serialization.
 * 
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/Prefix")
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
  @RdfProperty(value = "http://odalic.eu/internal/Prefix/with", datatype = "http://www.w3.org/2001/XMLSchema#string")
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
  @RdfProperty(value = "http://odalic.eu/internal/Prefix/what", datatype = "http://www.w3.org/2001/XMLSchema#string")
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

  public Prefix toPrefix() {
    return Prefix.create(with, what);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "PrefixValue [with=" + with + ", what=" + what + "]";
  }
}
