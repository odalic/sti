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

  public static final int SCRYPT_CPU_COST_PARAMETER = 16384;
  public static final int SCRYPT_MEMORY_COST_PARAMTER = 8;
  public static final int SCRYPT_PARALLELIZATION_PARAMETER = 1;

  public ScryptPasswordHashingService() {}

  @Override
  public String hash(final String password) {
    Preconditions.checkNotNull(password, "The password cannot be null!");
    Preconditions.checkArgument(!password.isEmpty(), "The password cannot be empty!");

    return SCryptUtil.scrypt(password, ScryptPasswordHashingService.SCRYPT_CPU_COST_PARAMETER,
        ScryptPasswordHashingService.SCRYPT_MEMORY_COST_PARAMTER,
        ScryptPasswordHashingService.SCRYPT_PARALLELIZATION_PARAMETER);
  }

  @Override
  public boolean check(final String password, final String hash) {
    Preconditions.checkNotNull(password, "The password cannot be null!");
    Preconditions.checkNotNull(hash, "The hash cannot be null!");

    return SCryptUtil.check(password, hash);
  }
}
