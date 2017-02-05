package cz.cuni.mff.xrg.odalic.api.rdf.values;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

@RdfsClass("http://odalic.eu/internal/KnowledgeBaseColumnPositionEntry")
public class KnowledgeBaseColumnPositionEntry {

  private KnowledgeBaseValue base;
  
  private ColumnPositionValue value;
  
  public KnowledgeBaseColumnPositionEntry() {}
  
  public KnowledgeBaseColumnPositionEntry(final KnowledgeBaseValue base, final ColumnPositionValue value) {
    Preconditions.checkNotNull(base);
    Preconditions.checkNotNull(value);
    
    this.base = base;
    this.value = value;
  }

  /**
   * @return the base
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseColumnPositionEntry/Base")
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
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseColumnPositionEntry/Value")
  public ColumnPositionValue getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(ColumnPositionValue value) {
    Preconditions.checkNotNull(value);
    
    this.value = value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "KnowledgeBaseColumnPositionEntry [base=" + base + ", value=" + value + "]";
  }
}
