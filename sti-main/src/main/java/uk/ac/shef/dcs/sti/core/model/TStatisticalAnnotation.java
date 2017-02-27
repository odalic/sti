package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;

/**
 * Extension for statistical annotation
 */
public class TStatisticalAnnotation implements Serializable, Comparable<TStatisticalAnnotation> {

    private static final long serialVersionUID = -1208912663212074692L;

    private TComponentType component;

    private String predicateURI;
    private String predicateLabel;

    private double score;

    public TStatisticalAnnotation(TComponentType component, String predicateURI, String predicateLabel, double score) {
        this.component = component;
        this.predicateURI = predicateURI;
        this.predicateLabel = predicateLabel;
        this.score = score;
    }

    public TComponentType getComponent() {
        return component;
    }

    public String getPredicateURI() {
        return predicateURI;
    }

    public String getPredicateLabel() {
        return predicateLabel;
    }

    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(TStatisticalAnnotation o) {
        return new Double(o.getScore()).compareTo(getScore());
    }

    public boolean equals(Object o) {
        if(o instanceof TStatisticalAnnotation) {
            TStatisticalAnnotation hbr = (TStatisticalAnnotation) o;
            return hbr.getComponent().equals(getComponent()) &&
                   hbr.getPredicateURI().equals(getPredicateURI()) &&
                   hbr.getPredicateLabel().equals(getPredicateLabel());
        }
        return false;
    }

    public enum TComponentType implements Serializable {
        DIMENSION,
        MEASURE,
        NONE
    }
}
