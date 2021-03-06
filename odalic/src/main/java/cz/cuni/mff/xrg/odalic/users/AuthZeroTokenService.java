/**
 *
 */
package cz.cuni.mff.xrg.odalic.users;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;

/**
 * A {@link TokenService} implementation based on {@link JWT}.
 *
 * @author Václav Brodec
 *
 */
public class AuthZeroTokenService implements TokenService {

  private static final String ISSUER_PROPERTY_KEY = "cz.cuni.mff.xrg.odalic.tokens.issuer";
  private static final String SECRET_PROPERTY_KEY = "cz.cuni.mff.xrg.odalic.tokens.secret";

  private static JWTVerifier initializeVerifier(final String secret, final String issuer) {
    try {
      return JWT.require(Algorithm.HMAC256(secret)).withIssuer(issuer).build();
    } catch (final UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  private final String secret;
  private final String issuer;

  private final JWTVerifier verifier;

  @Autowired
  public AuthZeroTokenService(final PropertiesService propertiesService) {
    Preconditions.checkNotNull(propertiesService, "The propertiesService cannot be null!");

    final Properties properties = propertiesService.get();

    this.secret = properties.getProperty(SECRET_PROPERTY_KEY);
    Preconditions.checkArgument(this.secret != null, String.format("The %s key is missing in the configuration!", SECRET_PROPERTY_KEY));

    this.issuer = properties.getProperty(ISSUER_PROPERTY_KEY);
    Preconditions.checkArgument(this.issuer != null, String.format("The %s key is missing in the configuration!", ISSUER_PROPERTY_KEY));

    this.verifier = initializeVerifier(this.secret, this.issuer);
  }

  public AuthZeroTokenService(final String secret, final String issuer) {
    Preconditions.checkNotNull(secret, "The secret cannot be null!");
    Preconditions.checkNotNull(issuer, "The issuer cannot be null!");

    this.secret = secret;
    this.issuer = issuer;

    this.verifier = initializeVerifier(secret, issuer);
  }

  @Override
  public Token create(final UUID id, final String subject, final Instant expiration) {
    Preconditions.checkNotNull(id, "The id cannot be null!");
    Preconditions.checkNotNull(subject, "The subject cannot be null!");
    Preconditions.checkNotNull(expiration, "The expiration cannot be null!");

    final String token;
    try {
      token = JWT.create().withJWTId(id.toString()).withIssuer(this.issuer)
          .withExpiresAt(java.util.Date.from(expiration)).withSubject(subject)
          .sign(Algorithm.HMAC256(this.secret));
    } catch (final JWTCreationException e) {
      throw new IllegalArgumentException(e);
    } catch (final UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }

    return new Token(token);
  }

  private UUID parseId(final DecodedJWT jwt) {
    final UUID id;
    try {
      id = UUID.fromString(jwt.getId());
    } catch (final IllegalArgumentException e) {
      throw new IllegalArgumentException(e);
    }
    return id;
  }

  @Override
  public DecodedToken validate(final Token token) throws IllegalArgumentException {
    final DecodedJWT jwt = validateAndDecode(token);

    return new DecodedToken(parseId(jwt), jwt.getIssuer(), jwt.getSubject(),
        jwt.getExpiresAt().toInstant());
  }

  private DecodedJWT validateAndDecode(final Token token) {
    final DecodedJWT jwt = verifyAndDecode(token);
    verifySubjectPresence(jwt);
    verifyIdPresence(jwt);;

    return jwt;
  }

  private DecodedJWT verifyAndDecode(final Token token) {
    final DecodedJWT jwt;
    try {
      jwt = this.verifier.verify(token.getToken());
    } catch (final JWTVerificationException e) {
      throw new IllegalArgumentException(String.format("Token %s verification failed!", token), e);
    }
    return jwt;
  }

  private void verifyIdPresence(final DecodedJWT jwt) {
    Preconditions.checkArgument(jwt.getId() != null, "The ID is not present!");
  }

  private void verifySubjectPresence(final DecodedJWT jwt) {
    Preconditions.checkArgument(jwt.getSubject() != null, "The subject is not present!");
  }
}
