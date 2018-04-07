package cz.cuni.mff.xrg.odalic.tasks.postprocessing;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Collection;
import java.util.List;
import org.apache.jena.ext.com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;
import com.google.common.base.Splitter;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.ExtraRelatablePostProcessor;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.InputConverter;

@Component
public final class DefaultPostProcessorFactory implements PostProcessorFactory {

  public static final String POST_PROCESSING_ENABLED_KEY = "eu.odalic.postProcessingEnabled";

  public static final String POST_PROCESSORS_LIST_KEY = "eu.odalic.postProcessorsList";

  public static final String POST_PROCESSORS_LIST_SEPARATOR = ";";

  public static final String EXTRA_RELATABLE_POST_PROCESSOR_NAME = "ExtraRelatable";

  private final InputConverter inputConverter;
  
  private final PrefixMappingService prefixMappingService;
  
  public DefaultPostProcessorFactory(final InputConverter inputConverter,
      final PrefixMappingService prefixMappingService) {
    checkNotNull(inputConverter);
    checkNotNull(prefixMappingService);
    
    this.inputConverter = inputConverter;
    this.prefixMappingService = prefixMappingService;
  }

  @Override
  public List<PostProcessor> getPostProcessors(final Collection<? extends KnowledgeBase> usedBases) {
    return usedBases.stream().filter(base -> {
        final boolean postProcessingEnabledByType = base.getAdvancedType().isPostProcessing();
        
        final String postProcessingEnabledValue = base.getAdvancedProperties().get(POST_PROCESSING_ENABLED_KEY);
        if (postProcessingEnabledValue == null) {
          return postProcessingEnabledByType; 
        } else {
          return Boolean.parseBoolean(postProcessingEnabledValue);
        }
      }).map(base -> {
        final String postProcessorsListValue = base.getAdvancedProperties().get(POST_PROCESSORS_LIST_KEY);
        if (postProcessorsListValue == null) {
          throw new IllegalArgumentException("Missing post-processors list!");
        }
        
        return Splitter.on(POST_PROCESSORS_LIST_SEPARATOR).splitToList(postProcessorsListValue).stream()
          .map(postProcessorName -> {
            if (!postProcessorName.equals(EXTRA_RELATABLE_POST_PROCESSOR_NAME)) {
              return null;
            }
            
            return new ExtraRelatablePostProcessor(inputConverter, prefixMappingService, base.getOwner().getEmail(), base.getName(), base.getAdvancedProperties());
         });
      })
      .flatMap(basePostProcessorsListStream -> basePostProcessorsListStream)
      .filter(postProcessor -> postProcessor != null)
     .collect(ImmutableList.toImmutableList());
  }
}
