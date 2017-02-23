package cz.cuni.mff.xrg.odalic.api.rdf.values;

import javax.annotation.Nullable;

import com.complexible.pinto.annotations.RdfId;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;

/**
 * Domain class {@link KnowledgeBase} adapted for RDF serialization.
 * 
 * @author Václav Brodec
 *
 */
@RdfsClass("http://odalic.eu/internal/KnowledgeBase")
public final class KnowledgeBaseValue {

  private String name;
  
  public KnowledgeBaseValue() {}
  
  public KnowledgeBaseValue(KnowledgeBase adaptee) {
    name = adaptee.getName();
  }
  
  /**
   * @return the name
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/name", datatype = "http://www.w3.org/2001/XMLSchema#string")
  @RdfId
  @Nullable
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    Preconditions.checkNotNull(name);
    
    this.name = name;
  }

  public KnowledgeBase toKnowledgeBase() {
    return new KnowledgeBase(name);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "KnowledgeBaseValue [name=" + name + "]";
  }
}
