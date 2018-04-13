package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable;

import static com.google.common.base.Preconditions.checkNotNull;
import java.net.URI;
import java.util.Map;
import org.springframework.stereotype.Component;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.MetadataValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.ParsedTableValue;

@Component
public class DefaultInputConverter implements InputConverter {



  @Override
  public ParsedTableValue convert(Input input, String languageTag, String author,
      Map<Integer, URI> declaredContextClasses, Map<Integer, URI> declaredContextProperties,
      Map<Integer, URI> collectedContextClasses, Map<Integer, URI> collectedContextProperties) {
    checkNotNull(input);
    
    final ParsedTableValue result = new ParsedTableValue();
    
    final MetadataValue metadata = new MetadataValue();
    
    metadata.setLanguageTag(languageTag);
    metadata.setTitle(input.identifier());
    metadata.setAuthor(author);
    metadata.setDeclaredClassUris(declaredContextClasses);
    metadata.setDeclaredPropertyUris(declaredContextProperties);
    metadata.setCollectedClassUris(collectedContextClasses);
    metadata.setCollectedPropertyUris(collectedContextProperties);
    
    result.setHeaders(input.headers());
    result.setRows(input.rows());
    result.setMetadata(metadata);
    
    return result;
  }

}
