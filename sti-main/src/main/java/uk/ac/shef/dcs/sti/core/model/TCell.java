package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;

import uk.ac.shef.dcs.sti.util.DataTypeClassifier;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk) Date: 01/10/12 Time: 15:46
 */
public class TCell implements Serializable {

  private static final long serialVersionUID = -8136725814000405913L;

  private String text; // the raw text found in the table cell
  private String otherText;
  private String xPath; // xpath that extracts this value
  private DataTypeClassifier.DataType type;

  public TCell(final String text) {
    this.text = text;
    this.type = DataTypeClassifier.DataType.UNKNOWN;
    this.otherText = "";
  }


  public String getOtherText() {
    return this.otherText;
  }

  public String getText() {
    return this.text;
  }

  public DataTypeClassifier.DataType getType() {
    return this.type;
  }

  public String getxPath() {
    return this.xPath;
  }

  public void setOtherText(final String otherText) {
    this.otherText = otherText;
  }

  public void setText(final String text) {
    this.text = text;
  }

  public void setType(final DataTypeClassifier.DataType type) {
    this.type = type;
  }

  public void setxPath(final String xPath) {
    this.xPath = xPath;
  }

  @Override
  public String toString() {
    return "(" + getText() + ") " + getType();
  }
}
