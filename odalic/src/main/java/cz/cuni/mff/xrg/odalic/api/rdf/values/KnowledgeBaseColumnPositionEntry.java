package cz.cuni.mff.xrg.odalic.api.rdf.values;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

@RdfsClass("http://odalic.eu/internal/KnowledgeBaseColumnPositionEntry")
public class KnowledgeBaseColumnPositionEntry {

  private String base;

  private ColumnPositionValue value;

  public KnowledgeBaseColumnPositionEntry() {}

  public KnowledgeBaseColumnPositionEntry(final String base,
      final ColumnPositionValue value) {
    Preconditions.checkNotNull(base);
    Preconditions.checkNotNull(value);

    this.base = base;
    this.value = value;
  }

  /**
   * @return the base
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseColumnPositionEntry/base")
  public String getBase() {
    return this.base;
  }

  /**
   * @return the value
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseColumnPositionEntry/value")
  public ColumnPositionValue getValue() {
    return this.value;
  }

  /**
   * @param base the base to set
   */
  public void setBase(final String base) {
    Preconditions.checkNotNull(base);

    this.base = base;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final ColumnPositionValue value) {
    Preconditions.checkNotNull(value);

    this.value = value;
  }

  @Override
  public String toString() {
    return "KnowledgeBaseColumnPositionEntry [base=" + this.base + ", value=" + this.value + "]";
  }
}
