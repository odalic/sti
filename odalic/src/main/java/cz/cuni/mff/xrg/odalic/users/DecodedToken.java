/**
 * 
 */
package cz.cuni.mff.xrg.odalic.users;

import java.time.Instant;
import java.util.UUID;

import com.google.common.base.Preconditions;

/**
 * Decoded {@link Token}.
 * 
 * @author Václav Brodec
 *
 */
public final class DecodedToken {

  private final UUID id;
  
  private final String issuer;
  
  private final String subject;
  
  private final Instant expiration;

  /**
   * Creates a decoded token representation.
   * 
   * @param id ID
   * @param subject subject
   * @param expiration expiration time
   */
  public DecodedToken(final UUID id, final String issuer, final String subject, final Instant expiration) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(issuer);
    Preconditions.checkNotNull(subject);
    Preconditions.checkNotNull(expiration);
    
    this.id = id;
    this.issuer = issuer;
    this.subject = subject;
    this.expiration = expiration;
  }

  /**
   * @return the ID
   */
  public UUID getId() {
    return id;
  }

  /**
   * @return the issuer
   */
  public String getIssuer() {
    return issuer;
  }
  
  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @return the expiration time
   */
  public Instant getExpiration() {
    return expiration;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id.hashCode();
    result = prime * result + issuer.hashCode();
    result = prime * result + subject.hashCode();
    result = prime * result + expiration.hashCode();
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DecodedToken other = (DecodedToken) obj;
    if (!id.equals(other.id)) {
      return false;
    }
    if (!issuer.equals(other.issuer)) {
      return false;
    }
    if (!subject.equals(other.subject)) {
      return false;
    }
    if (!expiration.equals(other.expiration)) {
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "DecodedToken [id=" + id + ", issuer=" + issuer + ", subject=" + subject
        + ", expiration=" + expiration + "]";
  }
}
