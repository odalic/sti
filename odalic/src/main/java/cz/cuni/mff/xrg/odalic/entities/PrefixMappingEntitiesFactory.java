/**
 *
 */
package cz.cuni.mff.xrg.odalic.entities;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.Prefix;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;

/**
 * An implementation of {@link EntitiesFactory} that uses {@link PrefixMappingService} to
 * instantiate {@link Entity} instances with their correct {@link Prefix}es.
 *
 * @author Václav Brodec
 *
 */
public final class PrefixMappingEntitiesFactory implements EntitiesFactory {

  private final PrefixMappingService prefixMappingService;

  @Autowired
  public PrefixMappingEntitiesFactory(final PrefixMappingService prefixMappingService) {
    Preconditions.checkNotNull(prefixMappingService);

    this.prefixMappingService = prefixMappingService;
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.entities.EntitiesFactory#create(java.lang.String, java.lang.String)
   */
  @Override
  public Entity create(final String resourceId, final String label) {
    return Entity.of(this.prefixMappingService.getPrefix(resourceId), resourceId, label);
  }
}
