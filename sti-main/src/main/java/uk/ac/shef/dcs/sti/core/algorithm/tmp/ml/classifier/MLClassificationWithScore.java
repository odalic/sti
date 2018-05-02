package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.classifier;

public class MLClassificationWithScore {
    private String mlClass;
    private Double score;

    public MLClassificationWithScore(String mlClass, Double score) {
        this.mlClass = mlClass;
        this.score = score;
    }

    public String getMlClass() {
        return mlClass;
    }

    public Double getScore() {
        return score;
    }
}
