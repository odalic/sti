package cz.cuni.mff.xrg.odalic.input.ml;

import cz.cuni.mff.xrg.odalic.files.File;

public class TaskMLConfiguration {

    private boolean useMlClassifier;
    private File trainingDatasetFile;

    public static TaskMLConfiguration disabled() {
        return new TaskMLConfiguration(false, null);
    }

    public TaskMLConfiguration(boolean useMlClassifier, File trainingDatasetFile) {
        this.useMlClassifier = useMlClassifier;
        this.trainingDatasetFile = trainingDatasetFile;
    }

    public boolean isUseMlClassifier() {
        return useMlClassifier;
    }

    public File getTrainingDatasetFile() {
        return trainingDatasetFile;
    }
}
