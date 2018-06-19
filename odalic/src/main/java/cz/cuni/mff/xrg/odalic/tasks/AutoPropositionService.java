package cz.cuni.mff.xrg.odalic.tasks;

import cz.cuni.mff.xrg.odalic.bases.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * Service for handling the Auto Proposition of new resources.
 *
 * @author Rastislav Kadlecek
 */
public interface AutoPropositionService {

    /**
     * Automatically propose all new resources for given task, according to the
     * task result.
     * @param task
     * @param taskInput
     * @param result
     * @param primaryKnowledgeBase
     */
    void autoProposeNewResources(Task task, Input taskInput, Result result, KnowledgeBase primaryKnowledgeBase);
}
