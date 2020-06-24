package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config;

public class UriWithScore {

    private String uri;
    private double score;

    public UriWithScore(String uri, double score) {
        this.uri = uri;
        this.score = score;
    }

    public String getUri() {
        return uri;
    }

    public double getScore() {
        return score;
    }
}
