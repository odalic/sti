package uk.ac.shef.dcs.sti.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Created by zqz on 29/04/2015.
 */
public class SubsetGenerator {

  public static List<String> generateSubsets(final Set<Integer> words) {
    final Set<Set<Integer>> subsets = Sets.powerSet(words);
    final List<String> result = new ArrayList<>();
    for (final Set<Integer> sub : subsets) {
      final List<Integer> ordered = new ArrayList<>(sub);
      Collections.sort(ordered);
      String string = "";
      for (final Integer a : ordered) {
        string += a + " ";
      }
      result.add(string.trim());
    }
    result.remove("");
    return result;
  }
}
