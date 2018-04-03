package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

public class MLPropertiesLoader {

    private static final Logger LOG = LoggerFactory.getLogger(MLPropertiesLoader.class);

    private String homePath;
    private String propsFilePath;
    private Properties props;

    private static final String PROPERTY_ML_CLASSIFIER_TRAINING_DATASET_FILEPATH =
            "sti.tmp.ml.training.dataset.file.path";

    private static final String PROPERTY_ML_CLASSIFIER_ONTOLOGY_MAPPING_FILEPATH =
            "sti.tmp.ml.ontology.mapping.file.path";

    private static final String PROPERTY_ML_CLASSIFIER_ONTOLOGY_DEFINITION_FILEPATHS =
            "sti.tmp.ml.ontology.definition.files";

    private static final char FILE_PATH_SEPARATOR = ',';

    public MLPropertiesLoader(String homePath, String propsFilePath) {
        this.homePath = homePath;
        this.propsFilePath = propsFilePath;
    }

    public Properties loadProps() throws IOException {
        final Properties properties = new Properties();
        properties.load(new FileInputStream(propsFilePath));
        return properties;
    }

    public Properties getProperties() throws IOException {
        if (props == null) {
            this.props = loadProps();
        }
        return props;
    }

    public String getMLClassifierTrainingDatasetFilePath() throws IOException, MLException {
        return getMLClassifierFilePath(
                PROPERTY_ML_CLASSIFIER_TRAINING_DATASET_FILEPATH,
                "ML Classifier training dataset file"
        );
    }

    public String getMLClassifierOntologyMappingFilePath() throws IOException, MLException {
        return getMLClassifierFilePath(
                PROPERTY_ML_CLASSIFIER_ONTOLOGY_MAPPING_FILEPATH,
                "ML Classifier ontology mapping file"
        );
    }

    public String[] getMLClassifierOntologyDefinitionFilePaths() throws IOException, MLException {
        return getMLClassifierFilePaths(
                PROPERTY_ML_CLASSIFIER_ONTOLOGY_DEFINITION_FILEPATHS,
                "ML Classifier ontology definitions file(s)"
        );
    }

    private String getMLClassifierFilePath(String propertyKey, String propertyDescription) throws IOException, MLException  {
        String relativeFilePath = getProperties().getProperty(propertyKey);

        if (relativeFilePath != null) {
            String fullPath = combinePaths(homePath, relativeFilePath);

            LOG.info(propertyDescription + ": '" + fullPath +"'.");

            if ((fullPath != null) && new File(fullPath).exists()) {
                return fullPath;
            } else {
                final String error = "Cannot proceed: " + propertyDescription + " is not set or does not exist. "
                        + propertyKey + "=" + relativeFilePath;
                throw new MLException(error);
            }
        } else {
            final String error = "Cannot proceed: " + propertyDescription + " is not set. "
                    + "Property: " + propertyKey;
            throw new MLException(error);
        }
    }

    private String[] getMLClassifierFilePaths(String propertyKey, String propertyDescription) throws IOException, MLException  {
        String relativeFilePathsConcat = getProperties().getProperty(propertyKey);

        if (relativeFilePathsConcat != null) {
            String[] relativeFilePaths = StringUtils.split(relativeFilePathsConcat, FILE_PATH_SEPARATOR);

            return Arrays.stream(relativeFilePaths)
                    .map(relPath -> combinePaths(homePath, relPath))
                    .toArray(String[]::new);
        } else {
            final String error = "Cannot proceed: " + propertyDescription + " is not set. "
                    + "Property: " + propertyKey;
            throw new MLException(error);
        }
    }
}
