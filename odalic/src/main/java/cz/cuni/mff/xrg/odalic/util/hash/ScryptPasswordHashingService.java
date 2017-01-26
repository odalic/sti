package cz.cuni.mff.xrg.odalic.util.hash;

import com.google.common.base.Preconditions;
import com.lambdaworks.crypto.SCryptUtil;

/**
 * A {@link PasswordHashingService} implementation using a pure Java Scrypt library underneath.
 * 
 * @author Václav Brodec
 *
 */
public final class ScryptPasswordHashingService implements PasswordHashingService {

  public static final int SCRYPT_N_PARAMETER = 16384;
  public static final int SCRYPT_R_PARAMTER = 8;
  public static final int SCRYPT_P_PARAMETER = 1;

  private ScryptPasswordHashingService() {}

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.util.hash.PasswordHashingService#hash(java.lang.String)
   */
  @Override
  public String hash(final String password) {
    Preconditions.checkNotNull(password);
    Preconditions.checkArgument(!password.isEmpty());

    return SCryptUtil.scrypt(password, ScryptPasswordHashingService.SCRYPT_N_PARAMETER,
        ScryptPasswordHashingService.SCRYPT_R_PARAMTER,
        ScryptPasswordHashingService.SCRYPT_P_PARAMETER);
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.util.hash.PasswordHashingService#check(java.lang.String,
   * java.lang.String)
   */
  @Override
  public boolean check(String password, String hash) {
    Preconditions.checkNotNull(password);
    Preconditions.checkNotNull(hash);

    return SCryptUtil.check(password, hash);
  }
}
