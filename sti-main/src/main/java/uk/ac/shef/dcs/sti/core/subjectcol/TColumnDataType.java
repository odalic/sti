package uk.ac.shef.dcs.sti.core.subjectcol;

import java.io.Serializable;

import uk.ac.shef.dcs.sti.util.DataTypeClassifier;

/**
 */
public class TColumnDataType implements Serializable, Comparable<TColumnDataType> {

  private static final long serialVersionUID = -1638925814006765913L;

  private DataTypeClassifier.DataType type; // what is the type of this column
  private int supportingRows; // how many rows contain this type of data in this column

  public TColumnDataType(final DataTypeClassifier.DataType type, final int supportingRows) {
    this.type = type;
    this.supportingRows = supportingRows;
  }

  @Override
  public int compareTo(final TColumnDataType o) {
    if (o.getType().equals(DataTypeClassifier.DataType.EMPTY)
        && !getType().equals(DataTypeClassifier.DataType.EMPTY)) {
      return -1;
    } else if (getType().equals(DataTypeClassifier.DataType.EMPTY)
        && !o.getType().equals(DataTypeClassifier.DataType.EMPTY)) {
      return 1;
    }

    return new Integer(o.getSupportingRows()).compareTo(getSupportingRows());
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof TColumnDataType) {
      return ((TColumnDataType) o).getType().equals(getType());
    }
    return false;
  }

  public int getSupportingRows() {
    return this.supportingRows;
  }

  public DataTypeClassifier.DataType getType() {
    return this.type;
  }

  public void setSupportingRows(final int supportingRows) {
    this.supportingRows = supportingRows;
  }

  public void setType(final DataTypeClassifier.DataType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return this.type + "," + getSupportingRows();
  }
}
