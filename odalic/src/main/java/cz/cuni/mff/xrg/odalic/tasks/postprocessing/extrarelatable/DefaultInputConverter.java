package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable;

import static com.google.common.base.Preconditions.checkNotNull;
import org.springframework.stereotype.Component;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.MetadataValue;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.ParsedTableValue;

@Component
public class DefaultInputConverter implements InputConverter {

  @Override
  public ParsedTableValue convert(final Input input, final String languageTag, final String author) {
    checkNotNull(input);
    
    final ParsedTableValue result = new ParsedTableValue();
    
    final MetadataValue metadata = new MetadataValue();
    
    metadata.setLanguageTag(languageTag);
    metadata.setTitle(input.identifier());
    metadata.setAuthor(author);
    
    result.setHeaders(input.headers());
    result.setRows(input.rows());
    result.setMetadata(metadata);
    
    return result;
  }

}
