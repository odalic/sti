package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.ParsedTableValue;

public interface InputConverter {
  ParsedTableValue convert(Input input, String languageTag, String author);
}
