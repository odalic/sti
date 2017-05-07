package cz.cuni.mff.xrg.odalic.groups;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.SortedSet;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.users.User;

/**
 * Manages {@link Group}s.
 *
 * @author Václav Brodec
 *
 */
public interface GroupsService {

  void initializeDefaults(User owner) throws IOException;
  
  SortedSet<Group> getGroups(String userId);
  
  Group getGroup(String userId, String groupId);
  
  Group verifyGroupExistenceById(String userId, String groupId);
  
  void create(Group group);
  
  void replace(Group group);

  boolean existsGroupWithId(String userId, String groupId);
  
  void deleteById(String userId, String groupId);
  
  void subscribe(KnowledgeBase base);
  
  void unsubscribe(KnowledgeBase base);

  Set<Group> detectUsed(String userId, URL endpoint);

  Group merge(Group group);

  void deleteAll(String userId);

}
