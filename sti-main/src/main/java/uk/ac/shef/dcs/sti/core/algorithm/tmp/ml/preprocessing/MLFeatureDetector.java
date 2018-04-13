package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing;

import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing.InputValue;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing.InputWithFeatures;

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
