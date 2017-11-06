package cz.cuni.mff.xrg.odalic.users;

import java.io.IOException;
import java.util.NavigableSet;

public interface UserService {

  //not used with SSO
  void activateUser(Token token) throws IOException;

  //not used with SSO
  User authenticate(Credentials credentials);

  //not used with SSO
  void confirmPasswordChange(Token token);

  //not used with SSO - as it expects password
  void create(Credentials credentials, Role role) throws IOException;

  void deleteUser(String userId);

  User getUser(String id);

  NavigableSet<User> getUsers();

  //not used with SSO
  Token issueToken(User user);

  //not used with SSO
  void requestPasswordChange(User user, String password);

  //not used with SSO
  void signUp(Credentials credentials) throws IOException;

  User validateToken(Token token);
}
