package cz.cuni.mff.xrg.odalic.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

/**
 * Utility class for -- you guessed it -- working with collections.
 *
 * @author Václav Brodec
 *
 */
public final class Lists {

  /**
   * Executes functional zip over a list and a collection, but modifies the list in the process.
   *
   * @param modified the list whose elements serve as the first argument of the zip function and
   *        then are replaced by its result
   * @param added collection whose elements serve as the second argument of the zip function
   * @param zipFunction zip function
   * @throws IllegalArgumentException If the modified and added have different number of elements
   * @throws UnsupportedOperationException if the set operation is not supported by the list
   *         iterator
   *
   * @param <T> type of elements in modified
   * @param <U> type of elements in added
   */
  public static <T, U> void zipWith(final List<T> modified, final Collection<U> added,
      final BiFunction<T, U, T> zipFunction) throws IllegalArgumentException {
    if (modified.size() != added.size()) {
      throw new IllegalArgumentException();
    }

    final ListIterator<T> listIterator = modified.listIterator();
    final Iterator<U> iterator = added.iterator();
    while (listIterator.hasNext() && iterator.hasNext()) {
      listIterator.set(zipFunction.apply(listIterator.next(), iterator.next()));
    }
  }


  /**
   * Merges two lists in such way that all the items from the first one are preserved in order and
   * all items from the second one not present in the first one are appended to them in order.
   * 
   * @param first first list
   * @param second second list
   * @return merged list
   * 
   * @param <T> type of elements
   */
  public static <T> List<T> merge(final List<T> first, final List<T> second) {
    final Set<T> firstSet = ImmutableSet.copyOf(first);
    final List<T> secondRemainder =
        second.stream().filter(e -> !firstSet.contains(e)).collect(Collectors.toList());

    final List<T> result = new ArrayList<>();
    result.addAll(first);
    result.addAll(secondRemainder);

    return result;
  }

  /**
   * We want to keep this class uninstantiable, so no visible constructor is available.
   */
  private Lists() {}
}
