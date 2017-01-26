package cz.cuni.mff.xrg.odalic.users;

import java.time.Instant;

public interface TokenService {
  Token create(final String subject, final Instant expiration);
}
