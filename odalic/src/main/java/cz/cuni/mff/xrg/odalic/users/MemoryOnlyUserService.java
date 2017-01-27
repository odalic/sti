/**
 * 
 */
package cz.cuni.mff.xrg.odalic.users;

import java.net.MalformedURLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

  private static final String SESSION_MAXIMUM_HOURS_PROPERTY_KEY = "cz.cuni.mff.xrg.odalic.users.session.maximum.hours";

  private static final Logger logger = LoggerFactory.getLogger(MemoryOnlyUserService.class);

  private final RandomService randomService;
  private final PasswordHashingService passwordHashingService;
  private final MailService mailService;
  private final TokenService tokenService;

  private final long sessionMaximumHours;
  
  private final Map<String, Credentials> codesToUnconfirmed;
  private final Map<String, User> codesToPasswordChanging;

  private final Map<String, User> users;

  @Autowired
  public MemoryOnlyUserService(final PropertiesService propertiesService,
      final RandomService randomService, final PasswordHashingService passwordHashingService,
      final MailService mailService, final TokenService tokenService) {
    Preconditions.checkNotNull(propertiesService);
    Preconditions.checkNotNull(randomService);
    Preconditions.checkNotNull(passwordHashingService);
    Preconditions.checkNotNull(mailService);
    Preconditions.checkNotNull(tokenService);

    final Properties properties = propertiesService.get();

    this.randomService = randomService;
    this.passwordHashingService = passwordHashingService;
    this.mailService = mailService;
    this.tokenService = tokenService;

    this.users = new HashMap<>();

    final int maximumCodesKept;
    try {
      maximumCodesKept = Integer.parseInt(properties.getProperty(MAXIMUM_CODES_KEPT_PROPERTY_KEY));
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid maximum codes kept value!", e);
    }
    this.codesToUnconfirmed = new FixedSizeHashMap<>(maximumCodesKept);
    this.codesToPasswordChanging = new FixedSizeHashMap<>(maximumCodesKept);
    
    try {
      this.sessionMaximumHours = Long.parseLong(properties.getProperty(SESSION_MAXIMUM_HOURS_PROPERTY_KEY));
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid maximum session duration value!", e);
    }
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.users.UserService#signUp(java.net.URL, java.lang.String, cz.cuni.mff.xrg.odalic.users.Credentials)
   */
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
    return this.randomService.getRandomString();
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.users.UserService#create(cz.cuni.mff.xrg.odalic.users.Credentials, cz.cuni.mff.xrg.odalic.users.Role)
   */
  @Override
  public void create(final Credentials credentials, final Role role) {
    Preconditions.checkNotNull(credentials);
    Preconditions.checkNotNull(role);
    Preconditions.checkArgument(!this.users.containsKey(credentials.getEmail()));

    final String email = credentials.getEmail();
    final String passwordHashed = hash(credentials.getPassword());

    this.users.put(email, new User(email, passwordHashed, role));
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.users.UserService#authenticate(cz.cuni.mff.xrg.odalic.users.Credentials)
   */
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

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.users.UserService#activateUser(java.lang.String)
   */
  @Override
  public void activateUser(final String code) {
    final Credentials credentials = this.codesToUnconfirmed.remove(code);
    if (credentials == null) {
      logger.warn("Invalid confirmation code %s!", code);
      throw new IllegalArgumentException("Invalid confirmation code!");
    }

    create(credentials, Role.USER);
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.users.UserService#issueToken(cz.cuni.mff.xrg.odalic.users.User)
   */
  @Override
  public Token issueToken(User user) {
    final String subject = user.getEmail();
    final Instant expiration = Instant.now().plus(Duration.ofHours(sessionMaximumHours));
    
    return this.tokenService.create(subject, expiration);
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.users.UserService#requestPasswordChange(java.net.URL, java.lang.String, cz.cuni.mff.xrg.odalic.users.User, java.lang.String)
   */
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

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.users.UserService#confirmPasswordChange(java.lang.String)
   */
  @Override
  public void confirmPasswordChange(final String code) {
    final User user = this.codesToPasswordChanging.remove(code);
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

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.users.UserService#validateToken(java.lang.String)
   */
  @Override
  public User validateToken(String token) {
    this.tokenService.validate(token);
    
    final String subject = this.tokenService.getSubject(token);
    final User user = this.users.get(subject);
    
    if (user == null) {
      logger.warn("Unknown user for a subject %s derived from token %s!", subject, token);
      throw new IllegalArgumentException("Authentication failed!");
    }
    
    return user;
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.users.UserService#getUser(java.lang.String)
   */
  @Override
  public User getUser(final String id) {
    Preconditions.checkNotNull(id);

    final User user = this.users.get(id);
    Preconditions.checkArgument(user != null);

    return user;
  }
}
