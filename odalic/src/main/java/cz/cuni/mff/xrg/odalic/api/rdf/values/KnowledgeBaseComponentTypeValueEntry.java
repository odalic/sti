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

  public KnowledgeBaseComponentTypeValueEntry(final KnowledgeBaseValue base,
      final ComponentTypeValue value) {
    Preconditions.checkNotNull(base);
    Preconditions.checkNotNull(value);

    this.base = base;
    this.value = value;
  }

  /**
   * @return the base
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseConponentTypeValueEntry/base")
  public KnowledgeBaseValue getBase() {
    return this.base;
  }

  /**
   * @return the value
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseConponentTypeValueEntry/value")
  public ComponentTypeValue getValue() {
    return this.value;
  }

  /**
   * @param base the base to set
   */
  public void setBase(final KnowledgeBaseValue base) {
    Preconditions.checkNotNull(base);

    this.base = base;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final ComponentTypeValue value) {
    Preconditions.checkNotNull(value);

    this.value = value;
  }

  @Override
  public String toString() {
    return "KnowledgeBaseConponentTypeValueEntry [base=" + this.base + ", value=" + this.value
        + "]";
  }
}
