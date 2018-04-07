package cz.cuni.mff.xrg.odalic.bases;

import java.util.Map;
import java.util.Set;

public interface AdvancedBaseTypeFactory {
  
  AdvancedBaseType createRegular(String name, Set<String> keys,
      Map<String, String> keysToDefaultValues, Map<String, String> keysToComments);
  
  AdvancedBaseType createPostProcessable(AdvancedBaseType prototype, String name,
      Set<String> postProcessingKeys, Map<String, String> postProcessingKeysToDefaultValues,
      Map<String, String> keysToComments);

}
