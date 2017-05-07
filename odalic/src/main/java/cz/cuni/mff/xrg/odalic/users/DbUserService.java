/**
 *
 */
package cz.cuni.mff.xrg.odalic.users;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

import cz.cuni.mff.xrg.odalic.files.FileService;
import cz.cuni.mff.xrg.odalic.groups.GroupsService;
import cz.cuni.mff.xrg.odalic.tasks.TaskService;
import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;
import cz.cuni.mff.xrg.odalic.util.hash.PasswordHashingService;
import cz.cuni.mff.xrg.odalic.util.mail.MailService;
import cz.cuni.mff.xrg.odalic.util.storage.DbService;

/**
 * This {@link UserService} implementation persists the users in {@link DB}-backed maps.
 *
 * @author Václav Brodec
 *
 */
public final class DbUserService implements UserService {

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


  private static final Logger logger = LoggerFactory.getLogger(DbUserService.class);


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

  private static int getMaxCodesKept(final Properties properties) {
    final String maximumCodesKeptString = properties.getProperty(MAXIMUM_CODES_KEPT_PROPERTY_KEY);
    Preconditions.checkNotNull(maximumCodesKeptString);

    try {
      return Integer.parseInt(maximumCodesKeptString);
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid maximum codes kept value!", e);
    }
  }

  private static String getPasswordSettingConfirmationUrlFormat(final Properties properties) {
    final String passwordSettingConfirmationUrlFormat =
        properties.getProperty(PASSWORD_SETTING_CONFIRMATION_URL_FORMAT_PROPERTY_KEY);

    Preconditions.checkArgument(passwordSettingConfirmationUrlFormat != null,
        String.format("Missing key %s in the configuration!",
            PASSWORD_SETTING_CONFIRMATION_URL_FORMAT_PROPERTY_KEY));

    try {
      formatUrlWithToken(passwordSettingConfirmationUrlFormat, new Token("dummy"));
    } catch (final IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid password setting confirmation URL format set!");
    }

    return passwordSettingConfirmationUrlFormat;
  }

  private static long getPasswordSettingConfirmationWindowsMinutes(final Properties properties) {
    final String passwordSettingConfirmationWindowMinutesString =
        properties.getProperty(PASSWORD_SETTING_CONFIRMATION_WINDOW_MINUTES_PROPERTY_KEY);
    Preconditions.checkArgument(passwordSettingConfirmationWindowMinutesString != null,
        String.format("Missing key %s in the configuration!",
            PASSWORD_SETTING_CONFIRMATION_WINDOW_MINUTES_PROPERTY_KEY));
    final long passwordSettingConfirmationWindowMinutes;
    try {
      passwordSettingConfirmationWindowMinutes =
          Long.parseLong(passwordSettingConfirmationWindowMinutesString);
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException(
          "Invalid password setting confirmation windows duration value!", e);
    }
    return passwordSettingConfirmationWindowMinutes;
  }

  private static long getSessionMaximumHours(final Properties properties) {
    final String sessionMaximumHoursString =
        properties.getProperty(SESSION_MAXIMUM_HOURS_PROPERTY_KEY);
    Preconditions.checkArgument(sessionMaximumHoursString != null,
        String.format("Missing key %s in the configuration!", SESSION_MAXIMUM_HOURS_PROPERTY_KEY));

    try {
      return Long.parseLong(sessionMaximumHoursString);
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid maximum session duration value!", e);
    }
  }

  private static String getSignUpConfirmationUrlFormat(final Properties properties) {
    final String signUpConfirmationUrlFormat =
        properties.getProperty(SIGNUP_CONFIRMATION_URL_FORMAT_PROPERTY_KEY);

    Preconditions.checkArgument(signUpConfirmationUrlFormat != null, String.format(
        "Missing key %s in the configuration!", SIGNUP_CONFIRMATION_URL_FORMAT_PROPERTY_KEY));

    try {
      formatUrlWithToken(signUpConfirmationUrlFormat, new Token("dummy"));
    } catch (final IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid sign-up confirmation URL format set!");
    }

    return signUpConfirmationUrlFormat;
  }

