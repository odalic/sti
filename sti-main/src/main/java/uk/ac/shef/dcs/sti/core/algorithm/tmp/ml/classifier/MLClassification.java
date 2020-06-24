package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.classifier;

import java.util.Map;

public class MLClassification {

    private String cellValue;
    private Map<String, Double> classifications;

    public MLClassification(String cellValue, Map<String, Double> classifications) {
        this.cellValue = cellValue;
        this.classifications = classifications;
    }

    public String getCellValue() {
        return cellValue;
    }

    public Map<String, Double> getClassifications() {
        return classifications;
    }

    public MLClassificationWithScore getHighestScoreMlClass(double confidenceThreshold) {
        double maxScore = 0;
        String maxClass = null;
        for (Map.Entry<String, Double> entry : classifications.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                maxClass = entry.getKey();
            }
        }
        if (maxClass != null) {
            if (maxScore >= confidenceThreshold) {
                return new MLClassificationWithScore(maxClass, maxScore);
            } else {
                // no value with high-enough confidence
                return null;
            }
        }else {
            // classifier did not return any valid classification
            return null;
        }
    }
}
