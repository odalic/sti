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
    return this.email;
  }

  /**
   * @return the role
   */
  @XmlElement
  @Nullable
  public Role getRole() {
    return this.role;
  }

  /**
   * @param email the email to set
   */
  public void setEmail(final String email) {
    Preconditions.checkNotNull(email, "The email cannot be null!");

    this.email = email;
  }

  /**
   * @param role the role to set
   */
  public void setRole(final Role role) {
    Preconditions.checkNotNull(role, "The role cannot be null!");

    this.role = role;
  }

  @Override
  public String toString() {
    return "UserValue [email=" + this.email + ", role=" + this.role + "]";
  }
}
