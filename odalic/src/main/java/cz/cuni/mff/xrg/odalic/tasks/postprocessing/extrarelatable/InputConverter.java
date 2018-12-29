package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable;

import java.util.Map;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.annotations.Entity;
import cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values.ParsedTableValue;

/**
 * Converts the input of Odalic to a format recognizable by ExtraRelaTable REST API.
 * 
 * @author Václav Brodec
 *
 */
public interface InputConverter {
  /**
   * Converts between Odalic fomrat and ERT format of a parsed table.
   * 
   * @param input parsed table as accepted by Odalic
   * @param languageTag language tag of the content according to BCP 47 norm
   * @param author presumed author of the table
   * @param declaredContextClasses manually curated classifications of indexed columns
   * @param declaredContextProperties manually curated relations between the subject columns and the
   *        indexed columns
   * @param collectedContextClasses automatically computed classifications of indexed columns
   * @param collectedContextProperties automatically discovered relations between the subject
   *        columns and the indexed columns
   * @return ERT-compatible parsed table
   */
  ParsedTableValue convert(Input input, String languageTag, String author,
      Map<? extends Integer, ? extends Entity> declaredContextClasses,
      Map<? extends Integer, ? extends Entity> declaredContextProperties,
      Map<? extends Integer, ? extends Entity> collectedContextClasses,
      Map<? extends Integer, ? extends Entity> collectedContextProperties);
}
