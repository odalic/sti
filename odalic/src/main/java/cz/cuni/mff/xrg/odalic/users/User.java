package cz.cuni.mff.xrg.odalic.users;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.UserAdapter;

@Immutable
@XmlJavaTypeAdapter(UserAdapter.class)
public final class User implements Comparable<User> {

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

    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
  }

  /**
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @return the password
   */
  public String getPasswordHash() {
    return passwordHash;
  }

  /**
   * @return the role
   */
  public Role getRole() {
    return role;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + email.hashCode();
    result = prime * result + passwordHash.hashCode();
    result = prime * result + role.hashCode();
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    if (!email.equals(other.email)) {
      return false;
    }
    if (!passwordHash.equals(other.passwordHash)) {
      return false;
    }
    if (role != other.role) {
      return false;
    }
    return true;
  }


  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "User [email=" + email + ", passwordHash=" + passwordHash + ", role=" + role + "]";
  }
}
