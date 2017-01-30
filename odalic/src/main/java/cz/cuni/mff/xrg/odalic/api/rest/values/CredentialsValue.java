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
    return email;
  }

  /**
   * @param email the email to set
   */
  public void setEmail(String email) {
    Preconditions.checkNotNull(email);
    
    this.email = email;
  }

  /**
   * @return the password
   */
  @XmlElement
  @Nullable
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    Preconditions.checkNotNull(password);
    
    this.password = password;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CredentialsValue [email=" + email + ", password=****]";
  }
}
