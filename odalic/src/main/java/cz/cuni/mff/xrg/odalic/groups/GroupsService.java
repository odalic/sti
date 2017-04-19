package cz.cuni.mff.xrg.odalic.groups;

import java.net.URL;
import java.util.Set;
import java.util.SortedSet;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;

/**
 * Manages {@link Group}s.
 *
 * @author Václav Brodec
 *
 */
public interface GroupsService {

  SortedSet<Group> getGroups(String userId);
  
  Group getGroup(String userId, String groupId);
  
  Group verifyGroupExistenceById(String userId, String groupId);
  
  void create(Group group);
  
  void replace(Group group);

  boolean existsGroupWithId(String userId, String groupId);
  
  void subscribe(Group group, KnowledgeBase base);
  
  void unsubscribe(Group group, KnowledgeBase base);

  Set<Group> detectUsed(String userId, URL endpoint);
}
