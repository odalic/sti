package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.DatasetFileReader;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.InputValue;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.InputWithFeatures;
import weka.classifiers.Classifier;
import weka.core.*;

import java.io.IOException;
import java.util.*;

public abstract class MLClassifier {

    private static final Logger LOG = LoggerFactory.getLogger(MLClassifier.class);

    private static final String CLASS_ATTRIBUTE_NAME = "class";
    private static final String BOOLEAN_TRUE = "true";
    private static final String BOOLEAN_FALSE = "false";
    private static final String TRAINING_RELATION_NAME = "traning_dataset";
    private static final String EMPTY_CLASS_VALUE = "";

    private DatasetFileReader fileReader;
    private MLFeatureDetector featureDetector;

    /**
     * Classes supported by the classifier (loaded from training dataset).
     */
    private List<String> classifierClasses;

    public MLClassifier(DatasetFileReader fileReader, MLFeatureDetector featureDetector) {
        this.fileReader = fileReader;
        this.featureDetector = featureDetector;
    }

    public void trainClassifier(String trainingDatasetFilePath) throws MLException {
        try {
            Instances trainingDataset = readDatasetFile(trainingDatasetFilePath);
            classifierClasses = getClassesFromInstances(trainingDataset);
            getClassifier().buildClassifier(trainingDataset);
        } catch (Exception e) {
            throw new MLException("Failed to train RandomForest classifier: " + e.getMessage());
        }
    }

    // TODO change return type to more appropriate one
    public String classify(String valueToClassify) throws MLException {
        if (classifierClasses == null || classifierClasses.size() == 0) {
            throw new MLException("Classifier not trained!");
        }
        try {
            // convert to instance (calculate features, assign classes and attributes)
            InputValue inputValue = new InputValue(valueToClassify, EMPTY_CLASS_VALUE);
            InputWithFeatures inputValueWithFeatures = featureDetector.detectFeatures(inputValue);
            Instance instanceToClassify = convertUnknownClassValueToInstance(inputValueWithFeatures);

            // classify converted value using classifier
            Classifier classifier = getClassifier();
            LOG.debug("Classificating '" + valueToClassify + "' using " + classifier.getClass().getSimpleName() + ".");

            double classifiedClass = classifier.classifyInstance(instanceToClassify);
            instanceToClassify.setClassValue(classifiedClass);

            // return class classified by classifier
            return classifierClasses.get((int) classifiedClass);
        } catch (Exception e) {
            throw new MLException("Failed to classify instance: " + e.getMessage(), e);
        }

    }

    /**
     * Reads the CSV file with given training data, detect features of the records
     * and output them in form of Weka Instances.
     * @param filename
     * @return
     * @throws IOException
     */
    protected Instances readDatasetFile(String filename) throws IOException {
        // parse input file
        InputValue[] parsedInputValues = fileReader.readFile(filename);
        // detect features
        InputWithFeatures[] inputValuesWithFeatures = featureDetector.detectFeatures(parsedInputValues);
        // build Instances instance
        return convertTrainingDatasetWithFeaturesToInstances(inputValuesWithFeatures);
    }

    protected List<String> getClassesFromInstances(Instances instances) {
        List<String> classes = new ArrayList<>();
        Attribute classAttribute = instances.classAttribute();
        for (int i = 0; i < classAttribute.numValues(); i++) {
            classes.add(classAttribute.value(i));
        }
        return classes;
    }

