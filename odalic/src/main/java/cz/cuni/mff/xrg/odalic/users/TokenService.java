package cz.cuni.mff.xrg.odalic.users;

import java.time.Instant;

public interface TokenService {
  /**
   * Creates a token.
   * 
   * @param subject token subject
   * @param expiration expiration time
   * @return token
   */
  Token create(final String subject, final Instant expiration);

  /**
   * Validates the token.
   * 
   * @param token token string
   * @throws IllegalArgumentException when the validation fails
   */
  void validate(String token) throws IllegalArgumentException;

  /**
   * Validates the token in the same way as {@link #validate(String)} and retrieves its subject.
   * 
   * @param token token
   * @return token subject
   */
  String getSubject(String token);
}
