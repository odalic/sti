package cz.cuni.mff.xrg.odalic.api.rdf.values;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

@RdfsClass("http://odalic.eu/internal/KnowledgeBaseEntityCandidateNavigableSetEntry")
public class KnowledgeBaseEntityCandidateNavigableSetEntry {

  private KnowledgeBaseValue base;
  
  private EntityCandidateNavigableSetWrapper set;
  
  public KnowledgeBaseEntityCandidateNavigableSetEntry() {}
  
  public KnowledgeBaseEntityCandidateNavigableSetEntry(final KnowledgeBaseValue base, EntityCandidateNavigableSetWrapper set) {
    Preconditions.checkNotNull(base);
    Preconditions.checkNotNull(set);
    
    this.base = base;
    this.set = set;
  }

  /**
   * @return the base
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseEntityCandidateNavigableSetEntry/Base")
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
   * @return the set
   */
  @RdfProperty("http://odalic.eu/internal/KnowledgeBaseEntityCandidateNavigableSetEntry/Set")
  public EntityCandidateNavigableSetWrapper getSet() {
    return set;
  }

  /**
   * @param set the set to set
   */
  public void setSet(EntityCandidateNavigableSetWrapper set) {
    Preconditions.checkNotNull(set);
    
    this.set = set;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "KnowledgeBaseEntityCandidateNavigableSetEntry [base=" + base + ", set=" + set + "]";
  }
}