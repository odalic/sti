package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml;

public class MLClassification {

    private String mlClass;
    private Double score;

    public MLClassification(String mlClass, Double score) {
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
