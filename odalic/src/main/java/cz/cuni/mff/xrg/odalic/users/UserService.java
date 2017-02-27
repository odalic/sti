package cz.cuni.mff.xrg.odalic.users;

import java.util.NavigableSet;

public interface UserService {
  void activateUser(Token token);

  User authenticate(Credentials credentials);

  void confirmPasswordChange(Token token);

  void create(Credentials credentials, Role role);

  void deleteUser(String userId);

  User getUser(String id);

  NavigableSet<User> getUsers();

  Token issueToken(User user);

  void requestPasswordChange(User user, String password);

  void signUp(Credentials credentials);

  User validateToken(Token token);
}