    private Instances convertTrainingDatasetWithFeaturesToInstances(InputWithFeatures[] inputValues) throws IllegalArgumentException {
        // check, if list of input values is nonempty
        if (inputValues.length == 0) {
            throw new IllegalArgumentException("Training dataset is empty!");
        }

        // all inputValues should contain same set of features
        List<String> intFeatureKeys = inputValues[0].getIntFeaturesKeys();
        List<String> boolFeatureKeys = inputValues[0].getBoolFeaturesKeys();

        ArrayList<Attribute> fvWekaAttributes = initInstanceWekaAttributes(intFeatureKeys, boolFeatureKeys);
        int classAttributeIndex = fvWekaAttributes.size() - 1;

        // create Instance entities
        Set<String> allDistinctClasses = new HashSet<>();
        List<Instance> lstInstances = new ArrayList<>();
        for (InputWithFeatures inputValue : inputValues) {

            String clazz = inputValue.getClazz();
            allDistinctClasses.add(clazz);

            Instance instance = createInstanceFromInputWithFeatures(intFeatureKeys, boolFeatureKeys, fvWekaAttributes, inputValue);
            // assign class value to instance
            instance.setValue(classAttributeIndex, inputValue.getClazz());
            lstInstances.add(instance);
        }

        List<String> lstAllDistinctClasses = new ArrayList<>(allDistinctClasses);
        fvWekaAttributes.add(new Attribute(CLASS_ATTRIBUTE_NAME, lstAllDistinctClasses));

        Instances instances = new Instances(TRAINING_RELATION_NAME, fvWekaAttributes, lstInstances.size());
        instances.setClassIndex(classAttributeIndex);

        for (Instance instance : lstInstances) {
            instances.add(instance);
        }

        return instances;
    }

    private Instance convertUnknownClassValueToInstance(InputWithFeatures valueToClassify) {
        List<String> intFeatureKeys = valueToClassify.getIntFeaturesKeys();
        List<String> boolFeatureKeys = valueToClassify.getBoolFeaturesKeys();

        ArrayList<Attribute> fvWekaAttributes = initInstanceWekaAttributes(intFeatureKeys, boolFeatureKeys);
        fvWekaAttributes.add(new Attribute(CLASS_ATTRIBUTE_NAME, classifierClasses));

        Instance instance = createInstanceFromInputWithFeatures(intFeatureKeys, boolFeatureKeys, fvWekaAttributes, valueToClassify);
        // assign class value to instance
        int classAttributeIndex = fvWekaAttributes.size() - 1;
        instance.setValue(classAttributeIndex, Utils.missingValue());
        return instance;
    }

    private ArrayList<Attribute> initInstanceWekaAttributes(List<String> intFeatureKeys, List<String> boolFeatureKeys) {
        int totalNumberOfFeatures = getTotalNumberOfFeatures(intFeatureKeys, boolFeatureKeys);

        // Declare Instances attributes & metadata
        ArrayList<String> booleanValues = new ArrayList<>(2);
        booleanValues.add(BOOLEAN_TRUE);
        booleanValues.add(BOOLEAN_FALSE);

        ArrayList<Attribute> fvWekaAttributes = new ArrayList<>(totalNumberOfFeatures);

        for (String intKey : intFeatureKeys) {
            fvWekaAttributes.add(new Attribute(intKey));
        }

        for (String boolKey : boolFeatureKeys) {
            fvWekaAttributes.add(new Attribute(boolKey, booleanValues));
        }
        return fvWekaAttributes;
    }

    private int getTotalNumberOfFeatures(List<String> intFeatureKeys, List<String> boolFeatureKeys) {
        return intFeatureKeys.size() + boolFeatureKeys.size() + 1;
    }

    private Instance createInstanceFromInputWithFeatures(List<String> intFeatureKeys, List<String> boolFeatureKeys,
                                                         List<Attribute> fvWekaAttributes, InputWithFeatures inputValue) {

        int totalNumberOfFeatures = getTotalNumberOfFeatures(intFeatureKeys, boolFeatureKeys);

        Instance instance = new DenseInstance(totalNumberOfFeatures);
        for (int intKeyIdx = 0; intKeyIdx < intFeatureKeys.size(); intKeyIdx++) {
            instance.setValue(
                    fvWekaAttributes.get(intKeyIdx),
                    inputValue.getIntFeature(intFeatureKeys.get(intKeyIdx))
            );
        }
        for (int boolKeyIdx = 0; boolKeyIdx < boolFeatureKeys.size(); boolKeyIdx++) {
            int attributeIdx = intFeatureKeys.size() + boolKeyIdx;
            instance.setValue(
                    fvWekaAttributes.get(attributeIdx),
                    String.valueOf(inputValue.getBoolFeature(boolFeatureKeys.get(boolKeyIdx)))
            );
        }
        return instance;
    }

    protected abstract Classifier getClassifier();

    protected MLFeatureDetector getFeatureDetector() {
        return featureDetector;
    }
}
