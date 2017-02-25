package cz.cuni.mff.xrg.odalic.users;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.UserAdapter;

/**
 * Application user.
 *
 * @author Václav Brodec
 *
 */
@Immutable
@XmlJavaTypeAdapter(UserAdapter.class)
public final class User implements Serializable, Comparable<User> {

  private static final long serialVersionUID = -174412970524757408L;

  private static void checkEmailAddressFormat(final String email) {
    try {
      new InternetAddress(email).validate();
    } catch (final AddressException e) {
      throw new IllegalArgumentException("Illegal format of the e-mail address!", e);
    }
  }

  private final String email;

  private final String passwordHash;

  private final Role role;

  /**
   * Creates user representation.
   *
   * @param email user's email serving also as the login
   * @param passwordHash user's chosen password hash
   * @param role user's role
   */
  public User(final String email, final String passwordHash, final Role role) {
    Preconditions.checkNotNull(email);
    Preconditions.checkNotNull(passwordHash);
    Preconditions.checkNotNull(role);

    Preconditions.checkArgument(!email.isEmpty(), "The provided e-mail address is empty!");
    checkEmailAddressFormat(email);

    Preconditions.checkArgument(!passwordHash.isEmpty(), "The provided password hash is empty!");

    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
  }

  @Override
  public int compareTo(final User other) {
    final int emailComparison = this.email.compareTo(other.email);
    if (emailComparison != 0) {
      return emailComparison;
    }

    final int passwordHashComparison = this.passwordHash.compareTo(other.passwordHash);
    if (passwordHashComparison != 0) {
      return passwordHashComparison;
    }

    return this.role.compareTo(other.role);
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
    final User other = (User) object;
    if (!this.email.equals(other.email)) {
      return false;
    }
    if (!this.passwordHash.equals(other.passwordHash)) {
      return false;
    }
    if (this.role != other.role) {
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
  public String getPasswordHash() {
    return this.passwordHash;
  }

  /**
   * @return the role
   */
  public Role getRole() {
    return this.role;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.email.hashCode();
    result = (prime * result) + this.passwordHash.hashCode();
    result = (prime * result) + this.role.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "User [email=" + this.email + ", passwordHash=" + this.passwordHash + ", role="
        + this.role + "]";
  }
}
