package cz.cuni.mff.xrg.odalic.tasks.postprocessing;

import static com.google.common.base.Preconditions.checkNotNull;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import org.apache.jena.ext.com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;
import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.annotations.prefixes.PrefixMappingService;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.ExtraRelatablePostProcessor;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.InputConverter;

@Component
public final class DefaultPostProcessorFactory implements PostProcessorFactory {

  public static final String LEARN_ANNOTATED_KEY = "learnAnnotated";

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
    return usedBases.stream().filter(base -> base.getAdvancedType().isPostProcessing()).map(base -> {
      try {
        return new ExtraRelatablePostProcessor(inputConverter, prefixMappingService, base.getEndpoint().toURI(), base.getName(), base.getLanguageTag(), base.getOwner().getEmail(), base.getAdvancedProperties());
      } catch (final URISyntaxException e) {
        throw new IllegalArgumentException("Invalid endpoint URL!", e);
      }
     }).collect(ImmutableList.toImmutableList());
  }
}
