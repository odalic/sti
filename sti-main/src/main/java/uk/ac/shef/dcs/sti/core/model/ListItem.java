package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk) Date: 10/10/12 Time: 12:25
 */
public class ListItem implements Serializable {
  private static final long serialVersionUID = -8136725546789405913L;
  private String text;
  private final Map<String, String> valueURIs;

  public ListItem(final String text) {
    this.valueURIs = new LinkedHashMap<>();
    this.text = text;
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof ListItem) {
      final ListItem c = (ListItem) o;
      return c.getText().equals(getText());
    }
    return false;
  }

  public String getText() {
    return this.text;
  }

  public Map<String, String> getValuesAndURIs() {
    return this.valueURIs;
  }

  @Override
  public int hashCode() {
    return getText().hashCode();
  }

  public void setText(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return this.text;
  }

}
