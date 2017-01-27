/**
 * 
 */
package cz.cuni.mff.xrg.odalic.users;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * A {@link RandomService} implementation using {@link SecureRandom} as the source randomness.
 * 
 * @author Václav Brodec
 *
 */
public final class SecureRandomService implements RandomService {

  private static final int RANDOM_BITS_COUNT = 130;
  private static final int ALPHA_NUMERIC_RADIX = 32;

  private final SecureRandom random;

  public SecureRandomService() {
    this.random = new SecureRandom();
  }

  public SecureRandomService(final byte[] seed) {
    this.random = new SecureRandom(seed.clone());
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.users.RandomService#getRandomString()
   */
  @Override
  public String getRandomString() {
    return new BigInteger(RANDOM_BITS_COUNT, this.random).toString(ALPHA_NUMERIC_RADIX);
  }

}
