package cz.cuni.mff.xrg.odalic.api.rdf.values;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

@RdfsClass("http://odalic.eu/internal/KnowledgeBaseColumnPositionSetEntry")
public class KnowledgeBaseColumnPositionSetEntry {

  private String base;

  private ColumnPositionSetWrapper set;

  public KnowledgeBaseColumnPositionSetEntry() {}

  public KnowledgeBaseColumnPositionSetEntry(final String base,
      final ColumnPositionSetWrapper set) {
    Preconditions.checkNotNull(base, "The base cannot be null!");
    Preconditions.checkNotNull(set, "The set cannot be null!");

    this.base = base;
    this.set = set;
  }

  /**
   * @return the base
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseColumnPositionSetEntry/base")
  public String getBase() {
    return this.base;
  }

  /**
   * @return the set
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseColumnPositionSetEntry/set")
  public ColumnPositionSetWrapper getSet() {
    return this.set;
  }

  /**
   * @param base the base to set
   */
  public void setBase(final String base) {
    Preconditions.checkNotNull(base, "The base cannot be null!");

    this.base = base;
  }

  /**
   * @param set the set to set
   */
  public void setSet(final ColumnPositionSetWrapper set) {
    Preconditions.checkNotNull(set, "The set cannot be null!");

    this.set = set;
  }

  @Override
  public String toString() {
    return "KnowledgeBaseColumnPositionSetEntry [base=" + this.base + ", set=" + this.set + "]";
  }
}
