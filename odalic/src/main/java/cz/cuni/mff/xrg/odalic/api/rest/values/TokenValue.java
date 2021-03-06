package cz.cuni.mff.xrg.odalic.api.rest.values;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.users.Token;

/**
 * Domain class {@link Token} adapted for REST API.
 *
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "token")
public final class TokenValue {

  private String token;

  public TokenValue() {}

  public TokenValue(final Token adaptee) {
    this.token = adaptee.getToken();
  }

  /**
   * @return the token
   */
  @XmlElement
  @Nullable
  public String getToken() {
    return this.token;
  }

  /**
   * @param token the token to set
   */
  public void setToken(final String token) {
    Preconditions.checkNotNull(token, "The token cannot be null!");

    this.token = token;
  }

  @Override
  public String toString() {
    return "TokenValue [token=" + this.token + "]";
  }
}
