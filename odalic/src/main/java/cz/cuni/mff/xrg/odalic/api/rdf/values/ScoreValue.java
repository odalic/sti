package cz.cuni.mff.xrg.odalic.api.rdf.values;

import java.io.Serializable;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Score;

/**
 * Domain class {@link Score} adapted for RDF serialization.
 *
 * @author Václav Brodec
 *
 */
public final class ScoreValue implements Serializable {

  private static final long serialVersionUID = -901650058091668104L;

  private double value;

  public ScoreValue() {
    this.value = Double.MIN_VALUE;
  }

  public ScoreValue(final Score adaptee) {
    this.value = adaptee.getValue();
  }

  /**
   * @return the value (negative when not set)
   */
  public double getValue() {
    return this.value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final double value) {
    Preconditions.checkArgument(value >= 0, "Score value must be nonnegative!");

    this.value = value;
  }

  public Score toScore() {
    return new Score(this.value);
  }

  @Override
  public String toString() {
    return "ScoreValue [value=" + this.value + "]";
  }
}
