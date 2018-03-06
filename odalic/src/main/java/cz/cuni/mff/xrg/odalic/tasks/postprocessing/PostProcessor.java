package cz.cuni.mff.xrg.odalic.tasks.postprocessing;

import cz.cuni.mff.xrg.odalic.feedbacks.Feedback;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

public interface PostProcessor {

	Result process(Input input, Result result, Feedback feedback, String primaryBaseName);

}
