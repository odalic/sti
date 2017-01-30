package cz.cuni.mff.xrg.odalic.users;

import java.time.Instant;
import java.util.UUID;

public interface TokenService {
  /**
   * Creates a token.
   * 
   * @param id token ID 
   * @param subject token subject
   * @param expiration expiration time
   * @return token
   */
  Token create(UUID id, final String subject, final Instant expiration);
  
  /**
   * Validates the token and returns it in its decoded form.
   * 
   * @param token token string
   * @return decoded token
   * @throws IllegalArgumentException when the validation fails
   */
  DecodedToken validate(Token token) throws IllegalArgumentException;
}
