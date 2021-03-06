package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.users.Credentials;

@XmlRootElement(name = "credentials")
public final class CredentialsValue {

  private String email;

  private String password;

  public CredentialsValue() {}

  public CredentialsValue(final Credentials adaptee) {
    this.email = adaptee.getEmail();
    this.password = adaptee.getPassword();
  }

  /**
   * @return the email
   */
  @XmlElement
  @Nullable
  public String getEmail() {
    return this.email;
  }

  /**
   * @return the password
   */
  @XmlElement
  @Nullable
  public String getPassword() {
    return this.password;
  }

  /**
   * @param email the email to set
   */
  public void setEmail(final String email) {
    Preconditions.checkNotNull(email, "The email cannot be null!");

    this.email = email;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(final String password) {
    Preconditions.checkNotNull(password, "The password cannot be null!");

    this.password = password;
  }

  @Override
  public String toString() {
    return "CredentialsValue [email=" + this.email + ", password=****]";
  }
}
