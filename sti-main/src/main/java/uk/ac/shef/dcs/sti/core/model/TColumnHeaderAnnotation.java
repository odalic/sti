package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.shef.dcs.kbproxy.model.Clazz;

/**
 * Created with IntelliJ IDEA. User: zqz Date: 24/01/14 Time: 14:48 To change this template use File
 * | Settings | File Templates.
 */
public class TColumnHeaderAnnotation implements Serializable, Comparable<TColumnHeaderAnnotation> {
  private static final long serialVersionUID = -6208426814708405913L;

  public static final String SCORE_FINAL = "final";

  public static TColumnHeaderAnnotation copy(final TColumnHeaderAnnotation ha) {
    final TColumnHeaderAnnotation newHa =
        new TColumnHeaderAnnotation(ha.getHeaderText(), ha.getAnnotation(), ha.getFinalScore());
    for (final int i : ha.getSupportingRows()) {
      newHa.addSupportingRow(i);
    }
    newHa.setScoreElements(new HashMap<>(ha.getScoreElements()));
    return newHa;
  }

  private String headerText;
  private final Clazz annotation;
  private double finalScore;
  private Map<String, Double> scoreElements;


  private final List<Integer> supportingRows;

  public TColumnHeaderAnnotation(final String headerText, final Clazz annotation,
      final double finalScore) {
    this.headerText = headerText;
    this.annotation = annotation;
    this.finalScore = finalScore;
    this.supportingRows = new ArrayList<>();
    this.scoreElements = new HashMap<>();
    if (finalScore > 0) {
      this.scoreElements.put(SCORE_FINAL, finalScore);
    } else {
      this.scoreElements.put(SCORE_FINAL, 0.0);
    }
  }

  public void addSupportingRow(final int rowId) {
    if (!this.supportingRows.contains(rowId)) {
      this.supportingRows.add(rowId);
    }
  }

  @Override
  public int compareTo(final TColumnHeaderAnnotation o) {
    final int compared = ((Double) o.getFinalScore()).compareTo(getFinalScore());
    if (compared == 0) {
      return new Integer(o.getSupportingRows().size()).compareTo(getSupportingRows().size());
    }

    return compared;
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof TColumnHeaderAnnotation) {
      final TColumnHeaderAnnotation ha = (TColumnHeaderAnnotation) o;
      return ha.getHeaderText().equals(getHeaderText())
          && ha.getAnnotation().equals(getAnnotation());
    }
    return false;
  }

  public Clazz getAnnotation() {
    return this.annotation;
  }

  public double getFinalScore() {
    return this.finalScore;
  }

  public String getHeaderText() {
    return this.headerText;
  }

  public Map<String, Double> getScoreElements() {
    return this.scoreElements;
  }

  public List<Integer> getSupportingRows() {
    return this.supportingRows;
  }

  @Override
  public int hashCode() {
    return getHeaderText().hashCode() + (19 * getAnnotation().getId().hashCode());
  }

  public void setFinalScore(final double finalScore) {
    this.finalScore = finalScore;
  }

  public void setHeaderText(final String headerText) {
    this.headerText = headerText;
  }

  public void setScoreElements(final Map<String, Double> scoreElements) {
    this.scoreElements = scoreElements;
  }

  @Override
  public String toString() {
    return this.headerText + "," + getAnnotation();
  }
}
