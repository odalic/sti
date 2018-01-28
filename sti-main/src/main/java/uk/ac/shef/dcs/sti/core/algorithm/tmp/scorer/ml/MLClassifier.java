package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml;

import org.apache.jena.atlas.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlAttribute;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.DatasetFileReader;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.InputValue;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.InputWithFeatures;
import weka.classifiers.Classifier;
import weka.core.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

public abstract class MLClassifier {

    private static final Logger LOG = LoggerFactory.getLogger(MLClassifier.class);

    private static final String CLASS_ATTRIBUTE_NAME = "class";
    private static final String BOOLEAN_TRUE = "true";
    private static final String BOOLEAN_FALSE = "false";
    private static final String TRAINING_RELATION_NAME = "traning_dataset";
    private static final String CLASSIFYING_RELATION_NAME = "classifying_dataset";
    private static final String EMPTY_CLASS_VALUE = "";

    private static final String PROPERTY_ML_CLASSIFIER_TRAINING_DATASET_FILEPATH =
            "sti.tmp.ml.training.dataset.file.path";

    private String homePath;
    private String propsFilePath;
    private Properties props;
    private DatasetFileReader fileReader;
    private MLFeatureDetector featureDetector;

    /**
     * Classes supported by the classifier (loaded from training dataset).
     */
    private List<String> classifierClasses;

    public MLClassifier(String homePath, String propsFilePath, DatasetFileReader fileReader, MLFeatureDetector featureDetector) {
        this.homePath = homePath;
        this.propsFilePath = propsFilePath;
        this.fileReader = fileReader;
        this.featureDetector = featureDetector;
    }

    private Properties loadProps(String propsFilePath) throws IOException {
        final Properties properties = new Properties();
        properties.load(new FileInputStream(propsFilePath));
        return properties;
    }


    public void trainClassifier() throws MLException {
        try {
            this.props = loadProps(this.propsFilePath);
            LOG.debug("ML Classifier training: properties loaded.");

            Instances trainingDataset = readDatasetFile(getMLClassifierTrainingDatasetFilePath());
            LOG.info("ML Classifier training: training dataset file reading done.");
            LOG.info("ML Classifier training: Parsed {} instances.", trainingDataset.size());
            classifierClasses = getClassesFromInstances(trainingDataset);
            LOG.info("ML Classifier training: retrieved {} classes.", classifierClasses.size());
            Classifier classifier = getClassifier();
            LOG.info("ML Classifier training: retrieved classifier: " + classifier.toString() + ". Building..");
            classifier.buildClassifier(trainingDataset);
            LOG.info("ML Classifier training: classifier built.");
        } catch (Exception e) {
            throw new MLException("Failed to train RandomForest classifier: " + e.getMessage());
        }
    }

