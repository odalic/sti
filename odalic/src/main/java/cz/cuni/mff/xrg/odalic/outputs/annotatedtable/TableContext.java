package cz.cuni.mff.xrg.odalic.outputs.annotatedtable;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.TableContextAdapter;

/**
 * <p>
 * This class represents a context of the annotated table.
 * </p>
 *
 * @author Josef Janoušek
 *
 */
@Immutable
@XmlJavaTypeAdapter(TableContextAdapter.class)
public class TableContext implements Serializable {

  private static final long serialVersionUID = 522894844434586518L;

  private final String csvw = "http://www.w3.org/ns/csvw";

  private final Map<String, String> mapping;

  /**
   * Creates new annotated table context representation.
   *
   * @param mapping mapping prefixes to URIs
   */
  public TableContext(final Map<String, String> mapping) {
    Preconditions.checkNotNull(mapping, "The mapping cannot be null!");

    this.mapping = mapping;
  }

  /**
   * Compares to another object for equality (only another TableContext composed from equal parts
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
    final TableContext other = (TableContext) obj;
    if (this.csvw == null) {
      if (other.csvw != null) {
        return false;
      }
    } else if (!this.csvw.equals(other.csvw)) {
      return false;
    }
    if (this.mapping == null) {
      if (other.mapping != null) {
        return false;
      }
    } else if (!this.mapping.equals(other.mapping)) {
      return false;
    }
    return true;
  }

  /**
   * @return the csvw URI
   */
  public String getCsvw() {
    return this.csvw;
  }

  /**
   * @return the mapping
   */
  public Map<String, String> getMapping() {
    return this.mapping;
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
    result = (prime * result) + ((this.csvw == null) ? 0 : this.csvw.hashCode());
    result = (prime * result) + ((this.mapping == null) ? 0 : this.mapping.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "TableContext [csvw=" + this.csvw + ", mapping=" + this.mapping + "]";
  }
}
