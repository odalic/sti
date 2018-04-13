package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.classifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLException;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing.InputValue;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing.InputWithFeatures;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing.MLFeatureDetector;
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
    private static final String CLASSIFYING_RELATION_NAME = "classifying_dataset";
    private static final String EMPTY_CLASS_VALUE = "";

    private static final String PROPERTY_ML_CLASSIFIER_CONFIDENCE_THRESHOLD =
            "sti.tmp.ml.confidence.threshold";

    private static final double ML_CONFIDENCE_THRESHOLD_DEFAULT = 0.5;

    private String homePath;
    private Properties props;
    private MLFeatureDetector featureDetector;
    private InputValue[] trainingDatasetInputValues;
    private Double confidenceThreshold = ML_CONFIDENCE_THRESHOLD_DEFAULT;

    /**
     * Classes supported by the classifier (loaded from training dataset).
     */
    private List<String> classifierClasses;

    public MLClassifier(String homePath, Properties props, MLFeatureDetector featureDetector,
                        InputValue[] trainingDatasetInputValues) {
        this.homePath = homePath;
        this.featureDetector = featureDetector;
        this.trainingDatasetInputValues = trainingDatasetInputValues;
    }

    private Properties loadProps(Properties properties) throws IOException, MLException {
        Double confidenceThresholdOverride = getDoublePropertyValue(
            properties, PROPERTY_ML_CLASSIFIER_CONFIDENCE_THRESHOLD, ML_CONFIDENCE_THRESHOLD_DEFAULT
        );
        if (confidenceThresholdOverride != null) {
            this.confidenceThreshold = confidenceThresholdOverride;
        }
        LOG.info("ML Classifier: Using confidence threshold: " + this.confidenceThreshold + ".");
        return properties;
    }


    public void trainClassifier() throws MLException {
        try {
            LOG.debug("ML Classifier training: properties loaded.");

            Instances trainingDataset = buildTrainingDatasetInstances();
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

    /*public MLAttributeClassification classifyToAttribute(String valueToClassify) throws MLException {
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
            if (classification.nonEmpty()) {
                // map to URI from mapping
                String mlClassLabel = classification.getAttribute().getRelationLabel();
                String ontologyMappingValue = getOntologyMappingValue(mlClassLabel);
                if (ontologyMappingValue != null) {
                    // ontology mapping found
                    classification = classification.withMappedUri(ontologyMappingValue);
                } else {
                    // ontology mapping not found, just assign prefix to create an URI
                    classification = classification.withUriPrefix("http://odalic.eu/tmp/");
                }
            }
            return classification;

        } catch (Exception e) {
            throw new MLException("Failed to classify instance: " + e.getMessage(), e);
        }

    }*/

    public MLClassification classify(String valueToClassify) throws MLException {
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
            MLClassification classification = findMostProbableClass(valueToClassify, instanceToClassify, instanceDistribution);
            return classification;

        } catch (Exception e) {
            throw new MLException("Failed to classify instance: " + e.getMessage(), e);
        }
    }

    /*private String getOntologyMappingValue(String mlClassLabel) {
        return this.ontologyMapping.getOntologyMappingValue(mlClassLabel);
    }*/

    /**
     * Detect features of the InpuValues of trainingDatasetInputValues,
     * and output them in form of Weka Instances.
     * @return
     */
    protected Instances buildTrainingDatasetInstances() {
        // detect features
        InputWithFeatures[] inputValuesWithFeatures = featureDetector.detectFeatures(this.trainingDatasetInputValues);
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



    /*private MLAttributeClassification findMostProbableClass(String value, Instance instance, double[] classificationDistribution) {
        double maxValue = 0;
        int maxValueIndex = -1;
        for (int i = 0; i < classificationDistribution.length; i++) {
            if (classificationDistribution[i] > maxValue) {
                maxValue = classificationDistribution[i];
                maxValueIndex = i;
            }
        }
        if (maxValueIndex > -1) {
            if (maxValue >= this.confidenceThreshold) {
                String className = instance.classAttribute().value(maxValueIndex);
                return createMLAttributeClassification(value, className, maxValue);
            } else {
                // no value with high-enough confidence
                return createEmptyMLAttributeClassification(value);
            }
        }else {
            // classifier did not return any valid classification
            return createEmptyMLAttributeClassification(value);
        }
    }*/

    private MLClassification findMostProbableClass(String value, Instance instance, double[] classificationDistribution) {
        double maxValue = 0;
        int maxValueIndex = -1;
        for (int i = 0; i < classificationDistribution.length; i++) {
            if (classificationDistribution[i] > maxValue) {
                maxValue = classificationDistribution[i];
                maxValueIndex = i;
            }
        }
        if (maxValueIndex > -1) {
            if (maxValue >= this.confidenceThreshold) {
                return new MLClassification(instance.classAttribute().value(maxValueIndex), maxValue);
            } else {
                // no value with high-enough confidence
                return null;
            }
        }else {
            // classifier did not return any valid classification
            return null;
        }
    }

    /*private MLAttributeClassification createMLAttributeClassification(String value, String className, double score) {
        uk.ac.shef.dcs.kbproxy.model.Attribute stiAttribute = new SparqlAttribute(className, className, value, null);
        stiAttribute.setRelationLabel(className);
        return new MLAttributeClassification(value, stiAttribute, score);
    }

    private MLAttributeClassification createEmptyMLAttributeClassification(String value) {
        return new MLAttributeClassification(value, null, null);
    }*/

    protected abstract Classifier getClassifier();

    protected MLFeatureDetector getFeatureDetector() {
        return featureDetector;
    }

    protected Double getDoublePropertyValue(Properties props, String key, Double defaultValue) throws MLException {
        String strValue = props.getProperty(key);
        if (strValue != null) {
            try {
                return Double.parseDouble(strValue);
            } catch (NumberFormatException e){
                throw new MLException("Can not parse property: " + key + " (value: " + strValue + "). Expected: Double.");
            }
        } else {
            return defaultValue;
        }
    }

}
