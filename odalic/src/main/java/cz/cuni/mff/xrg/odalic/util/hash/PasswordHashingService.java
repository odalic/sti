package cz.cuni.mff.xrg.odalic.util.hash;

/**
 * Password hashing service.
 *
 * @author Václav Brodec
 *
 */
public interface PasswordHashingService {
  /**
   * Hashes the password.
   *
   * @param password nonempty text
   * @return hash
   */
  String hash(String password);

  /**
   * Checks whether the hash conforms to the password.
   *
   * @param password password
   * @param hash hash
   * @return true if the hash conforms to the password according to the implementing algorithm
   */
  boolean check(String password, String hash);
}
