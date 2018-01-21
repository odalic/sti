package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlAttribute;

public class MLAttributeClassification {

    private String value;
    private Attribute attribute;
    private Double score;

    public MLAttributeClassification(String value, Attribute attribute, Double score) {
        this.value = value;
        this.attribute = attribute;
        this.score = score;
    }

    public MLAttributeClassification withUriPrefix(String prefix) {
        String uriWoPrefix = attribute.getRelationURI();
        Attribute newAttribute = new SparqlAttribute(prefix + uriWoPrefix, value);
        return new MLAttributeClassification(value, newAttribute, score);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
