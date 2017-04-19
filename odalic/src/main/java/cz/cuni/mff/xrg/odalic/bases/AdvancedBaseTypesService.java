package cz.cuni.mff.xrg.odalic.bases;

import java.util.SortedSet;

import javax.annotation.Nullable;

/**
 * Provides available advanced base types.
 *
 * @author Václav Brodec
 *
 */
public interface AdvancedBaseTypesService {

  /**
   * @return the advanced base types
   */
  SortedSet<AdvancedBaseType> getTypes();
  
  /**
   * @param name advanced base type name
   * @return the advanced base type
   */
  AdvancedBaseType getType(String name) throws IllegalArgumentException;

  /**
   * @param name advanced base type name
   * @return the advanced base type
   */
  @Nullable
  AdvancedBaseType verifyTypeExistenceByName(String name);
}
