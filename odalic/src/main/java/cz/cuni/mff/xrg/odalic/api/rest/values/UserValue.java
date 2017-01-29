package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.users.Role;
import cz.cuni.mff.xrg.odalic.users.User;

@XmlRootElement(name = "user")
public final class UserValue {

  private String email;
  
  private Role role;

  public UserValue() {}
  
  public UserValue(final User adaptee) {
    this.email = adaptee.getEmail();
    this.role = adaptee.getRole();
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
   * @return the role
   */
  @XmlElement
  @Nullable
  public Role getRole() {
    return role;
  }

  /**
   * @param role the role to set
   */
  public void setRole(Role role) {
    Preconditions.checkNotNull(role);
    
    this.role = role;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "UserValue [email=" + email + ", role=" + role + "]";
  }
}
