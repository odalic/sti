package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable;

import java.net.URI;
import java.util.Map;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.ParsedTableValue;

public interface InputConverter {
  ParsedTableValue convert(Input input, String languageTag, String author, Map<Integer, URI> declaredContextClasses, Map<Integer, URI> declaredContextProperties, Map<Integer, URI> collectedContextClases, Map<Integer, URI> collectedContextProperties);
}
