package uk.ac.shef.dcs.sti.core.subjectcol;

import java.io.Serializable;

/**
 */
public class TColumnFeature implements Serializable {

  private static final long serialVersionUID = -1208225814300474918L;

  private int colId;
  private int numRows;

  private TColumnDataType mostFrequentDataType;
  private boolean isFirstNEColumn;
  private boolean isOnlyNEColumn;
  private boolean isOnlyNonEmptyNEColumn;
  private boolean isOnlyNonDuplicateNEColumn;
  private double cellValueDiversity;
  private double tokenValueDiversity;
  private double contextMatchScore;
  private double webSearchScore;
  private int emptyCells;
  private boolean isIvalidPOS;
  private boolean isCode_or_Acronym;

  public TColumnFeature(final int colId, final int numRows) {
    this.colId = colId;
    this.numRows = numRows;
  }

  public double getCMScore() {
    return this.contextMatchScore;
  }

  public int getColId() {
    return this.colId;
  }

  public int getEmptyCellCount() {
    return this.emptyCells;
  }

  public TColumnDataType getMostFrequentDataType() {
    return this.mostFrequentDataType;
  }

  public int getNumRows() {
    return this.numRows;
  }

  public double getUniqueCellCount() {
    return this.cellValueDiversity;
  }

  public double getUniqueTokenCount() {
    return this.tokenValueDiversity;
  }

  public double getWSScore() {
    return this.webSearchScore;
  }

  public boolean isAcronymColumn() {
    return this.isCode_or_Acronym;
  }

  public boolean isFirstNEColumn() {
    return this.isFirstNEColumn;
  }

  public boolean isInvalidPOS() {
    return this.isIvalidPOS;
  }

  public boolean isOnlyNEColumn() {
    return this.isOnlyNEColumn;
  }

  public boolean isOnlyNonDuplicateNEColumn() {
    return this.isOnlyNonDuplicateNEColumn;
  }

  public boolean isOnlyNonEmptyNEColumn() {
    return this.isOnlyNonEmptyNEColumn;
  }

  public void setAcronymColumn(final boolean code_or_Acronym) {
    this.isCode_or_Acronym = code_or_Acronym;
  }

  public void setColId(final int colId) {
    this.colId = colId;
  }

  public void setContextMatchScore(final double contextMatchScore) {
    this.contextMatchScore = contextMatchScore;
  }

  public void setEmptyCellCount(final int emptyCells) {
    this.emptyCells = emptyCells;
  }

  public void setFirstNEColumn(final boolean firstNEColumn) {
    this.isFirstNEColumn = firstNEColumn;
  }

  public void setInvalidPOS(final boolean ivalidPOS) {
    this.isIvalidPOS = ivalidPOS;
  }

  public void setIsOnlyNonDuplicateNEColumn(final boolean isOnlyNonDuplicateNEColumn) {
    this.isOnlyNonDuplicateNEColumn = isOnlyNonDuplicateNEColumn;
  }

  public void setIsOnlyNonEmptyNEColumn(final boolean isOnlyNonEmptyNEColumn) {
    this.isOnlyNonEmptyNEColumn = isOnlyNonEmptyNEColumn;
  }

  public void setMostFrequentDataType(final TColumnDataType mostFrequentDataType) {
    this.mostFrequentDataType = mostFrequentDataType;
  }

  public void setNumRows(final int numRows) {
    this.numRows = numRows;
  }

  public void setOnlyNEColumn(final boolean onlyNEColumn) {
    this.isOnlyNEColumn = onlyNEColumn;
  }

  public void setUniqueCellCount(final double valueDiversity) {
    this.cellValueDiversity = valueDiversity;
  }

  public void setUniqueTokenCount(final double tokenValueDiversity) {
    this.tokenValueDiversity = tokenValueDiversity;
  }

  public void setWebSearchScore(final double webSearchScore) {
    this.webSearchScore = webSearchScore;
  }

  @Override
  public String toString() {
    return String.valueOf(this.colId);
  }
}
