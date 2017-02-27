package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class TColumnColumnRelationAnnotation
    implements Serializable, Comparable<TColumnColumnRelationAnnotation> {

  private static final long serialVersionUID = -1208912663212074692L;
  public static final String SCORE_FINAL = "final";

  public static String toStringExpanded(final int fromCol, final int toCol,
      final String relationURL) {
    return "(" + fromCol + "-" + toCol + ")" + relationURL;
  }

  private RelationColumns relationColumns;
  private final List<Integer> supportingRows;

  private final String relationURI;

  private String relationLabel;
  private double finalScore;


  private Map<String, Double> scoreElements;

  // matched_value[]: (0)=property name (1)=the attribute value matched with the objecCol field on
  // this row; (2) id/uri, if any (used for later knowledge base retrieval)
  public TColumnColumnRelationAnnotation(final RelationColumns key, final String relationURI,
      final String relation_label, final double score) {
    this.relationColumns = key;
    this.relationLabel = relation_label;
    this.relationURI = relationURI;
    this.finalScore = score;
    this.scoreElements = new HashMap<>();
    this.scoreElements = new HashMap<>();
    if (score != 0) {
      this.scoreElements.put(SCORE_FINAL, score);
    } else {
      this.scoreElements.put(SCORE_FINAL, 0.0);
    }
    this.supportingRows = new ArrayList<>();
  }

  public void addSupportingRow(final int row) {
    if (!this.supportingRows.contains(row)) {
      this.supportingRows.add(row);
    }
  }

  @Override
  public int compareTo(final TColumnColumnRelationAnnotation o) {
    final int compared = new Double(o.getFinalScore()).compareTo(getFinalScore());

    if (compared == 0) {
      return new Integer(o.getSupportingRows().size()).compareTo(getSupportingRows().size());
    }

    return compared;
  }


  @Override
  public boolean equals(final Object o) {
    if (o instanceof TColumnColumnRelationAnnotation) {
      final TColumnColumnRelationAnnotation hbr = (TColumnColumnRelationAnnotation) o;
      return hbr.getRelationColumns().equals(getRelationColumns())
          && hbr.getRelationURI().equals(getRelationURI());
    }
    return false;
  }

  public double getFinalScore() {
    return this.finalScore;
  }

  public RelationColumns getRelationColumns() {
    return this.relationColumns;
  }


  public String getRelationLabel() {
    return this.relationLabel;
  }

  public String getRelationURI() {
    return this.relationURI;
  }

  public Map<String, Double> getScoreElements() {
    return this.scoreElements;
  }

  public List<Integer> getSupportingRows() {
    return this.supportingRows;
  }

  public void setFinalScore(final double finalScore) {
    this.finalScore = finalScore;
  }

  public void setRelationColumns(final RelationColumns relationColumns) {
    this.relationColumns = relationColumns;
  }

  public void setRelationLabel(final String relationLabel) {
    this.relationLabel = relationLabel;
  }

  public void setScoreElements(final Map<String, Double> scoreElements) {
    this.scoreElements = scoreElements;
  }

  @Override
  public String toString() {
    return this.relationURI;
  }

  public String toStringExpanded() {
    return "(" + getRelationColumns() + ")" + this.relationURI;
  }
}
