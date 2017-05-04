package cz.cuni.mff.xrg.odalic.bases;

import java.io.IOException;
import java.util.NavigableSet;
import javax.annotation.Nullable;

import cz.cuni.mff.xrg.odalic.tasks.Task;

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
  
  KnowledgeBase merge(KnowledgeBase base);
  
  boolean existsBaseWithId(String userId, String baseId);

  KnowledgeBase getByName(String userId, String name) throws IllegalArgumentException;
  
  @Nullable
  KnowledgeBase verifyBaseExistenceByName(String userId, String name);

  void deleteById(String userId, String name) throws IOException;

  void subscribe(Task task);

  void unsubscribe(Task previous);

}
