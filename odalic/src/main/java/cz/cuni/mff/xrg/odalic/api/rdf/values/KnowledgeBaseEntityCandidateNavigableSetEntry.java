package cz.cuni.mff.xrg.odalic.api.rdf.values;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

@RdfsClass("http://odalic.eu/internal/KnowledgeBaseEntityCandidateNavigableSetEntry")
public class KnowledgeBaseEntityCandidateNavigableSetEntry {

  private KnowledgeBaseValue base;

  private EntityCandidateNavigableSetWrapper set;

  public KnowledgeBaseEntityCandidateNavigableSetEntry() {}

  public KnowledgeBaseEntityCandidateNavigableSetEntry(final KnowledgeBaseValue base,
      final EntityCandidateNavigableSetWrapper set) {
    Preconditions.checkNotNull(base);
    Preconditions.checkNotNull(set);

    this.base = base;
    this.set = set;
  }

  /**
   * @return the base
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseEntityCandidateNavigableSetEntry/base")
  public KnowledgeBaseValue getBase() {
    return this.base;
  }

  /**
   * @return the set
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseEntityCandidateNavigableSetEntry/set")
  public EntityCandidateNavigableSetWrapper getSet() {
    return this.set;
  }

  /**
   * @param base the base to set
   */
  public void setBase(final KnowledgeBaseValue base) {
    Preconditions.checkNotNull(base);

    this.base = base;
  }

  /**
   * @param set the set to set
   */
  public void setSet(final EntityCandidateNavigableSetWrapper set) {
    Preconditions.checkNotNull(set);

    this.set = set;
  }

  @Override
  public String toString() {
    return "KnowledgeBaseEntityCandidateNavigableSetEntry [base=" + this.base + ", set=" + this.set
        + "]";
  }
}
