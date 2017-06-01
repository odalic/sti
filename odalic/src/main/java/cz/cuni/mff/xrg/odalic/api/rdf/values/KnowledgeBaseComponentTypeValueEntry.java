package cz.cuni.mff.xrg.odalic.api.rdf.values;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.values.ComponentTypeValue;

@RdfsClass("http://odalic.eu/internal/KnowledgeBaseConponentTypeValueEntry")
public class KnowledgeBaseComponentTypeValueEntry {

  private String base;

  private ComponentTypeValue value;

  public KnowledgeBaseComponentTypeValueEntry() {}

  public KnowledgeBaseComponentTypeValueEntry(final String base,
      final ComponentTypeValue value) {
    Preconditions.checkNotNull(base, "The base cannot be null!");
    Preconditions.checkNotNull(value, "The value cannot be null!");

    this.base = base;
    this.value = value;
  }

  /**
   * @return the base
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseConponentTypeValueEntry/base")
  public String getBase() {
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
  public void setBase(final String base) {
    Preconditions.checkNotNull(base, "The base cannot be null!");

    this.base = base;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final ComponentTypeValue value) {
    Preconditions.checkNotNull(value, "The value cannot be null!");

    this.value = value;
  }

  @Override
  public String toString() {
    return "KnowledgeBaseConponentTypeValueEntry [base=" + this.base + ", value=" + this.value
        + "]";
  }
}
