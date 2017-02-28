package uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping;

import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MUST BE RE-INSTANTIATED FOR EVERY INTERPRETATION TASK, because class variable "current iteration"
 * and "previous entropy" does not reset
 */
public class IInf extends StoppingCriteria {

  private static final Logger LOG = LoggerFactory.getLogger(IInf.class.getName());
  private final double minimum_state_score_sum;
  private double previous_iteration_entropy;
  private final int minimum_iterations;
  private int current_iteration;

  private double convergence_threshold = 0.01;

  // minimum #
  public IInf(final double minimum_state_score_sum, final int minimum_iterations,
      final double convergence_threshold) {
    this.minimum_state_score_sum = minimum_state_score_sum;
    this.minimum_iterations = minimum_iterations;
    this.current_iteration = 0;
    this.convergence_threshold = convergence_threshold;
  }

  @Override
  public boolean stop(final Map<Object, Double> state, final int max) {
    this.current_iteration++;

    // evaluate the state by calculating entropy
    double sum = 0.0;
    for (final Double d : new HashSet<>(state.values())) {
      sum += d;
    }
    double entropy = 0.0;
    if (state.size() > 1) {
      for (final Map.Entry<Object, Double> e : state.entrySet()) {
        if (e.getValue() == 0) {
          continue;
        }
        final double p_a = e.getValue() / sum;
        final double log_p_a = Math.log(p_a);

        entropy = entropy + (0 - (p_a * log_p_a));
      }
    }
    // is it converged?
    boolean has_converged = false;

    if (this.previous_iteration_entropy != 0) {
      has_converged =
          Math.abs(entropy - this.previous_iteration_entropy) < this.convergence_threshold;
    }
    this.previous_iteration_entropy = entropy;

    if (this.current_iteration < this.minimum_iterations) {
      return false;
    }
    if (sum < this.minimum_state_score_sum) {
      return false;
    }

    if (has_converged) {
      LOG.debug("\tConvergence iteration=" + this.current_iteration + ", potential max=" + max
          + ", savings=" + ((double) (max - this.current_iteration) / max));
      /*
       * if(current_iteration>20) System.out.println();
       */
      // previous_iteration_entropy=0.0;//reset
      // current_iteration=0;
      return true;
    }

    if (this.current_iteration == max) {
      LOG.debug("\t(negative)Convergence iteration=" + this.current_iteration + ", potential max="
          + max + ", savings=" + ((double) (max - this.current_iteration) / max));
    }

    return false;
  }
}