  private static long getSignUpConfirmationWindowMinutes(final Properties properties) {
    final String signUpConfirmationWindowMinutesString =
        properties.getProperty(SIGNUP_CONFIRMATION_WINDOW_MINUTES_PROPERTY_KEY);
    Preconditions.checkArgument(signUpConfirmationWindowMinutesString != null, String.format(
        "Missing key %s in the configuration!", SIGNUP_CONFIRMATION_WINDOW_MINUTES_PROPERTY_KEY));
    try {
      return Long.parseLong(signUpConfirmationWindowMinutesString);
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException("Invalid sign-up confirmation windows duration value!", e);
    }
  }

  private final PasswordHashingService passwordHashingService;

  private final MailService mailService;
  private final TokenService tokenService;

  private final TaskService taskService;

  private final FileService fileService;
  
  private final GroupsService groupsService;

  private final long signUpConfirmationWindowMinutes;

  private final long passwordSettingConfirmationWindowMinutes;

  private final long sessionMaximumHours;

  private final String signUpConfirmationUrlFormat;

  private final String passwordSettingConfirmationUrlFormat;

  /**
   * The shared database instance.
   */
  private final DB db;

  private final Map<UUID, Credentials> tokenIdsToUnconfirmed;

  private final Map<UUID, User> tokenIdsToPasswordChanging;


  private final Map<String, User> userIdsToUsers;


  /**
   * A multimap from user IDs to tokenIDs implemented as a map of user ID and token ID pairs to
   * dummy boolean values.
   */
  private final BTreeMap<Object[], Boolean> userIdsToTokenIds;


  @SuppressWarnings("unchecked")
  @Autowired
  public DbUserService(final PropertiesService propertiesService,
      final PasswordHashingService passwordHashingService, final MailService mailService,
      final TokenService tokenService, final DbService dbService, final TaskService taskService,
      final FileService fileService, final GroupsService groupsService) throws IOException {
    Preconditions.checkNotNull(propertiesService);
    Preconditions.checkNotNull(passwordHashingService);
    Preconditions.checkNotNull(mailService);
    Preconditions.checkNotNull(tokenService);
    Preconditions.checkNotNull(taskService);
    Preconditions.checkNotNull(fileService);
    Preconditions.checkNotNull(groupsService);

    final Properties properties = propertiesService.get();

    this.passwordHashingService = passwordHashingService;
    this.mailService = mailService;
    this.tokenService = tokenService;
    this.taskService = taskService;
    this.fileService = fileService;
    this.groupsService = groupsService;

    this.db = dbService.getDb();

    this.userIdsToUsers =
        this.db.hashMap("userIdsToUsers", Serializer.STRING, Serializer.JAVA).createOrOpen();

    final int maximumCodesKept = getMaxCodesKept(properties);

    this.tokenIdsToUnconfirmed =
        this.db.hashMap("tokenIdsToUnconfirmed", Serializer.UUID, Serializer.JAVA)
            .expireMaxSize(maximumCodesKept).expireAfterCreate().expireAfterGet()
            .expireAfterUpdate().createOrOpen();
    this.tokenIdsToPasswordChanging =
        this.db.hashMap("tokenIdsToPasswordChanging", Serializer.UUID, Serializer.JAVA)
            .expireMaxSize(maximumCodesKept).expireAfterCreate().expireAfterGet()
            .expireAfterUpdate().createOrOpen();

    this.sessionMaximumHours = getSessionMaximumHours(properties);
    this.signUpConfirmationWindowMinutes = getSignUpConfirmationWindowMinutes(properties);
    this.passwordSettingConfirmationWindowMinutes =
        getPasswordSettingConfirmationWindowsMinutes(properties);

    this.signUpConfirmationUrlFormat = getSignUpConfirmationUrlFormat(properties);
    this.passwordSettingConfirmationUrlFormat = getPasswordSettingConfirmationUrlFormat(properties);

    this.userIdsToTokenIds =
        this.db
            .treeMap("userIdsToTokenIds",
                new SerializerArrayTuple(Serializer.STRING, Serializer.UUID), Serializer.BOOLEAN)
            .createOrOpen();

    createAdminIfNotPresent(properties);
  }

