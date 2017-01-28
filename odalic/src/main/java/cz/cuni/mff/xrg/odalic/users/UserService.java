package cz.cuni.mff.xrg.odalic.users;

public interface UserService {
  void signUp(Credentials credentials);
  
  void create(Credentials credentials, Role role);
  
  User authenticate(Credentials credentials);
  
  void activateUser(Token token);

  void requestPasswordChange(User user, String password);
  
  void confirmPasswordChange(Token token);
  
  Token issueToken(User user);

  User validateToken(Token token);

  User getUser(String id);
}
