package uk.ac.shef.dcs.sti.core.extension.annotations;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * Score value for annotation.
 *
 * @author Václav Brodec
 *
 */
@Immutable
public final class Score implements Comparable<Score>, Serializable {

  private static final long serialVersionUID = -901650058091668104L;

  private final double value;

  public Score(final double value) {
    Preconditions.checkArgument(value >= 0);

    this.value = value;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final Score o) {
    return Double.compare(this.value, o.value);
  }

  /**
   * Compares for equality (only other Score with the same values passes).
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Score other = (Score) obj;
    if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(other.value)) {
      return false;
    }
    return true;
  }

  /**
   * @return the value
   */
  public double getValue() {
    return this.value;
  }

  /**
   * Computes hash code based on the value.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(this.value);
    result = (prime * result) + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Score [value=" + this.value + "]";
  }

}
