package uk.ac.shef.dcs.sti;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk) Date: 05/10/12 Time: 15:52
 */
public enum STIEnum {

  TABLE_HEADER_UNKNOWN("H_Unknown");

  private String value;

  STIEnum(final String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }

  public void setValue(final String value) {
    this.value = value;
  }
}
