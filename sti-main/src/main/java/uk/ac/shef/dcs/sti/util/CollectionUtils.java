package uk.ac.shef.dcs.sti.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk) Date: 07/05/13 Time: 16:38
 */
public class CollectionUtils {

  /**
   * how much of b does a cover
   *
   * @param a
   * @param b
   * @return
   */
  public static double computeCoverage(final Collection<String> a, final Collection<String> b) {
    final List<String> c = new ArrayList<>(b);
    c.retainAll(a);
    if (c.size() == 0) {
      return 0.0;
    }
    final double score = (double) c.size() / b.size();
    return score;
  }

  // entity //context

  public static double computeDice(final Collection<String> c1, final Collection<String> c2) {
    final Set<String> intersection = new HashSet<>(c1);
    intersection.retainAll(c1);
    intersection.retainAll(c2);

    if (intersection.size() == 0) {
      return 0.0;
    }
    final double score = (2 * (double) intersection.size()) / (c1.size() + c2.size());
    return score;

  }

  public static double computeFrequencyWeightedDice(final Collection<String> c1,
      final Collection<String> c2) {
    final List<String> union = new ArrayList<>();
    union.addAll(c1);
    union.addAll(c2);

    final List<String> intersection = new ArrayList<>(union);
    intersection.retainAll(c1);
    intersection.retainAll(c2);

    if (intersection.size() == 0) {
      return 0.0;
    }
    final double score = (2 * (double) intersection.size()) / (c1.size() + c2.size());
    return score;

  }

}
