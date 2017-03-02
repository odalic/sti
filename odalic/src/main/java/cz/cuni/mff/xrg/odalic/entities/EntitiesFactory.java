/**
 *
 */
package cz.cuni.mff.xrg.odalic.entities;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;

/**
 * Factory of {@link Entity} instances.
 *
 * @author Václav Brodec
 *
 */
public interface EntitiesFactory {
  Entity create(final String resourceId, final String label);
}
