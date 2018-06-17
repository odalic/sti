package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml;

import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLFeedback;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLOntologyDefinition;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config.MLPreClassification;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLException;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.Set;

public interface MLPreClassifier {

    MLPreClassification preClassificate(Table table, MLFeedback feedback) throws MLException;

    MLOntologyDefinition getMlOntologyDefinition();

}
