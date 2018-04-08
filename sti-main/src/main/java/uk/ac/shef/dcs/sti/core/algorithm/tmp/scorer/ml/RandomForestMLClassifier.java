package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml;

import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.InputValue;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.config.MLOntologyDefinition;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.config.MLOntologyMapping;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;

import java.util.Properties;

public class RandomForestMLClassifier extends MLClassifier {

    private static final int MAX_DEPTH = 15;
    private static final int NUM_FEATURES = 3;
    private static final int NUM_BAGGING_INTERATIONS = 120;
    private static final boolean BREAK_TIES_RANDOMLY = true;

    private RandomForest classifier;

    public RandomForestMLClassifier(String homePath, Properties props, MLFeatureDetector featureDetector,
                                    InputValue[] trainingDatasetInputValues) {

        super(homePath, props, featureDetector, trainingDatasetInputValues);
        this.classifier = new RandomForest();
        this.classifier.setMaxDepth(MAX_DEPTH);
        this.classifier.setNumFeatures(NUM_FEATURES);
        this.classifier.setNumIterations(NUM_BAGGING_INTERATIONS);
        this.classifier.setBreakTiesRandomly(BREAK_TIES_RANDOMLY);
    }

    @Override
    protected Classifier getClassifier() {
        return classifier;
    }

}
