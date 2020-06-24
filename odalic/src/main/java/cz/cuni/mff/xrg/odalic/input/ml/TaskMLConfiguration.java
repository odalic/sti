package cz.cuni.mff.xrg.odalic.input.ml;

import cz.cuni.mff.xrg.odalic.input.ParsingResult;

public class TaskMLConfiguration {

    private boolean useMlClassifier;
    private ParsingResult trainingDataset;

    public static TaskMLConfiguration disabled() {

        return new TaskMLConfiguration(false, null);
    }

    public TaskMLConfiguration(boolean useMlClassifier,
                               ParsingResult trainingDataset) {
        this.useMlClassifier = useMlClassifier;
        this.trainingDataset = trainingDataset;
    }

    public boolean isUseMlClassifier() {
        return useMlClassifier;
    }

    public ParsingResult getTrainingDataset() {
        return trainingDataset;
    }
}
