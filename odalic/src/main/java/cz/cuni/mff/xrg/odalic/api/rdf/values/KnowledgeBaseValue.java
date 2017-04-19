package cz.cuni.mff.xrg.odalic.api.rdf.values;

import javax.annotation.Nullable;

import com.complexible.pinto.annotations.RdfId;
import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;

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

  public KnowledgeBaseValue(final KnowledgeBase adaptee) {
    this.name = adaptee.getName();
  }

  /**
   * @return the name
   */
  @RdfProperty(value = "http://odalic.eu/internal/KnowledgeBase/name",
      datatype = "http://www.w3.org/2001/XMLSchema#string")
  @RdfId
  @Nullable
  public String getName() {
    return this.name;
  }

  /**
   * @param name the name to set
   */
  public void setName(final String name) {
    Preconditions.checkNotNull(name);

    this.name = name;
  }

  @Override
  public String toString() {
    return "KnowledgeBaseValue [name=" + this.name + "]";
  }
}
