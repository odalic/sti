package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;
import java.util.List;

import uk.ac.shef.dcs.kbproxy.model.Attribute;

/**
 * annotates relation between two cells on the same row. Annotations on multi rows must be
 * aggregated to derive an annotation for two columns
 */
public class TCellCellRelationAnotation
    implements Serializable, Comparable<TCellCellRelationAnotation> {

  private static final long serialVersionUID = -1208912234750474692L;
  private RelationColumns relationColumns;
  private final int row;

  private final String relationURI;
  private String relationLabel;

  private List<Attribute> winningAttributes; // multiple winner possible
  private double winningAttributeMatchScore;

  // matched_value[]: (0)=property name (1)=the attribute value matched with the objecCol field on
  // this row; (2) id/uri, if any (used for later knowledge base retrieval)
  public TCellCellRelationAnotation(final RelationColumns key, final int row,
      final String relation_annotation, final String relation_label,
      final List<Attribute> winningAttributes, final double winningAttributeMatchScore) {
    this.relationColumns = key;
    this.row = row;
    this.relationURI = relation_annotation;
    this.relationLabel = relation_label;
    this.winningAttributeMatchScore = winningAttributeMatchScore;
    this.winningAttributes = winningAttributes;
  }

  public void addWinningAttributes(final List<Attribute> toAdd) {
    for (final Attribute vta : toAdd) {
      if (!this.winningAttributes.contains(vta)) {
        this.winningAttributes.add(vta);
      }
    }
  }

  @Override
  public int compareTo(final TCellCellRelationAnotation o) {
    final int compared = new Integer(o.getRow()).compareTo(getRow());

    if (compared == 0) {
      return new Double(o.getWinningAttributeMatchScore())
          .compareTo(getWinningAttributeMatchScore());
    }

    return compared;
  }



  @Override
  public boolean equals(final Object o) {
    if (o instanceof TCellCellRelationAnotation) {
      final TCellCellRelationAnotation that = (TCellCellRelationAnotation) o;
      return that.getRelationColumns().equals(getRelationColumns()) && (that.getRow() == getRow())
          && that.getRelationURI().equals(getRelationURI());
    }
    return false;
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

  public int getRow() {
    return this.row;
  }

  public double getWinningAttributeMatchScore() {
    return this.winningAttributeMatchScore;
  }

  public List<Attribute> getWinningAttributes() {
    return this.winningAttributes;
  }

  @Override
  public int hashCode() {
    return getRelationColumns().hashCode() + (19 * getRow()) + (29 * getRelationURI().hashCode());
  }

  public void setRelationColumns(final RelationColumns relationColumns) {
    this.relationColumns = relationColumns;
  }

  public void setRelationLabel(final String relationLabel) {
    this.relationLabel = relationLabel;
  }

  public void setWinningAttributeMatchScore(final double winningAttributeMatchScore) {
    this.winningAttributeMatchScore = winningAttributeMatchScore;
  }

  public void setWinningAttributes(final List<Attribute> winningAttributes) {
    this.winningAttributes = winningAttributes;
  }

  @Override
  public String toString() {
    return this.relationURI;
  }
}
