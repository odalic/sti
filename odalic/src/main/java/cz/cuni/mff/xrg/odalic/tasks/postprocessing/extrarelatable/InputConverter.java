package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable;

import java.util.Map;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.ParsedTableValue;

public interface InputConverter {
  ParsedTableValue convert(Input input, String languageTag, String author,
      Map<? extends Integer, ? extends Entity> declaredContextClasses, Map<? extends Integer, ? extends Entity> declaredContextProperties,
      Map<? extends Integer, ? extends Entity> collectedContextClasses,
      Map<? extends Integer, ? extends Entity> collectedContextProperties);
}
