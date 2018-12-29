/**
 * 
 */
package cz.cuni.mff.xrg.odalic.entities.postprocessing;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.entities.EntitiesFactory;
import cz.cuni.mff.xrg.odalic.entities.EntitiesService;
import cz.cuni.mff.xrg.odalic.entities.EntitiesServicesFactory;
import cz.cuni.mff.xrg.odalic.entities.postprocessing.extrarelatable.ExtraRelatableEntitiesService;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.DefaultPostProcessorFactory;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.PostProcessor;

/**
 * <p>
 * This implementation of {@link EntitiesServicesFactory} is in almost all ways analogous to the way
 * {@link DefaultPostProcessorFactory} is implemented, with the difference being that the result are
 * {@link EntitiesService}s instead of {@link PostProcessor}s.
 * </p>
 * 
 * <p>
 * The provided {@link EntitiesService}s are meant to extend the support for entity proposal and
 * search to other data bases, usually used by the corresponding post-processors.
 * </p>
 * 
 * @author Václav Brodec
 *
 */
@Component
public class PostProcessingEntitiesServicesFactory implements EntitiesServicesFactory {

  private final EntitiesFactory entitiesFactory;

  @Autowired
  public PostProcessingEntitiesServicesFactory(final EntitiesFactory entitiesFactory) {
    checkNotNull(entitiesFactory);

    this.entitiesFactory = entitiesFactory;
  }

  @Override
  public List<EntitiesService> getEntitiesServices(final KnowledgeBase base) {
    final boolean postProcessingEnabledByType = base.getAdvancedType().isPostProcessing();

    final boolean postProcessingEnabled;

    final String postProcessingEnabledValue =
        base.getAdvancedProperties().get(DefaultPostProcessorFactory.POST_PROCESSING_ENABLED_KEY);
    if (postProcessingEnabledValue == null) {
      postProcessingEnabled = postProcessingEnabledByType;
    } else {
      postProcessingEnabled = Boolean.parseBoolean(postProcessingEnabledValue);
    }

    if (!postProcessingEnabled) {
      return ImmutableList.of();
    }


    final String postProcessorsListValue =
        base.getAdvancedProperties().get(DefaultPostProcessorFactory.POST_PROCESSORS_LIST_KEY);
    if (postProcessorsListValue == null) {
      throw new IllegalArgumentException("Missing post-processors list!");
    }

    return Splitter.on(DefaultPostProcessorFactory.POST_PROCESSORS_LIST_SEPARATOR)
        .splitToList(postProcessorsListValue).stream().map(postProcessorName -> {
          if (!postProcessorName
              .equals(DefaultPostProcessorFactory.EXTRA_RELATABLE_POST_PROCESSOR_NAME)) {
            return null;
          }

          return new ExtraRelatableEntitiesService(this.entitiesFactory);
        }).filter(postProcessor -> postProcessor != null).collect(ImmutableList.toImmutableList());
  }

}
