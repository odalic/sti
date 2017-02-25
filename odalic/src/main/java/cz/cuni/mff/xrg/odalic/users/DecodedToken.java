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
   * @param issuer issuer
   * @param subject subject
   * @param expiration expiration time
   */
  public DecodedToken(final UUID id, final String issuer, final String subject,
      final Instant expiration) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(issuer);
    Preconditions.checkNotNull(subject);
    Preconditions.checkNotNull(expiration);

    this.id = id;
    this.issuer = issuer;
    this.subject = subject;
    this.expiration = expiration;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DecodedToken other = (DecodedToken) obj;
    if (!this.id.equals(other.id)) {
      return false;
    }
    if (!this.issuer.equals(other.issuer)) {
      return false;
    }
    if (!this.subject.equals(other.subject)) {
      return false;
    }
    if (!this.expiration.equals(other.expiration)) {
      return false;
    }
    return true;
  }

  /**
   * @return the expiration time
   */
  public Instant getExpiration() {
    return this.expiration;
  }

  /**
   * @return the ID
   */
  public UUID getId() {
    return this.id;
  }

  /**
   * @return the issuer
   */
  public String getIssuer() {
    return this.issuer;
  }

  /**
   * @return the subject
   */
  public String getSubject() {
    return this.subject;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.id.hashCode();
    result = (prime * result) + this.issuer.hashCode();
    result = (prime * result) + this.subject.hashCode();
    result = (prime * result) + this.expiration.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "DecodedToken [id=" + this.id + ", issuer=" + this.issuer + ", subject=" + this.subject
        + ", expiration=" + this.expiration + "]";
  }
}
