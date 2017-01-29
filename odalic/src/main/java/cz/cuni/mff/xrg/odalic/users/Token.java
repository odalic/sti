package cz.cuni.mff.xrg.odalic.users;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.TokenAdapter;

/**
 * Authorization and authentication token string container.
 * 
 * @author Václav Brodec
 *
 */
@XmlJavaTypeAdapter(TokenAdapter.class)
@Immutable
public final class Token {

  private String token;
  
  public Token() {}
  
  public Token(final String token) {
    Preconditions.checkNotNull(token);
    
    this.token = token;
  }

  /**
   * @return the token
   */
  @XmlElement
  @Nullable
  public String getToken() {
    return token;
  }

  /**
   * @param token the token to set
   */
  public void setToken(String token) {
    Preconditions.checkNotNull(token);
    
    this.token = token;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Token [token=" + token + "]";
  }
}
