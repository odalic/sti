package cz.cuni.mff.xrg.odalic.util.mail;

import javax.annotation.Nullable;
import javax.mail.Address;

/**
 * Simple mail service.
 * 
 * @author Václav Brodec
 *
 */
public interface MailService {
  /**
   * Sends an e-mail.
   * 
   * @param subject subject
   * @param text message content
   * @param tos recipients
   * @param ccs closed copy recipients
   */
  void send(String subject, String text, Address[] tos, @Nullable Address... ccs);
}
