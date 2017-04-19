package cz.cuni.mff.xrg.odalic.bases;

import java.util.NavigableSet;
import javax.annotation.Nullable;

/**
 * Provides basic capabilities of bases management.
 *
 * @author Václav Brodec
 *
 */
public interface BasesService {

  /**
   * @param userId owner's ID
   * @return bases naturally ordered
   */
  NavigableSet<KnowledgeBase> getBases(String userId);
  
  /**
   * @param userId owner's ID
   * @return insert supporting bases, naturally ordered
   */
  NavigableSet<KnowledgeBase> getInsertSupportingBases(String userId);
  
  void create(KnowledgeBase base);
  
  void replace(KnowledgeBase base);
  
  boolean existsBaseWithId(String userId, String baseId);

  KnowledgeBase getByName(String userId, String name) throws IllegalArgumentException;
  
  @Nullable
  KnowledgeBase verifyBaseExistenceByName(String userId, String name);
}
