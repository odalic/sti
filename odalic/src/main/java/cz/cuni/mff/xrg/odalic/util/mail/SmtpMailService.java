/**
 * 
 */
package cz.cuni.mff.xrg.odalic.util.mail;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;
import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesUtil;

/**
 * Uses the configured SMTP server to send e-mail messages.
 * 
 * @author Václav Brodec
 *
 */
public final class SmtpMailService implements MailService {

  public static final String USERNAME_CONFIGURATION_KEY = "mail.username";
  public static final String PASSWORD_CONFIGURATION_KEY = "mail.password";
  public static final String FROM_CONFIGURATION_KEY = "mail.from";

  public static final String MAIL_SMTP_PORT_PROPERTY_KEY = "mail.smtp.port";
  public static final String MAIL_SMTP_AUTH_PROPERTY_KEY = "mail.smtp.auth";
  public static final String MAIL_SMTP_SOCKET_FACTORY_CLASS_PROPERTY_KEY =
      "mail.smtp.socketFactory.class";
  public static final String MAIL_SMTP_SOCKET_FACTORY_PORT_PROPERTY_KEY =
      "mail.smtp.socketFactory.port";
  public static final String MAIL_SMTP_HOST_PROPERTY_KEY = "mail.smtp.host";

  private static final Logger logger = LoggerFactory.getLogger(SmtpMailService.class);

  private final String username;
  private final String password;
  private final Address from;
  private final Properties smtpConfiguration;

  private final Executor executor;

  @Autowired
  public SmtpMailService(final PropertiesService propertiesService) throws AddressException {
    final Properties properties = propertiesService.get();

    this.username = properties.getProperty(USERNAME_CONFIGURATION_KEY);
    Preconditions.checkNotNull(this.username);

    this.password = properties.getProperty(PASSWORD_CONFIGURATION_KEY);
    Preconditions.checkNotNull(this.password);

    this.from = parseFromAddress(properties);
    Preconditions.checkNotNull(this.from);

    this.smtpConfiguration = new Properties();
    PropertiesUtil.copyProperty(MAIL_SMTP_HOST_PROPERTY_KEY, properties, this.smtpConfiguration);
    PropertiesUtil.copyProperty(MAIL_SMTP_SOCKET_FACTORY_PORT_PROPERTY_KEY, properties,
        this.smtpConfiguration);
    PropertiesUtil.copyProperty(MAIL_SMTP_SOCKET_FACTORY_CLASS_PROPERTY_KEY, properties,
        this.smtpConfiguration);
    PropertiesUtil.copyProperty(MAIL_SMTP_AUTH_PROPERTY_KEY, properties, this.smtpConfiguration);
    PropertiesUtil.copyProperty(MAIL_SMTP_PORT_PROPERTY_KEY, properties, this.smtpConfiguration);

    this.executor = Executors.newFixedThreadPool(1);
  }

  private static InternetAddress parseFromAddress(final Properties properties)
      throws AddressException {
    final InternetAddress[] fromAdresses =
        InternetAddress.parse(properties.getProperty(FROM_CONFIGURATION_KEY), true);

    return fromAdresses[0];
  }

  public SmtpMailService(final String username, final String password, final Address from,
      final Properties smtpConfiguration, final Executor executor) {
    Preconditions.checkNotNull(username);
    Preconditions.checkNotNull(password);
    Preconditions.checkNotNull(from);
    Preconditions.checkNotNull(smtpConfiguration);
    Preconditions.checkNotNull(executor);

    this.username = username;
    this.password = password;
    this.from = from;
    this.smtpConfiguration = smtpConfiguration;
    this.executor = executor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.util.mail.MailService#send(java.lang.String, java.lang.String,
   * javax.mail.Address[], javax.mail.Address[])
   */
  @Override
  public void send(final String subject, final String text, final Address[] to,
      final Address... cc) {
    logger.info("Message added to the queue. Subject: {}, text: {}, to: {}, cc: {}.", subject, text,
        Arrays.toString(to), Arrays.toString(cc));

    executor.execute(() -> {
      logger.info("Message processing started. Subject: {}, text: {}, to: {}, cc: {}.", subject,
          text, Arrays.toString(to), Arrays.toString(cc));

      final Session session =
          Session.getInstance(smtpConfiguration, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(username, password);
            }

          });

      logger.info("Message processing session created for {}.", username);

      final Message message = new MimeMessage(session);
      try {
        message.setFrom(from);
        message.setRecipients(Message.RecipientType.TO, to);

        if (cc != null && cc.length > 0) {
          message.addRecipients(Message.RecipientType.CC, cc);
        }

        message.setSubject(subject);
        message.setText(text);

        logger.info("Sending message {}...", message);

        Transport.send(message);

        logger.info("Message {} sent.", message);
      } catch (final Exception e) {
        logger.error(String.format("Sending of message {} failed!", message), e);
      }
    });
  }
}
