package cz.cuni.mff.xrg.odalic.users;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.CredentialsAdapter;

/**
 * User credentials.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(CredentialsAdapter.class)
public final class Credentials implements Serializable {

  private static final long serialVersionUID = 2412143757527351681L;

  private static void checkEmailAddressFormat(final String email) {
    try {
      new InternetAddress(email).validate();
    } catch (final AddressException e) {
      throw new IllegalArgumentException("Illegal format of the e-mail address!", e);
    }
  }

  private final String email;

  private final String password;

  /**
   * Creates the credentials.
   *
   * @param email user's email serving also as the login
   * @param password user's chosen password
   */
  public Credentials(final String email, final String password) {
    Preconditions.checkNotNull(email);
    Preconditions.checkNotNull(password);

    Preconditions.checkArgument(!email.isEmpty(), "The provided e-mail address is empty!");
    checkEmailAddressFormat(email);

    Preconditions.checkArgument(!password.isEmpty(), "The provided password is empty!");

    this.email = email;
    this.password = password;
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (object == null) {
      return false;
    }
    if (getClass() != object.getClass()) {
      return false;
    }
    final Credentials other = (Credentials) object;
    if (!this.email.equals(other.email)) {
      return false;
    }
    if (!this.password.equals(other.password)) {
      return false;
    }
    return true;
  }

  /**
   * @return the email
   */
  public String getEmail() {
    return this.email;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return this.password;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.email.hashCode();
    result = (prime * result) + this.password.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "User [email=" + this.email + ", password=****]";
  }
}
