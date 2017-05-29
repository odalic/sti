package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.io.Serializable;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.TableSchemaAdapter;

/**
 * <p>
 * This class represents a schema of the annotated table.
 * </p>
 *
 * @author Josef Janoušek
 *
 */
@Immutable
@XmlJavaTypeAdapter(TableSchemaAdapter.class)
public class TableSchema implements Serializable {

  private static final long serialVersionUID = 522894844434586518L;

  private final List<TableColumn> columns;

  /**
   * Creates new annotated table schema representation.
   *
   * @param columns annotated table columns
   */
  public TableSchema(final List<TableColumn> columns) {
    Preconditions.checkNotNull(columns, "The columns cannot be null!");

    this.columns = columns;
  }

  /**
   * Compares to another object for equality (only another TableSchema composed from equal parts
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
    final TableSchema other = (TableSchema) obj;
    if (this.columns == null) {
      if (other.columns != null) {
        return false;
      }
    } else if (!this.columns.equals(other.columns)) {
      return false;
    }
    return true;
  }

  /**
   * @return the columns
   */
  public List<TableColumn> getColumns() {
    return this.columns;
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
    result = (prime * result) + ((this.columns == null) ? 0 : this.columns.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "TableSchema [columns=" + this.columns + "]";
  }
}
