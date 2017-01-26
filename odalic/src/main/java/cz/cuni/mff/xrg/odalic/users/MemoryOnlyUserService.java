/**
 * 
 */
package cz.cuni.mff.xrg.odalic.users;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.util.FixedSizeHashMap;
import cz.cuni.mff.xrg.odalic.util.URL;
import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;
import cz.cuni.mff.xrg.odalic.util.hash.PasswordHashingService;
import cz.cuni.mff.xrg.odalic.util.mail.MailService;

/**
 * This {@link UserService} implementation provides no persistence.
 * 
 * @author Václav Brodec
 *
 */
public final class MemoryOnlyUserService implements UserService {

  private static final String MAXIMUM_CODES_KEPT_PROPERTY_KEY =
      "cz.cuni.mff.xrg.odalic.users.maximumCodesKept";

  private static final String ODALIC_SIGN_UP_CONFIRMATION_SUBJECT = "Odalic sign-up confirmation";
  private static final String SIGN_UP_MESSAGE_FORMAT =
      "Please confirm your registration by following this link: %s\n";

  private static final String ODALIC_PASSWORD_CHANGING_CONFIRMATION_SUBJECT =
      "Odalic password setting confirmation";
  private static final String PASSWORD_SETTING_MESSAGE_FORMAT =
      "Please confirm the setting of a new password by following this link: %s\n";

  private static final Logger logger = LoggerFactory.getLogger(MemoryOnlyUserService.class);

  private final PasswordHashingService passwordHashingService;
  private final MailService mailService;

  private final Map<String, Credentials> codesToUnconfirmed;
  private Map<String, User> codesToPasswordChanging;

  private final Map<String, User> users;

  @Autowired
  public MemoryOnlyUserService(final PropertiesService propertiesService,
      final PasswordHashingService passwordHashingService, final MailService mailService) {
    Preconditions.checkNotNull(propertiesService);
    Preconditions.checkNotNull(passwordHashingService);
    Preconditions.checkNotNull(mailService);

    final Properties properties = propertiesService.get();

    this.passwordHashingService = passwordHashingService;
    this.mailService = mailService;

    this.users = new HashMap<>();

    final int maximumCodesKept =
        Integer.parseInt(properties.getProperty(MAXIMUM_CODES_KEPT_PROPERTY_KEY));
    this.codesToUnconfirmed = new FixedSizeHashMap<>(maximumCodesKept);
  }

  @Override
  public void signUp(final java.net.URL confirmationUrl, final String codeQueryParameter,
      final Credentials credentials) throws MalformedURLException {
    Preconditions.checkNotNull(confirmationUrl);
    Preconditions.checkNotNull(codeQueryParameter);
    Preconditions.checkNotNull(credentials);
    Preconditions.checkNotNull(!codeQueryParameter.isEmpty());

    final Address address = extractAddress(credentials);
    final String code = generateCode();

    this.codesToUnconfirmed.put(code, credentials);

    final java.net.URL confirmationCodeUrl =
        URL.setQueryParameter(confirmationUrl, codeQueryParameter, code);
    this.mailService.send(ODALIC_SIGN_UP_CONFIRMATION_SUBJECT,
        generateSignUpMessage(confirmationCodeUrl), new Address[] {address});
  }

  private static Address extractAddress(final Credentials credentials) {
    final Address address;
    try {
      address = new InternetAddress(credentials.getEmail(), true);
    } catch (final AddressException e) {
      throw new IllegalArgumentException(e);
    }
    return address;
  }

  private static String generateSignUpMessage(final java.net.URL confirmationCodeUrl) {
    return String.format(SIGN_UP_MESSAGE_FORMAT, confirmationCodeUrl);
  }

  private String generateCode() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void create(final Credentials credentials, final Role role) {
    Preconditions.checkNotNull(credentials);
    Preconditions.checkNotNull(role);
    Preconditions.checkArgument(!this.users.containsKey(credentials.getEmail()));

    final String email = credentials.getEmail();
    final String passwordHashed = hash(credentials.getPassword());

    this.users.put(email, new User(email, passwordHashed, role));
  }

