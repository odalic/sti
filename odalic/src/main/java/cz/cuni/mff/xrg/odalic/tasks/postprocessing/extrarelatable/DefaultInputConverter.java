package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable;

import static com.google.common.base.Preconditions.checkNotNull;
import java.net.URI;
import java.util.Map;
import java.util.NavigableSet;
import java.util.stream.Collectors;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSortedSet;
import org.springframework.stereotype.Component;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.DeclaredEntityValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.MetadataValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.ParsedTableValue;

@Component
public class DefaultInputConverter implements InputConverter {

  private static final String PROPERTY_LABEL_EDNING_MARK_PATTERN = " \\(ER\\)$";

  @Override
  public ParsedTableValue convert(Input input, String languageTag, String author,
      Map<? extends Integer, ? extends Entity> declaredContextClasses, Map<? extends Integer, ? extends Entity> declaredContextProperties,
      Map<? extends Integer, ? extends Entity> collectedContextClasses, Map<? extends Integer, ? extends Entity> collectedContextProperties) {
    checkNotNull(input);
    
    final ParsedTableValue result = new ParsedTableValue();
    
    final MetadataValue metadata = new MetadataValue();
    
    metadata.setLanguageTag(languageTag);
    metadata.setTitle(input.identifier());
    metadata.setAuthor(author);
    metadata.setDeclaredClasses(toDeclaredEntities(declaredContextClasses));
    metadata.setDeclaredProperties(toDeclaredEntities(declaredContextProperties));
    metadata.setCollectedClasses(toDeclaredEntities(collectedContextClasses));
    metadata.setCollectedProperties(toDeclaredEntities(collectedContextProperties));
    
    result.setHeaders(input.headers());
    result.setRows(input.rows());
    result.setMetadata(metadata);
    
    return result;
  }

  private static Map<Integer, DeclaredEntityValue> toDeclaredEntities(
      Map<? extends Integer, ? extends Entity> entities) {
    return entities.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new DeclaredEntityValue(URI.create(e.getValue().getResource()), toLabels(e.getValue().getLabel()))));
  }

  private static NavigableSet<String> toLabels(final String jointLabel) {
    final String demarkedLabel = jointLabel.replaceFirst(PROPERTY_LABEL_EDNING_MARK_PATTERN, "");
    
    return ImmutableSortedSet.copyOf(Splitter.on(ExtraRelatablePostProcessor.LABELS_DELIMITER).omitEmptyStrings().splitToList(demarkedLabel));
  }
}
