package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.config;

import java.util.Map;
import java.util.Set;

public class MLFeedback {

    Set<Integer> ignoreColumns;
    Map<Integer, String> classifications;

    public MLFeedback(Set<Integer> ignoreColumns, Map<Integer, String> classifications) {
        this.ignoreColumns = ignoreColumns;
        this.classifications = classifications;
    }

    public Set<Integer> getIgnoreColumns() {
        return ignoreColumns;
    }

    public void setIgnoreColumns(Set<Integer> ignoreColumns) {
        this.ignoreColumns = ignoreColumns;
    }

    public Map<Integer, String> getClassifications() {
        return classifications;
    }

    public void setClassifications(Map<Integer, String> classifications) {
        this.classifications = classifications;
    }
}
