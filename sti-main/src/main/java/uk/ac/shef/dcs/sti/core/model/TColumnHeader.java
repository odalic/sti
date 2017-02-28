package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;
import java.util.List;

import uk.ac.shef.dcs.sti.core.subjectcol.TColumnDataType;
import uk.ac.shef.dcs.sti.core.subjectcol.TColumnFeature;

/**
 */
public class TColumnHeader implements Serializable

{

  private static final long serialVersionUID = -1638925814000405913L;

  private String headerText; // the raw text found in the table cell
  private String xPath; // xpath that extracts this value
  private List<TColumnDataType> type;
  private TColumnFeature feature;

  public TColumnHeader(final String text) {
    this.headerText = text;
  }

  public TColumnFeature getFeature() {
    return this.feature;
  }

  public String getHeaderText() {
    return this.headerText;
  }

  public String getHeaderXPath() {
    return this.xPath;
  }

  public List<TColumnDataType> getTypes() {
    return this.type;
  }

  public void setFeature(final TColumnFeature feature) {
    this.feature = feature;
  }

  public void setHeaderText(final String text) {
    this.headerText = text;
  }

  public void setHeaderXPath(final String xPath) {
    this.xPath = xPath;
  }

  public void setType(final List<TColumnDataType> type) {
    this.type = type;
  }
}