    public MLAttributeClassification classifyToAttribute(String valueToClassify) throws MLException {
        if (classifierClasses == null || classifierClasses.size() == 0) {
            throw new MLException("Classifier not trained!");
        }
        try {
            // convert to instance (calculate features, assign classes and attributes)
            InputValue inputValue = new InputValue(valueToClassify, EMPTY_CLASS_VALUE);
            InputWithFeatures inputValueWithFeatures = featureDetector.detectFeatures(inputValue);
            Instances instancesToClassify = convertUnknownClassValueToInstance(inputValueWithFeatures);
            Instance instanceToClassify = instancesToClassify.firstInstance();

            // classify converted value using classifier
            Classifier classifier = getClassifier();
            LOG.debug("Classificating '" + valueToClassify + "' using " + classifier.getClass().getSimpleName() + ".");

            double[] instanceDistribution = classifier.distributionForInstance(instanceToClassify);
            // find index of class with highest classifier score
            MLAttributeClassification classification = findMostProbableClass(valueToClassify, instanceToClassify, instanceDistribution);

            // add URI prefix
            return classification.withUriPrefix("http://kadlecek.sk/tmp/");

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

        // get list of distinct classes
        Set<String> allDistinctClasses = new HashSet<>();
        for (InputWithFeatures inputValue : inputValues) {
            allDistinctClasses.add(inputValue.getClazz());
        }
        List<String> lstAllDistinctClasses = new ArrayList<>(allDistinctClasses);

        Attribute classAttribute = new Attribute(CLASS_ATTRIBUTE_NAME, lstAllDistinctClasses);
        fvWekaAttributes.add(classAttribute);
        int classAttributeIndex = fvWekaAttributes.size() - 1;

        // create instances entity - needs to be created before actual Instance entities, as
        // it has side effect on fvWekaAttributes list
        Instances instances = new Instances(TRAINING_RELATION_NAME, fvWekaAttributes, inputValues.length);
        instances.setClassIndex(classAttributeIndex);

        // create Instance entities
        for (InputWithFeatures inputValue : inputValues) {
            String clazz = inputValue.getClazz();
            allDistinctClasses.add(clazz);

            Instance instance = createInstanceFromInputWithFeatures(intFeatureKeys, boolFeatureKeys,
                    fvWekaAttributes, inputValue);
            // assign class value to instance
            instance.setValue(classAttribute, inputValue.getClazz());
            instances.add(instance);
        }
        return instances;
    }

    private Instances convertUnknownClassValueToInstance(InputWithFeatures valueToClassify) {
        List<String> intFeatureKeys = valueToClassify.getIntFeaturesKeys();
        List<String> boolFeatureKeys = valueToClassify.getBoolFeaturesKeys();

        ArrayList<Attribute> fvWekaAttributes = initInstanceWekaAttributes(intFeatureKeys, boolFeatureKeys);
        Attribute classAttribute = new Attribute(CLASS_ATTRIBUTE_NAME, classifierClasses);
        fvWekaAttributes.add(classAttribute);

        Instances instances = new Instances(CLASSIFYING_RELATION_NAME, fvWekaAttributes, 1);
        instances.setClassIndex(fvWekaAttributes.size() - 1);

        DenseInstance instance = createInstanceFromInputWithFeatures(intFeatureKeys, boolFeatureKeys,
                fvWekaAttributes, valueToClassify);

        instance.setValue(classAttribute, Utils.missingValue());
        instances.add(instance);

        return instances;
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

    private DenseInstance createInstanceFromInputWithFeatures(List<String> intFeatureKeys, List<String> boolFeatureKeys,
                                                              List<Attribute> fvWekaAttributes,
                                                              InputWithFeatures inputValue) {

        int totalNumberOfFeatures = getTotalNumberOfFeatures(intFeatureKeys, boolFeatureKeys);

        DenseInstance instance = new DenseInstance(totalNumberOfFeatures);
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

    private MLAttributeClassification findMostProbableClass(String value, Instance instance, double[] classificationDistribution) {
        double maxValue = 0;
        int maxValueIndex = -1;
        for (int i = 0; i < classificationDistribution.length; i++) {
            if (classificationDistribution[i] > maxValue) {
                maxValue = classificationDistribution[i];
                maxValueIndex = i;
            }
        }
        if (maxValueIndex > -1) {
            String className = instance.classAttribute().value(maxValueIndex);
            return createMLAttributeClassification(value, className, maxValue);
        }else {
            // classifier did not return any valid classification
            String className = instance.classAttribute().value(0);
            return createMLAttributeClassification(value, className, maxValue);
        }
    }

    private MLAttributeClassification createMLAttributeClassification(String value, String className, double score) {
        uk.ac.shef.dcs.kbproxy.model.Attribute stiAttribute = new SparqlAttribute(className, value);
        stiAttribute.setRelationLabel(className);
        return new MLAttributeClassification(value, stiAttribute, score);
    }

    protected abstract Classifier getClassifier();

    protected MLFeatureDetector getFeatureDetector() {
        return featureDetector;
    }

    private String getMLClassifierTrainingDatasetFilePath() throws MLException {
        String propertyDescription = "ML Classifier training dataset file";
        String trainingDatasetFilePath = this.props.getProperty(PROPERTY_ML_CLASSIFIER_TRAINING_DATASET_FILEPATH);

        if (trainingDatasetFilePath != null) {
            String fullPath = combinePaths(homePath, trainingDatasetFilePath);

            LOG.info("ML Classifier training dataset file: '" + fullPath +"'.");

            if ((fullPath != null) && new File(fullPath).exists()) {
                return fullPath;
            } else {
                final String error = "Cannot proceed: " + propertyDescription + " is not set or does not exist. "
                        + PROPERTY_ML_CLASSIFIER_TRAINING_DATASET_FILEPATH + "=" + trainingDatasetFilePath;
                throw new MLException(error);
            }
        } else {
            final String error = "Cannot proceed: " + propertyDescription + " is not set. "
                    + "Property: " + PROPERTY_ML_CLASSIFIER_TRAINING_DATASET_FILEPATH;
            throw new MLException(error);
        }

    }

}
