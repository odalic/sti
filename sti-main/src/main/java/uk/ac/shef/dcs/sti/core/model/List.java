package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk) Date: 09/10/12 Time: 11:07
 */
public class List implements Serializable {
  private static final long serialVersionUID = -8136725813759687613L;
  private final String sourceId;
  private final String listId;
  private final java.util.List<ListItem> items;
  private final java.util.List<String> contexts;

  public List(final String sourceId, final String listId) {
    this.sourceId = sourceId;
    this.listId = listId;
    this.items = new ArrayList<>();
    this.contexts = new ArrayList<>();
  }

  public void addContext(final String ctx) {
    this.contexts.add(ctx);
  }

  public void addItem(final ListItem item) {
    this.items.add(item);
  }

  public java.util.List<String> getContexts() {
    return this.contexts;
  }

  public java.util.List<ListItem> getItems() {
    return this.items;
  }

  public String getListId() {
    return this.listId;
  }

  public String getSourceId() {
    return this.sourceId;
  }

  @Override
  public String toString() {
    return getSourceId() + "," + getListId();
  }
}
