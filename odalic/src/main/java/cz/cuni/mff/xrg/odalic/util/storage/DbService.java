package cz.cuni.mff.xrg.odalic.util.storage;

import org.mapdb.DB;

/**
 * A {@link DB} instance provider.
 * 
 * @author Václav Brodec
 *
 */
public interface DbService {
  /**
   * Provides a database.
   * 
   * @return a {@link DB} instance
   */
  DB get();
}
