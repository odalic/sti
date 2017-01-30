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
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    return prime * token.hashCode();
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
    final Token other = (Token) object;
    if (!token.equals(other.token)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Token [token=" + token + "]";
  }
}