  @Override
  public void activateUser(final Token token) throws IOException {
    final DecodedToken decodedToken = validateAndDecode(token);

    final Credentials credentials = matchCredentials(decodedToken);

    try {
      doCreate(credentials, Role.USER);
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }

    this.db.commit();
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

    final User replaced;
    try {
      replaced = replace(newUser);
    } catch (final Exception e) {
      this.db.rollback();
      throw e;
    }

    invalidateTokens(replaced);

    this.db.commit();
  }

  @Override
  public void create(final Credentials credentials, final Role role) throws IOException {
    Preconditions.checkNotNull(credentials);
    Preconditions.checkNotNull(role);

    final User user = doCreate(credentials, role);

    this.db.commit();
    
    this.groupsService.initializeDefaults(user);
  }

  private void createAdminIfNotPresent(final Properties properties) throws IOException {
    final String adminEmail = properties.getProperty(ADMIN_EMAIL_PROPERTY_KEY);
    Preconditions.checkArgument(adminEmail != null,
        String.format("Missing key %s in the configuration!", ADMIN_EMAIL_PROPERTY_KEY));

    final String adminInitialPassword = properties.getProperty(ADMIN_INITIAL_PASSWORD_PROPERTY_KEY);
    Preconditions.checkArgument(adminInitialPassword != null,
        String.format("Missing key %s in the configuration!", ADMIN_INITIAL_PASSWORD_PROPERTY_KEY));
    Preconditions.checkArgument(!adminInitialPassword.isEmpty(),
        "The initial admin password cannot be empty!");

    if (this.userIdsToUsers.containsKey(adminEmail)) {
      logger.info("The administrator account already present. Skipping its creation.");
      return;
    }

    final User admin = doCreate(new Credentials(adminEmail, adminInitialPassword), Role.ADMINISTRATOR);
    this.db.commit();
    
    this.groupsService.initializeDefaults(admin);
  }

  @Override
  public void deleteUser(final String userId) {
    this.taskService.deleteAll(userId);
    this.fileService.deleteAll(userId);

    this.userIdsToTokenIds.prefixSubMap(new Object[] {userId}).clear();
    final User removed = this.userIdsToUsers.remove(userId);
    Preconditions.checkArgument(removed != null, "No such user exists!");
  }

  private User doCreate(final Credentials credentials, final Role role) throws IOException {
    Preconditions.checkArgument(!this.userIdsToUsers.containsKey(credentials.getEmail()));

    final String email = credentials.getEmail();
    final String passwordHashed = hash(credentials.getPassword());

    final User user = new User(email, passwordHashed, role);
    
    this.userIdsToUsers.put(email, user);
    
    return user;
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
    if (!this.userIdsToTokenIds.containsKey(new Object[] {userId, decodedToken.getId()})) {
      logger.warn("Obsolete token {}!", decodedToken);
      throw new IllegalArgumentException("Authentication failed!");
    }
  }

  private void invalidateTokens(final User user) {
    this.userIdsToTokenIds.prefixSubMap(new Object[] {user.getEmail()}).clear();
  }

  @Override
  public Token issueToken(final User user) {
    final UUID tokenId = UUID.randomUUID();
    final String userId = user.getEmail();
    final Instant expiration = Instant.now().plus(Duration.ofHours(this.sessionMaximumHours));

    final Token token = this.tokenService.create(tokenId, userId, expiration);
    this.userIdsToTokenIds.put(new Object[] {userId, tokenId}, true);

    this.db.commit();

    return token;
  }

  private Credentials matchCredentials(final DecodedToken decodedToken) {
    final UUID tokenId = decodedToken.getId();

    final Credentials credentials = this.tokenIdsToUnconfirmed.remove(tokenId);
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

    this.db.commit();
  }

  @Override
  public void signUp(final Credentials credentials) {
    Preconditions.checkNotNull(credentials);

    final Address address = extractAddress(credentials);

    final Token token = generateSignUpToken(credentials);

    final String message = generateSignUpMessage(token);

    this.mailService.send(ODALIC_SIGN_UP_CONFIRMATION_SUBJECT, message, new Address[] {address});

    this.db.commit();
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
