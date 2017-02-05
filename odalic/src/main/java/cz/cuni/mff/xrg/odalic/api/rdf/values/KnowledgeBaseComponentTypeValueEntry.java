package cz.cuni.mff.xrg.odalic.api.rdf.values;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;

@RdfsClass("http://odalic.eu/internal/KnowledgeBaseConponentTypeValueEntry")
public class KnowledgeBaseComponentTypeValueEntry {

  private KnowledgeBaseValue base;
  
  private ComponentTypeValue value;
  
  public KnowledgeBaseComponentTypeValueEntry() {}
  
  public KnowledgeBaseComponentTypeValueEntry(final KnowledgeBaseValue base, final ComponentTypeValue value) {
    Preconditions.checkNotNull(base);
    Preconditions.checkNotNull(value);
    
    this.base = base;
    this.value = value;
  }

  /**
   * @return the base
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseConponentTypeValueEntry/Base")
  public KnowledgeBaseValue getBase() {
    return base;
  }

  /**
   * @param base the base to set
   */
  public void setBase(KnowledgeBaseValue base) {
    Preconditions.checkNotNull(base);
    
    this.base = base;
  }

  /**
   * @return the value
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseConponentTypeValueEntry/Value")
  public ComponentTypeValue getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(ComponentTypeValue value) {
    Preconditions.checkNotNull(value);
    
    this.value = value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "KnowledgeBaseConponentTypeValueEntry [base=" + base + ", value=" + value + "]";
  }
}
