package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import uk.ac.shef.dcs.kbproxy.model.Entity;

/**
 * Annotation for an entity or a concept
 */
public class TCellAnnotation implements Serializable, Comparable<TCellAnnotation> {

  private static final long serialVersionUID = -8136725814000843856L;

  public static final String SCORE_FINAL = "final";

  public static TCellAnnotation copy(final TCellAnnotation ca) {
    final TCellAnnotation newCa = new TCellAnnotation(ca.getTerm(), ca.getAnnotation(),
        ca.getFinalScore(), new HashMap<>(ca.getScoreElements()));
    return newCa;
  }

  private String term;
  private Entity annotation;
  private final Map<String, Double> score_element_map;

  private double finalScore;

  public TCellAnnotation(final String term, final Entity annotation, final double score,
      final Map<String, Double> score_elements) {
    this.term = term;
    this.annotation = annotation;
    this.finalScore = score;
    this.score_element_map = score_elements;
  }

  @Override
  public int compareTo(final TCellAnnotation o) {

    return new Double(o.getFinalScore()).compareTo(getFinalScore());

  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof TCellAnnotation) {
      final TCellAnnotation ca = (TCellAnnotation) o;
      return ca.getAnnotation().equals(getAnnotation()) && ca.getTerm().equals(getTerm());
    }
    return false;
  }

  public Entity getAnnotation() {
    return this.annotation;
  }

  public double getFinalScore() {
    return this.finalScore;
  }

  public Map<String, Double> getScoreElements() {
    return this.score_element_map;
  }

  public String getTerm() {
    return this.term;
  }

  public void setAnnotation(final Entity annotation) {
    this.annotation = annotation;
  }

  public void setFinalScore(final double score) {
    this.finalScore = score;
  }

  public void setTerm(final String term) {
    this.term = term;
  }


  @Override
  public String toString() {
    return getTerm() + "," + getAnnotation();
  }
}
