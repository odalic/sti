package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.AnnotatedTableAdapter;

/**
 * <p>
 * This class represents an annotated table (CSV on the Web standard).
 * </p>
 *
 * @author Josef Janoušek
 *
 */
@Immutable
@XmlJavaTypeAdapter(AnnotatedTableAdapter.class)
public class AnnotatedTable implements Serializable {

  private static final long serialVersionUID = 164936506495425123L;

  private final TableContext context;

  private final String url;

  private final TableSchema tableSchema;

  /**
   * Creates new annotated table representation.
   *
   * @param context annotated table context
   * @param url name of corresponding CSV file
   * @param tableSchema annotated table schema
   */
  public AnnotatedTable(final TableContext context, final String url,
      final TableSchema tableSchema) {
    Preconditions.checkNotNull(context);
    Preconditions.checkNotNull(url);
    Preconditions.checkNotNull(tableSchema);

    this.context = context;
    this.url = url;
    this.tableSchema = tableSchema;
  }

  /**
   * Compares to another object for equality (only another AnnotatedTable composed from equal parts
   * passes).
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AnnotatedTable other = (AnnotatedTable) obj;
    if (this.context == null) {
      if (other.context != null) {
        return false;
      }
    } else if (!this.context.equals(other.context)) {
      return false;
    }
    if (this.url == null) {
      if (other.url != null) {
        return false;
      }
    } else if (!this.url.equals(other.url)) {
      return false;
    }
    if (this.tableSchema == null) {
      if (other.tableSchema != null) {
        return false;
      }
    } else if (!this.tableSchema.equals(other.tableSchema)) {
      return false;
    }
    return true;
  }

  /**
   * @return the context
   */
  public TableContext getContext() {
    return this.context;
  }

  /**
   * @return the table schema
   */
  public TableSchema getTableSchema() {
    return this.tableSchema;
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * Computes hash code based on all its parts.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.context == null) ? 0 : this.context.hashCode());
    result = (prime * result) + ((this.url == null) ? 0 : this.url.hashCode());
    result = (prime * result) + ((this.tableSchema == null) ? 0 : this.tableSchema.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "AnnotatedTable [context=" + this.context + ", url=" + this.url + ", tableSchema="
        + this.tableSchema + "]";
  }
}
