package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.classifier;

import cz.cuni.mff.xrg.odalic.util.parsing.UriParsingUtil;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.sparql.SparqlAttribute;

public class MLAttributeClassification {

    private String value;
    private Attribute attribute;
    private Double score;
    private boolean empty = true;

    public MLAttributeClassification(String value, Attribute attribute, Double score) {
        this.value = value;
        this.attribute = attribute;
        this.score = score;
        if (this.attribute == null) {
            this.empty = true;
        }else {
            this.empty = false;
        }
    }

    public MLAttributeClassification withUriPrefix(String prefix) {
        String uriWoPrefix = attribute.getRelationURI();
        Attribute newAttribute = new SparqlAttribute(uriWoPrefix, prefix + uriWoPrefix,  value, null);
        newAttribute.setRelationLabel(attribute.getRelationLabel());
        return new MLAttributeClassification(value, newAttribute, score);
    }

    public MLAttributeClassification withMappedUri(String uri) {
        boolean applyUriLabelHeuristics = true;
        String label = UriParsingUtil.parseLabelFromResourceUri(uri, applyUriLabelHeuristics);
        Attribute newAttribute = new SparqlAttribute(label, uri,  value, null);
        return new MLAttributeClassification(value, newAttribute, score);
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean nonEmpty() {
        return !empty;
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
