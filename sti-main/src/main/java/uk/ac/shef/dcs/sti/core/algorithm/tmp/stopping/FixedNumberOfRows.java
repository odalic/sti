package uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping;

import java.util.Map;

/**
 */
public class FixedNumberOfRows extends StoppingCriteria {

  private int stop_at_row_counter = 0;
  private int current_iteration;

  public FixedNumberOfRows(final int rows) {
    this.stop_at_row_counter = rows;
  }

  @Override
  public boolean stop(final Map<Object, Double> state, final int max) {
    this.current_iteration++;

    if (this.current_iteration < this.stop_at_row_counter) {
      return false;
    }

    return true;
  }
}
