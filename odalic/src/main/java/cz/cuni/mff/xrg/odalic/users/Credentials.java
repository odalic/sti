package cz.cuni.mff.xrg.odalic.users;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.CredentialsAdapter;

/**
 * User credentials.
 * 
 * @author Václav Brodec
 *
 */
@XmlJavaTypeAdapter(CredentialsAdapter.class)
@Immutable
public final class Credentials {

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
    
    this.email = email;
    this.password = password;
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
  public String getPassword() {
    return password;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + email.hashCode();
    result = prime * result + password.hashCode();
    return result;
  }

  /* (non-Javadoc)
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
    final Credentials other = (Credentials) object;
    if (!email.equals(other.email)) {
      return false;
    }
    if (!password.equals(other.password)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "User [email=" + email + ", password=****]";
  }
}
