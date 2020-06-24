package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml;

import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLFeedback;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLOntologyDefinition;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLPreClassification;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLException;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.Set;

/**
 * Implementation of {@link MLPreClassifier} which returns empty results.
 * This implementation should be used in case ML subsystem is turned off for a given task.
 */
public class NoMLPreClassifier implements MLPreClassifier {


    @Override
    public MLPreClassification preClassificate(Table table, MLFeedback feedback) throws MLException {
        return MLPreClassification.empty();
    }

    @Override
    public MLOntologyDefinition getMlOntologyDefinition() {
        return MLOntologyDefinition.empty();
    }
}
