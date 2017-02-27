package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

/**
 * Encapsulation of old and new password for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "password-change")
public final class PasswordChangeValue {

  private String oldPassword;
  private String newPassword;

  public PasswordChangeValue() {}

  /**
   * @return the new password
   */
  @XmlElement
  @Nullable
  public String getNewPassword() {
    return this.newPassword;
  }

  /**
   * @return the old password
   */
  @XmlElement
  @Nullable
  public String getOldPassword() {
    return this.oldPassword;
  }

  /**
   * @param newPassword the new password to set
   */
  public void setNewPassword(final String newPassword) {
    Preconditions.checkNotNull(newPassword);

    this.newPassword = newPassword;
  }

  /**
   * @param oldPassword the old password to set
   */
  public void setOldPassword(final String oldPassword) {
    Preconditions.checkNotNull(oldPassword);

    this.oldPassword = oldPassword;
  }

  @Override
  public String toString() {
    return "PasswordChangeValue [oldPassword=****, newPassword=****]";
  }
}
