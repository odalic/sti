package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml;

import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.InputValue;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.InputWithFeatures;

public interface MLFeatureDetector {

    /**
     * Detects the features from given inputValue.
     * @param inputValue
     * @return
     */
    InputWithFeatures detectFeatures(InputValue inputValue);

    /**
     * Detects the features from given array of inputValue-s.
     * @param inputValues
     * @return
     */
    InputWithFeatures[] detectFeatures(InputValue[] inputValues);

}
