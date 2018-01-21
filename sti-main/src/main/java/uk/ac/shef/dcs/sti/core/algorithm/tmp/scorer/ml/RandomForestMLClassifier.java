package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml;

import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.DatasetFileReader;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;

public class RandomForestMLClassifier extends MLClassifier {

    private static final int MAX_DEPTH = 15;
    private static final int NUM_FEATURES = 3;
    private static final int NUM_BAGGING_INTERATIONS = 120;
    private static final boolean BREAK_TIES_RANDOMLY = true;

    private RandomForest classifier;

    public RandomForestMLClassifier(String homePath, String propsFilePath, DatasetFileReader fileReader, MLFeatureDetector featureDetector) {
        super(homePath, propsFilePath, fileReader, featureDetector);
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
