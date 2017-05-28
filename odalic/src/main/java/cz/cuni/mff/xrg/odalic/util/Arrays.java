package cz.cuni.mff.xrg.odalic.util;

import java.lang.reflect.Array;
import java.util.function.BiFunction;

import com.google.common.base.Preconditions;

/**
 * Utility class for -- you guessed it -- working with arrays.
 *
 * @author Václav Brodec
 *
 */
public final class Arrays {

  /**
   * <p>
   * Checks for {@code null} every (array) element of the array and elements of the element array.
   * </p>
   *
   * <p>
   * Please note that the method does not expect the elements of the nested array to be also arrays
   * (in other words: it does not go beyond the second dimension).
   * </p>
   *
   * @param arrayOfArrays array of arrays
   * @return true, if any element up to second dimension is null, false otherwise
   * @throws NullPointerException If the arrayOfArrays itself is {@code null}
   */
  public static boolean containsNull(final Object[][] arrayOfArrays) throws NullPointerException {
    for (final Object[] arrayOfArray : arrayOfArrays) {
      if (arrayOfArray == null) {
        return true;
      }

      for (int j = 0; j < arrayOfArray.length; j++) {
        if (arrayOfArray[j] == null) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Creates a deep copy of a matrix (up to second dimension).
   *
   * @param type type of elements
   * @param matrix the matrix
   * @return deep copy of the matrix
   * @throws IllegalArgumentException if either of dimensions of the matrix is zero, if the type of
   *         elements is Void.TYPE, or if the number of any dimension of the matrix exceeds 255.
   *
   * @param <T> type of elements
   */
  public static <T> T[][] deepCopy(final Class<T> type, final T[][] matrix)
      throws IllegalArgumentException {
    final int rowsCount = matrix.length;
    final int columnsCount = (rowsCount > 0) ? (matrix[0].length) : 0;

    @SuppressWarnings("unchecked")
    final T[][] copy = (T[][]) Array.newInstance(type, rowsCount, columnsCount);
    for (int i = 0; i < rowsCount; i++) {
      copy[i] = matrix[i].clone();
    }

    return copy;
  }

  /**
   * <p>
   * Checks whether the array of arrays is a matrix (all the rows has the same number of cells).
   * </p>
   *
   * <p>
   * Please note that the method does not expect the elements of the nested array to be also arrays
   * (in other words: it does not go beyond the second dimension).
   * </p>
   *
   * <p>
   * Also it accepts empty array of arrays and array of zero-length arrays as a valid matrix.
   * </p>
   *
   * @param arrayOfArrays array of arrays
   * @return true, if the array of arrays is a matrix (up to second dimension)
   */
  public static boolean isMatrix(final Object[][] arrayOfArrays) {
    if (arrayOfArrays.length == 0) {
      return true;
    }

    final int columnsCount = arrayOfArrays[0].length;
    for (int i = 1; i < arrayOfArrays.length; i++) {
      if (arrayOfArrays[i].length != columnsCount) {
        return false;
      }
    }

    return true;
  }

  /**
   * Executes functional zip over a two matrices, but modifies the first one in the process.
   *
   * @param modified the matrix whose elements serve as the first argument of the zip function and
   *        then are replaced by its result
   * @param added matrix whose elements serve as the second argument of the zip function
   * @param zipFunction zip function
   * @throws IllegalArgumentException If the modified and added are not matrices or have different
   *         dimensions
   *
   * @param <T> type of elements in modified
   * @param <U> type of elements in added
   */
  public static <T, U> void zipMatrixWith(final T[][] modified, final U[][] added,
      final BiFunction<T, U, T> zipFunction) throws IllegalArgumentException {
    Preconditions.checkArgument(modified.length != 0, "The modified array cannot be empty!");
    Preconditions.checkArgument(added.length == modified.length, "Both matrices must have the same number of rows!");
    Preconditions.checkArgument(added[0].length == modified[0].length, "Both matrices must have the same number of columns!");
    Preconditions.checkArgument(isMatrix(modified), "The modified array is not a matrix!");
    Preconditions.checkArgument(isMatrix(added), "The added array is not a matrix!");

    for (int i = 0; i < modified.length; i++) {
      for (int j = 0; j < modified[0].length; j++) {
        modified[i][j] = zipFunction.apply(modified[i][j], added[i][j]);
      }
    }
  }

  /**
   * We want to keep this class uninstantiable, so no visible constructor is available.
   */
  private Arrays() {}

}
