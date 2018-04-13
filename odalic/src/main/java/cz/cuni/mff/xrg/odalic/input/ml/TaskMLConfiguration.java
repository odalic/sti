package cz.cuni.mff.xrg.odalic.input.ml;

import cz.cuni.mff.xrg.odalic.files.formats.Format;

public class TaskMLConfiguration {

    private boolean useMlClassifier;
    private Format trainingDatasetFileFormat;
    private String trainingDatasetFileContents;

    public static TaskMLConfiguration disabled() {

        return new TaskMLConfiguration(false, null, null);
    }

    public TaskMLConfiguration(boolean useMlClassifier, Format trainingDatasetFileFormat, String trainingDatasetFileContents) {
        this.useMlClassifier = useMlClassifier;
        this.trainingDatasetFileFormat = trainingDatasetFileFormat;
        this.trainingDatasetFileContents = trainingDatasetFileContents;
    }

    public boolean isUseMlClassifier() {
        return useMlClassifier;
    }

    public Format getTrainingDatasetFileFormat() {
        return trainingDatasetFileFormat;
    }

    public String getTrainingDatasetFileContents() {
        return trainingDatasetFileContents;
    }
}
