package cz.cuni.mff.xrg.odalic.tasks.postprocessing;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * <p>
 * Post-processors are provided with the result of a task and are free to modify them, even in
 * chains, before the final result is presented to the users.
 * </p>
 * 
 * <p>
 * Post-processors have access not only to the result itself, but also the associated input, any
 * previous feedback provided by the users and the name of the primary base of the task.
 * </p>
 * 
 * @author Václav Brodec
 *
 */
public interface PostProcessor {

  /**
   * Alters the input result.
   * 
   * @param input task file input
   * @param result task result
   * @param feedback feedback on the task provided by the user so far
   * @param primaryBaseName name of the preferred knowledge base used in the post-processed task
   * @return altered result
   */
  Result process(Input input, Result result, Feedback feedback, String primaryBaseName);

}
