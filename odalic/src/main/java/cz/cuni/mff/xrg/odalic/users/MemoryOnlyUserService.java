/**
 *
 */
package cz.cuni.mff.xrg.odalic.users;

import java.net.MalformedURLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;

import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;
import cz.cuni.mff.xrg.odalic.util.FixedSizeHashMap;
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

  private static final String SIGNUP_TOKEN_SUBJECT = "signup";

  private static final String ODALIC_SIGN_UP_CONFIRMATION_SUBJECT = "Odalic sign-up confirmation";
  private static final String SIGN_UP_MESSAGE_FORMAT =
      "Please confirm your registration by following this link: %s\n";

  private static final String ODALIC_PASSWORD_CHANGING_CONFIRMATION_SUBJECT =
      "Odalic password setting confirmation";
  private static final String PASSWORD_SETTING_MESSAGE_FORMAT =
      "Please confirm the setting of a new password by following this link: %s\n";

  private static final String MAXIMUM_CODES_KEPT_PROPERTY_KEY =
      "cz.cuni.mff.xrg.odalic.users.maximumCodesKept";
  private static final String SESSION_MAXIMUM_HOURS_PROPERTY_KEY =
      "cz.cuni.mff.xrg.odalic.users.session.maximum.hours";
  private static final String SIGNUP_CONFIRMATION_WINDOW_MINUTES_PROPERTY_KEY =
      "cz.cuni.mff.xrg.odalic.users.signup.window.minutes";
  private static final String PASSWORD_SETTING_CONFIRMATION_WINDOW_MINUTES_PROPERTY_KEY =
      "cz.cuni.mff.xrg.odalic.users.reset.window.minutes";
  private static final String SIGNUP_CONFIRMATION_URL_FORMAT_PROPERTY_KEY =
      "cz.cuni.mff.xrg.odalic.users.signup.url";
  private static final String PASSWORD_SETTING_CONFIRMATION_URL_FORMAT_PROPERTY_KEY =
      "cz.cuni.mff.xrg.odalic.users.reset.url";
  private static final String ADMIN_EMAIL_PROPERTY_KEY = "cz.cuni.mff.xrg.odalic.users.admin.email";
  private static final String ADMIN_INITIAL_PASSWORD_PROPERTY_KEY =
      "cz.cuni.mff.xrg.odalic.users.admin.password";


  private static final Logger logger = LoggerFactory.getLogger(MemoryOnlyUserService.class);


  private static Address extractAddress(final Credentials credentials) {
    final Address address;
    try {
      address = new InternetAddress(credentials.getEmail(), true);
    } catch (final AddressException e) {
      throw new IllegalArgumentException(e);
    }
    return address;
  }

  private static Address extractAddress(final User user) {
    final Address address;
    try {
      address = new InternetAddress(user.getEmail(), true);
    } catch (final AddressException e) {
      throw new IllegalArgumentException(e);
    }
    return address;
  }

  private static String formatMessageWithUrl(final String messageFormat,
      final java.net.URL confirmationCodeUrl) {
    return String.format(messageFormat, confirmationCodeUrl);
  }

  private static java.net.URL formatUrlWithToken(final String urlFormat, final Token token) {
    // The token string is to be URL friendly, so no escaping is needed.
    final String urlString = String.format(urlFormat, token.getToken());

    final java.net.URL confirmationCodeUrl;
    try {
      confirmationCodeUrl = new java.net.URL(urlString);
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("The token cannot be encoded to URL!", e);
    }
    return confirmationCodeUrl;
  }

  private final PasswordHashingService passwordHashingService;

  private final MailService mailService;
  private final TokenService tokenService;
  private final TaskService taskService;

  private final FileService fileService;
  private final long signUpConfirmationWindowMinutes;

  private final long passwordSettingConfirmationWindowMinutes;
  private final long sessionMaximumHours;

  private final String signUpConfirmationUrlFormat;
  private final String passwordSettingConfirmationUrlFormat;

  private final Map<UUID, Credentials> tokenIdsToUnconfirmed;

  private final Map<UUID, User> tokenIdsToPasswordChanging;


  private final Map<String, User> userIdsToUsers;


  private final Multimap<String, UUID> userIdsToTokenIds;

  @Autowired
  public MemoryOnlyUserService(final PropertiesService propertiesService,
      final PasswordHashingService passwordHashingService, final MailService mailService,
      final TokenService tokenService, final TaskService taskService,
      final FileService fileService) {
    Preconditions.checkNotNull(propertiesService);
    Preconditions.checkNotNull(passwordHashingService);
    Preconditions.checkNotNull(mailService);
    Preconditions.checkNotNull(tokenService);
    Preconditions.checkNotNull(taskService);
    Preconditions.checkNotNull(fileService);

    final Properties properties = propertiesService.get();

    this.passwordHashingService = passwordHashingService;
    this.mailService = mailService;
    this.tokenService = tokenService;
    this.taskService = taskService;
    this.fileService = fileService;

    this.userIdsToUsers = new HashMap<>();

    final String maximumCodesKeptString = properties.getProperty(MAXIMUM_CODES_KEPT_PROPERTY_KEY);
    Preconditions.checkNotNull(maximumCodesKeptString);
    final int maximumCodesKept;
    try {
      maximumCodesKept = Integer.parseInt(maximumCodesKeptString);
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid maximum codes kept value!", e);
    }
    this.tokenIdsToUnconfirmed = new FixedSizeHashMap<>(maximumCodesKept);
    this.tokenIdsToPasswordChanging = new FixedSizeHashMap<>(maximumCodesKept);

    final String sessionMaximumHoursString =
        properties.getProperty(SESSION_MAXIMUM_HOURS_PROPERTY_KEY);
    Preconditions.checkArgument(sessionMaximumHoursString != null,
        String.format("Missing key %s in the configuration!", SESSION_MAXIMUM_HOURS_PROPERTY_KEY));
    try {
      this.sessionMaximumHours = Long.parseLong(sessionMaximumHoursString);
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid maximum session duration value!", e);
    }

    final String signUpConfirmationWindowMinutesString =
        properties.getProperty(SIGNUP_CONFIRMATION_WINDOW_MINUTES_PROPERTY_KEY);
    Preconditions.checkArgument(signUpConfirmationWindowMinutesString != null, String.format(
        "Missing key %s in the configuration!", SIGNUP_CONFIRMATION_WINDOW_MINUTES_PROPERTY_KEY));
    try {
      this.signUpConfirmationWindowMinutes = Long.parseLong(signUpConfirmationWindowMinutesString);
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid sign-up confirmation windows duration value!", e);
    }

    final String passwordSettingConfirmationWindowMinutesString =
        properties.getProperty(PASSWORD_SETTING_CONFIRMATION_WINDOW_MINUTES_PROPERTY_KEY);
    Preconditions.checkArgument(passwordSettingConfirmationWindowMinutesString != null,
        String.format("Missing key %s in the configuration!",
            PASSWORD_SETTING_CONFIRMATION_WINDOW_MINUTES_PROPERTY_KEY));
    try {
      this.passwordSettingConfirmationWindowMinutes =
          Long.parseLong(passwordSettingConfirmationWindowMinutesString);
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException(
          "Invalid password setting confirmation windows duration value!", e);
    }

    this.signUpConfirmationUrlFormat =
        properties.getProperty(SIGNUP_CONFIRMATION_URL_FORMAT_PROPERTY_KEY);
    Preconditions.checkArgument(this.signUpConfirmationUrlFormat != null, String.format(
        "Missing key %s in the configuration!", SIGNUP_CONFIRMATION_URL_FORMAT_PROPERTY_KEY));
    try {
      formatUrlWithToken(this.signUpConfirmationUrlFormat, new Token("dummy"));
    } catch (final IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid sign-up confirmation URL format set!");
    }

    this.passwordSettingConfirmationUrlFormat =
        properties.getProperty(PASSWORD_SETTING_CONFIRMATION_URL_FORMAT_PROPERTY_KEY);
    Preconditions.checkArgument(this.passwordSettingConfirmationUrlFormat != null,
        String.format("Missing key %s in the configuration!",
            PASSWORD_SETTING_CONFIRMATION_URL_FORMAT_PROPERTY_KEY));
    try {
      formatUrlWithToken(this.passwordSettingConfirmationUrlFormat, new Token("dummy"));
    } catch (final IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid password setting confirmation URL format set!");
    }

    this.userIdsToTokenIds = HashMultimap.create();

    createAdminIfNotPresent(properties);
  }

  @Override
  public void activateUser(final Token token) {
    final DecodedToken decodedToken = validateAndDecode(token);

    final Credentials credentials = matchCredentials(decodedToken);

    create(credentials, Role.USER);
  }


  @Override
  public User authenticate(final Credentials credentials) {
    Preconditions.checkNotNull(credentials);

    final User user = this.userIdsToUsers.get(credentials.getEmail());
    if (user == null) {
      logger.warn("No user found for {}!", credentials);
      throw new IllegalArgumentException(
          String.format("Authorization failed for %s.", credentials.getEmail()));
    }

    if (!check(credentials.getPassword(), user.getPasswordHash())) {
      logger.warn("Invalid password for {}!", credentials);
      throw new IllegalArgumentException(
          String.format("Authorization failed for %s.", credentials.getEmail()));
    }

    return user;
  }

  @Override
  public void confirmPasswordChange(final Token token) {
    final DecodedToken decodedToken = validateAndDecode(token);

    final User newUser = matchPasswordChangingUser(decodedToken);

    final User replaced = replace(newUser);

    invalidateTokens(replaced);
  }

  @Override
  public void create(final Credentials credentials, final Role role) {
    Preconditions.checkNotNull(credentials);
    Preconditions.checkNotNull(role);
    Preconditions.checkArgument(!this.userIdsToUsers.containsKey(credentials.getEmail()));

    final String email = credentials.getEmail();
    final String passwordHashed = hash(credentials.getPassword());

    this.userIdsToUsers.put(email, new User(email, passwordHashed, role));
  }

  private void createAdminIfNotPresent(final Properties properties) {
    final String adminEmail = properties.getProperty(ADMIN_EMAIL_PROPERTY_KEY);
    Preconditions.checkArgument(adminEmail != null,
        String.format("Missing key %s in the configuration!", ADMIN_EMAIL_PROPERTY_KEY));

    final String adminInitialPassword = properties.getProperty(ADMIN_INITIAL_PASSWORD_PROPERTY_KEY);
    Preconditions.checkArgument(adminInitialPassword != null,
        String.format("Missing key %s in the configuration!", ADMIN_INITIAL_PASSWORD_PROPERTY_KEY));
    Preconditions.checkArgument(!adminInitialPassword.isEmpty(),
        "The initial admin password cannot be empty!");

    if (this.userIdsToTokenIds.containsKey(adminEmail)) {
      logger.info("The administrator account already present. Skipping its creation.");
      return;
    }

    create(new Credentials(adminEmail, adminInitialPassword), Role.ADMINISTRATOR);
  }

  @Override
  public void deleteUser(final String userId) {
    this.taskService.deleteAll(userId);
    this.fileService.deleteAll(userId);

    this.userIdsToTokenIds.removeAll(userId);
    final User removed = this.userIdsToUsers.remove(userId);
    Preconditions.checkArgument(removed != null, "No such user exists!");
  }

  private String generatePasswordChangingMessage(final Token token) {
    final java.net.URL confirmationCodeUrl =
        formatUrlWithToken(this.passwordSettingConfirmationUrlFormat, token);

    return formatMessageWithUrl(PASSWORD_SETTING_MESSAGE_FORMAT, confirmationCodeUrl);
  }

  private String generateSignUpMessage(final Token token) {
    final java.net.URL confirmationCodeUrl =
        formatUrlWithToken(this.signUpConfirmationUrlFormat, token);

    return formatMessageWithUrl(SIGN_UP_MESSAGE_FORMAT, confirmationCodeUrl);
  }

  private Token generateSignUpToken(final Credentials credentials) {
    final UUID tokenId = UUID.randomUUID();
    final Instant expiration =
        Instant.now().plus(Duration.ofMinutes(this.signUpConfirmationWindowMinutes));

    final Token token = this.tokenService.create(tokenId, SIGNUP_TOKEN_SUBJECT, expiration);
    this.tokenIdsToUnconfirmed.put(tokenId, credentials);
    return token;
  }

  @Override
  public User getUser(final String id) {
    Preconditions.checkNotNull(id);

    final User user = this.userIdsToUsers.get(id);
    Preconditions.checkArgument(user != null);

    return user;
  }

  @Override
  public NavigableSet<User> getUsers() {
    return ImmutableSortedSet.copyOf(this.userIdsToUsers.values());
  }

  private String hash(final String password) {
    return this.passwordHashingService.hash(password);
  }

  private boolean check(final String password, final String passwordHash) {
    return this.passwordHashingService.check(password, passwordHash);
  }

  private void checkFreshness(final DecodedToken decodedToken, final String userId) {
    if (!this.userIdsToTokenIds.containsEntry(userId, decodedToken.getId())) {
      logger.warn("Obsolete token {}!", decodedToken);
      throw new IllegalArgumentException("Authentication failed!");
    }
  }

  private void invalidateTokens(final User user) {
    this.userIdsToTokenIds.removeAll(user.getEmail());
  }

  @Override
  public Token issueToken(final User user) {
    final UUID tokenId = UUID.randomUUID();
    final String userId = user.getEmail();
    final Instant expiration = Instant.now().plus(Duration.ofHours(this.sessionMaximumHours));

    final Token token = this.tokenService.create(tokenId, userId, expiration);
    this.userIdsToTokenIds.put(userId, tokenId);

    return token;
  }

  private Credentials matchCredentials(final DecodedToken decodedToken) {
    final Credentials credentials = this.tokenIdsToUnconfirmed.remove(decodedToken.getId());
    if (credentials == null) {
      logger.warn("Unknown sign-up confirmation token {}!", decodedToken);
      throw new IllegalArgumentException("Invalid confirmation code!");
    }
    return credentials;
  }

  private User matchPasswordChangingUser(final DecodedToken decodedToken) {
    final User user = this.tokenIdsToPasswordChanging.remove(decodedToken.getId());
    if (user == null) {
      logger.warn("Invalid password setting confirmation token {}!", decodedToken);
      throw new IllegalArgumentException("Invalid confirmation code!");
    }
    return user;
  }

  private User matchUser(final DecodedToken decodedToken) {
    final String userId = decodedToken.getSubject();
    final User user = this.userIdsToUsers.get(userId);
    if (user == null) {
      logger.warn("Unknown user for token {}!", decodedToken);
      throw new IllegalArgumentException("Authentication failed!");
    }

    checkFreshness(decodedToken, user.getEmail());

    return user;
  }

  private User replace(final User user) {
    final User replaced = this.userIdsToUsers.replace(user.getEmail(), user);
    Preconditions.checkState(replaced != null, "Nonexisting user!");
    return replaced;
  }

  @Override
  public void requestPasswordChange(final User user, final String newPassword) {
    Preconditions.checkNotNull(user);
    Preconditions.checkNotNull(newPassword);
    Preconditions.checkNotNull(!newPassword.isEmpty());

    final Address address = extractAddress(user);
    final UUID tokenId = UUID.randomUUID();
    final Instant expiration =
        Instant.now().plus(Duration.ofMinutes(this.passwordSettingConfirmationWindowMinutes));

    final Token token = this.tokenService.create(tokenId, SIGNUP_TOKEN_SUBJECT, expiration);

    this.tokenIdsToPasswordChanging.put(tokenId,
        new User(user.getEmail(), hash(newPassword), user.getRole()));

    this.mailService.send(ODALIC_PASSWORD_CHANGING_CONFIRMATION_SUBJECT,
        generatePasswordChangingMessage(token), new Address[] {address});
  }

  @Override
  public void signUp(final Credentials credentials) {
    Preconditions.checkNotNull(credentials);

    final Address address = extractAddress(credentials);

    final Token token = generateSignUpToken(credentials);

    final String message = generateSignUpMessage(token);

    this.mailService.send(ODALIC_SIGN_UP_CONFIRMATION_SUBJECT, message, new Address[] {address});
  }

  private DecodedToken validateAndDecode(final Token token) {
    final DecodedToken decodedToken;
    try {
      decodedToken = this.tokenService.validate(token);
    } catch (final IllegalArgumentException e) {
      logger.warn("Token validation failed!", e);
      throw new IllegalArgumentException("Authentication failed!");
    }
    return decodedToken;
  }

  @Override
  public User validateToken(final Token token) {
    final DecodedToken decodedToken = validateAndDecode(token);

    return matchUser(decodedToken);
  }
}
