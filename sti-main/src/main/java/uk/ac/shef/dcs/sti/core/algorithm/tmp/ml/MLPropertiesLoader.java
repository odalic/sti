package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.exception.MLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static uk.ac.shef.dcs.util.StringUtils.combinePaths;

public class MLPropertiesLoader {

    private static final Logger LOG = LoggerFactory.getLogger(MLPropertiesLoader.class);

    private String homePath;
    private String propsFilePath;
    private Properties props;

    private static final String PROPERTY_ML_CLASSIFIER_ONTOLOGY_MAPPING_FILEPATH =
            "sti.tmp.ml.ontology.mapping.file.path";

    private static final String PROPERTY_ML_CLASSIFIER_ONTOLOGY_DEFINITIONS_FOLDERPATH =
            "sti.tmp.ml.ontology.definition.folder";

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

    public String getMLClassifierOntologyMappingFilePath() throws IOException, MLException {
        return getMLClassifierFilePath(
                PROPERTY_ML_CLASSIFIER_ONTOLOGY_MAPPING_FILEPATH,
                "ML Classifier ontology mapping file"
        );
    }

    public String[] getMLClassifierOntologyDefinitionFilePaths() throws IOException, MLException {
        String fullFolderPath = getMLClassifierFilePath(
                PROPERTY_ML_CLASSIFIER_ONTOLOGY_DEFINITIONS_FOLDERPATH,
                "ML Classifier ontology definitions folder"
        );
        return listAllFilesInFolder(fullFolderPath);
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

    private String[] listAllFilesInFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] allFiles = folder.listFiles();

        List<String> files = new ArrayList<>();
        if (allFiles != null) {
            for (File file : allFiles) {
                if (file.isFile()) {
                    files.add(file.getAbsolutePath());
                }
            }
        }

        String[] filesArray = new String[files.size()];
        return files.toArray(filesArray);
    }
}