  @Override
  public User authenticate(final Credentials credentials) {
    Preconditions.checkNotNull(credentials);

    final User user = this.users.get(credentials.getEmail());
    if (user == null) {
      logger.warn("No user found for %s!", credentials);
      throw new IllegalArgumentException(
          String.format("Authorization failed for %s.", credentials.getEmail()));
    }

    if (!check(credentials.getPassword(), user.getPasswordHash())) {
      logger.warn("Invalid password for %s!", credentials);
      throw new IllegalArgumentException(
          String.format("Authorization failed for %s.", credentials.getEmail()));
    }

    return user;
  }

  @Override
  public void activateUser(final String code) {
    final Credentials credentials = this.codesToUnconfirmed.get(code);
    if (credentials == null) {
      logger.warn("Invalid confirmation code %s!", code);
      throw new IllegalArgumentException("Invalid confirmation code!");
    }

    create(credentials, Role.USER);
  }

  @Override
  public Token issueToken(User user) {
    final String issuer = "odalic";
    final String subject = user.getEmail();
    final Date expiration = Date.from(Instant.now().plus(Duration.ofDays(4)));
    
    try {
      try {
        String token = JWT.create()
            .withIssuer(issuer)
            .withExpiresAt(expiration)
            .withSubject(subject)
            .sign(Algorithm.HMAC256("sec"));
      } catch (IllegalArgumentException | UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (final JWTCreationException e){
        //Invalid Signing configuration / Couldn't convert Claims.
    }
    return null;
  }

  @Override
  public void requestPasswordChange(final java.net.URL confirmationUrl,
      final String codeQueryParameter, User user, String password) throws MalformedURLException {
    Preconditions.checkNotNull(confirmationUrl);
    Preconditions.checkNotNull(codeQueryParameter);
    Preconditions.checkNotNull(user);
    Preconditions.checkNotNull(password);
    Preconditions.checkNotNull(!codeQueryParameter.isEmpty());
    Preconditions.checkNotNull(!password.isEmpty());

    final Address address = extractAddress(user);
    final String code = generateCode();

    this.codesToPasswordChanging.put(code,
        new User(user.getEmail(), hash(password), user.getRole()));

    final java.net.URL confirmationCodeUrl =
        URL.setQueryParameter(confirmationUrl, codeQueryParameter, code);
    this.mailService.send(ODALIC_PASSWORD_CHANGING_CONFIRMATION_SUBJECT,
        generatePasswordChangingMessage(confirmationCodeUrl), new Address[] {address});
  }

  private static Address extractAddress(User user) {
    final Address address;
    try {
      address = new InternetAddress(user.getEmail(), true);
    } catch (final AddressException e) {
      throw new IllegalArgumentException(e);
    }
    return address;
  }

  private static String generatePasswordChangingMessage(final java.net.URL confirmationCodeUrl) {
    return String.format(PASSWORD_SETTING_MESSAGE_FORMAT, confirmationCodeUrl);
  }

  @Override
  public void confirmPasswordChange(final String code) {
    final User user = this.codesToPasswordChanging.get(code);
    if (user == null) {
      logger.warn("Invalid confirmation code %s!", code);
      throw new IllegalArgumentException("Invalid confirmation code!");
    }

    final User replaced = this.users.replace(user.getEmail(), user);
    Preconditions.checkState(replaced != null, "Nonexisting user!");
  }

  private String hash(final String password) {
    return passwordHashingService.hash(password);
  }

  private boolean check(final String password, final String passwordHash) {
    return passwordHashingService.check(password, passwordHash);
  }

  @Override
  public User validateToken(String token) {
    final JWT jwt;
    try {
      jwt = JWT.decode(token);
    } catch (final JWTDecodeException e) {
      throw new IllegalArgumentException(e);
    }
    return null;
  }

  @Override
  public User getUser(final String id) {
    Preconditions.checkNotNull(id);

    final User user = this.users.get(id);
    Preconditions.checkArgument(user != null);

    return user;
  }
}
