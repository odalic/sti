package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;

/**
 * Extension for statistical annotation
 */
public class TStatisticalAnnotation implements Serializable, Comparable<TStatisticalAnnotation> {

  public enum TComponentType implements Serializable {
    DIMENSION, MEASURE, NONE
  }

  private static final long serialVersionUID = -1208912663212074692L;

  private final TComponentType component;
  private final String predicateURI;

  private final String predicateLabel;

  private final double score;

  public TStatisticalAnnotation(final TComponentType component, final String predicateURI,
      final String predicateLabel, final double score) {
    this.component = component;
    this.predicateURI = predicateURI;
    this.predicateLabel = predicateLabel;
    this.score = score;
  }

  @Override
  public int compareTo(final TStatisticalAnnotation o) {
    return new Double(o.getScore()).compareTo(getScore());
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof TStatisticalAnnotation) {
      final TStatisticalAnnotation hbr = (TStatisticalAnnotation) o;
      return hbr.getComponent().equals(getComponent())
          && hbr.getPredicateURI().equals(getPredicateURI())
          && hbr.getPredicateLabel().equals(getPredicateLabel());
    }
    return false;
  }

  public TComponentType getComponent() {
    return this.component;
  }

  public String getPredicateLabel() {
    return this.predicateLabel;
  }

  public String getPredicateURI() {
    return this.predicateURI;
  }

  public double getScore() {
    return this.score;
  }
}
