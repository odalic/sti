package cz.cuni.mff.xrg.odalic.bases;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

public class DefaultAdvancedBaseTypeFactory implements AdvancedBaseTypeFactory {

  @Override
  public AdvancedBaseType createRegular(String name, Set<String> keys,
      Map<String, String> keysToDefaultValues,
      Map<String, String> keysToComments) {
    return new AdvancedBaseType(name, keys, keysToDefaultValues, keysToComments);
  }

  @Override
  public AdvancedBaseType createPostProcessable(AdvancedBaseType prototype, String name,
      Set<String> postProcessingKeys,
      Map<String, String> postProcessingKeysToDefaultValues,
      Map<String, String> postProcessingKeysToComments) {
    return new AdvancedBaseType(name, mergeIfNotPresent(prototype.getKeys(), postProcessingKeys), merge(prototype.getKeysToDefaultValues(), postProcessingKeysToDefaultValues), merge(prototype.getKeysToComments(), postProcessingKeysToComments));
  }

  private static Map<String, String> merge(Map<String, String> first,
      Map<String, String> second) {
    return ImmutableMap.<String, String>builder().putAll(first).putAll(second).build();
  }

  private static Set<String> mergeIfNotPresent(Set<String> original, Set<String> added) {
    final Sets.SetView<String> result = Sets.union(original, added);
    checkArgument(result.size() == original.size() + added.size(), "Cannot create post-processable version. There are conflicting advanced property keys: " + Sets.intersection(original, added));
    
    return result.immutableCopy();
  }
}
