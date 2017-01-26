package cz.cuni.mff.xrg.odalic.users;

import java.net.MalformedURLException;
import java.net.URL;

public interface UserService {
  void signUp(URL confirmationUrl, String codeQueryParameter, Credentials credentials) throws MalformedURLException;
  
  void create(Credentials credentials, Role role);
  
  User authenticate(Credentials credentials);
  
  void activateUser(String code);

  void requestPasswordChange(URL confirmationUrl, String codeQueryParameter, User user, String password) throws MalformedURLException;
  
  void confirmPasswordChange(String code);
  
  Token issueToken(User user);

  User validateToken(String token);

  User getUser(String id);
}
