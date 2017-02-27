package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;

/**
 * An TContext could be any textual content around an Table object.
 */
public class TContext implements Serializable, Comparable<TContext> {
  public enum TableContextType implements Serializable {

    CAPTION("Caption"), PAGETITLE("PageTitle"), // title of the page containing the table
    PARAGRAPH_BEFORE("Before"), // context occuring before table
    PARAGRAPH_AFTER("After"); // context occurring after table


    private String type;

    TableContextType(final String type) {
      this.type = type;
    }

    public String getType() {
      return this.type;
    }

    public void setType(final String type) {
      this.type = type;
    }
  }

  private static final long serialVersionUID = -8136777654860405913L;
  private String text;
  private double rankScore; // how relevant is this context to the table


  private TableContextType type;

  public TContext(final String text, final TableContextType type, final double score) {
    this.text = text;
    this.rankScore = score;
    this.type = type;
  }

  @Override
  public int compareTo(final TContext o) {
    return new Double(getImportanceScore()).compareTo(o.getImportanceScore());
  }

  public double getImportanceScore() {
    return this.rankScore;
  }

  public String getText() {
    return this.text;
  }

  public TableContextType getType() {
    return this.type;
  }

  public void setRankScore(final double rankScore) {
    this.rankScore = rankScore;
  }

  public void setText(final String text) {
    this.text = text;
  }

  public void setType(final TableContextType type) {
    this.type = type;
  }
}
